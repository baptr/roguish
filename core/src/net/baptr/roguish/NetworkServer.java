package net.baptr.roguish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class NetworkServer extends Listener {
  private static final boolean debug = false;

  Server server;
  List<PlayerConnection> conns = new ArrayList<PlayerConnection>();
  // TODO(baptr): Queue?
  List<Network.InputVector> ivs = new ArrayList<Network.InputVector>();
  List<PlayerConnection> pendingJoins = new ArrayList<PlayerConnection>();
  List<PlayerConnection> pendingParts = new ArrayList<PlayerConnection>();
  private float simAccum;

  public NetworkServer() throws IOException {
    server = new Server() {
      protected Connection newConnection() {
        return new PlayerConnection();
      }
    };

    Network.register(server);

    server.addListener(this);
    server.bind(Network.PORT, Network.PORT);
    server.start();
  }

  static class PlayerConnection extends Connection {
    String version;
    Network.Player player;
    // TODO(baptr): Track/display latency.
  }

  @Override
  public void received(Connection c, Object o) {
    PlayerConnection pc = (PlayerConnection)c;

    if (o instanceof Network.Join) {
      if (pc.player != null) {
        return;
      }
      Network.Join j = (Network.Join)o;
      pc.version = j.version;
      pc.player = new Network.Player();
      pc.player.name = j.user;
      pc.player.id = -1;
      System.out.println("Connected: " + pc.player.name);

      // Queue a full sync.
      pendingJoins.add(pc);
    }

    if (o instanceof Network.InputVector) {
      Network.InputVector iv = (Network.InputVector)o;
      // System.out.println("Updated (" + iv.x + ", " + iv.y + ")");
      if (iv.playerId != pc.player.id) {
        System.out.printf("%s is a liar! (%d != %d) in InputVector\n",
            pc.player.name, iv.playerId, pc.player.id);
        // pc.close();
      }
      iv.playerId = pc.player.id;
      // Queue use of update.
      synchronized (ivs) {
        ivs.add(iv);
      }
    }
  }

  @Override
  public void disconnected(Connection c) {
    PlayerConnection pc = (PlayerConnection)c;
    if (pc.player != null) {
      System.out.println("Disconnected: " + pc.player.name);

      pendingParts.add(pc);
    }
    conns.remove(pc);
  }

  private void syncState(Roguish game) {
    // TODO(baptr): Deltas, short circuiting, object caching.
    Network.FullSync sync = new Network.FullSync();
    sync.tick = game.simTick;
    List<Network.Player> tmpPlayers = new ArrayList<Network.Player>();
    for (PlayerConnection pc : conns) {
      tmpPlayers.add(pc.player);
    }
    sync.players = tmpPlayers.toArray(new Network.Player[0]);
    List<Network.Entity> tmpEntities = new ArrayList<Network.Entity>();
    for (Entity e : game.entities.values()) {
      Network.Entity ne = new Network.Entity();
      ne.id = e.id;
      ne.tick = game.simTick;
      ne.vx = e.vel.x;
      ne.vy = e.vel.y;
      ne.x = e.pos.x;
      ne.y = e.pos.y;

      tmpEntities.add(ne);
    }
    sync.entities = tmpEntities.toArray(new Network.Entity[0]);
    server.sendToAllTCP(sync);
  }

  public void update(Roguish game, float delta) {
    // Announce departed players
    for (PlayerConnection pc : pendingParts) {
      Network.Part d = new Network.Part();
      d.player = pc.player;
      server.sendToAllTCP(d);
      game.entities.remove(pc.player.id);
      game.players.remove(pc.player.id);
    }
    pendingParts.clear();

    // Add any new players
    for (PlayerConnection pc : pendingJoins) {
      pc.player.id = game.addPlayer(pc.player.name);

      conns.add(pc);

      // Announce join to all connected players.
      server.sendToAllTCP(pc.player);
    }
    pendingJoins.clear();

    // Process IVs
    synchronized (ivs) {
      for (Network.InputVector iv : ivs) {
        if (debug) {
          System.out.printf("Applying %d's IV for tick %d\n", iv.playerId,
              iv.tick);
        }
        // TODO(baptr): Validate tick.
        RemotePlayer p = game.players.get(iv.playerId);
        if (p != null) {
          p.iv.set(iv.x, iv.y);
        } else {
          System.out.printf("No such player %d!\n", iv.playerId);
        }
      }
      ivs.clear();
    }

    // Update simTick if necessary
    if (delta > Roguish.tickRate) {
      System.out.println("Lost ticks!");
    }
    simAccum += delta;
    float simNow = 0;
    while (simAccum >= Roguish.tickRate) {
      game.simTick++;
      simNow += Roguish.tickRate;
      simAccum -= Roguish.tickRate;
    }

    // Sim tick
    if (simNow > 0) {
      if (debug) {
        System.out.printf("Simulating tick %d + %f @ %f\n", game.simTick,
            simAccum, simNow);
      }
      // Update positions/run AI
      game.updateEntities(simNow);

      // Send syncs if necessary
      syncState(game);
    }
  }
}