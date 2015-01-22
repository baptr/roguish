package net.baptr.roguish;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class NetworkClient extends Listener {
  private static final boolean debug = false;
  Client client;
  int id = -1;
  Queue<Network.FullSync> syncs = new LinkedList<Network.FullSync>();
  private float viewAccum;

  public NetworkClient() {
    client = new Client();
    client.start();

    Network.register(client);
    client.addListener(this);
  }

  public void connect(String name) throws IOException {
    client.connect(1000, "localhost", Network.PORT, Network.PORT);
    Network.Join j = new Network.Join();
    j.user = name;
    j.version = "0";
    client.sendTCP(j);

    client.updateReturnTripTime();
  }

  @Override
  public void received(Connection c, Object o) {
    if (o instanceof Network.Player) {
      Network.Player p = (Network.Player)o;
      System.out.printf("Received player announcement %s (%d)\n", p.name, p.id);
      if (id == -1) { // My announcement
        System.out.println("I'm here!");
        id = p.id;
        client.updateReturnTripTime();
      } else {
        System.out.printf("Player %s connected!\n", p.name);
      }
      return;
    }
    if (o instanceof Network.Part) {
      Network.Part p = (Network.Part)o;
      System.out.printf("Player %s disconnected!\n", p.player.name);
      return;
    }
    if (o instanceof Network.FullSync) {
      Network.FullSync sync = (Network.FullSync)o;
      if (debug) {
        System.out.printf("Received tick %d full sync\n", sync.tick);
      }
      synchronized (syncs) {
        syncs.add(sync);
      }
      return;
    }
    // TODO(baptr): Partial syncs (raw Entity messages?)
    System.out.println("Unhandled client message: " + o.getClass().getName());
  }

  @Override
  public void disconnected(Connection c) {
    System.out.println("I disconnected");
    // TODO(baptr): Pause/reconnect/resync/exit
  }

  void sendIV(int tick, float offset, Vector2 v) {
    Network.InputVector iv = new Network.InputVector();
    iv.playerId = id;
    iv.tick = tick;
    iv.offset = offset;
    iv.x = v.x;
    iv.y = v.y;
    client.sendTCP(iv); // TODO(baptr): UDP for just movement?
  }

  private void procSync(Roguish game) {
    synchronized (syncs) {
      // Process any Syncs
      for (Network.FullSync s : syncs) {
        if (s.tick < game.viewTick) {
          System.out.printf("Received stale FullSync! %d < %d < %d\n", s.tick,
              game.viewTick, game.simTick);
          // TODO(baptr): Request a new sync?
          continue;
        }
        for (Network.Player np : s.players) {
          if (np.id == id) {
            if (game.player == null) {
              game.player = new Player(np.id, 2, 8);
              game.addEntity(game.player);
            }
          } else if (!game.players.containsKey(np.id)) {
            game.addPlayer(np.name, np.id);
          }
        }
        for (Network.Entity ne : s.entities) {
          Entity e = game.entities.get(ne.id);
          if (e != null) {
            e.pos.set(ne.x, ne.y);
            e.vel.set(ne.vx, ne.vy);
          } else {
            System.out.println("Can't create remote entities yet");
          }
        }
        // Always jump sim state to the FullSync state, and view state to
        // fullsync-simOffset?
      }
      syncs.clear();
    }
  }

  public void update(Roguish game, float delta) {
    // Increment viewTick if necessary
    if (delta > Roguish.tickRate) {
      System.out.println("Lost view tick!");
    }
    viewAccum += delta;
    while (viewAccum >= Roguish.tickRate) {
      viewAccum -= Roguish.tickRate;
      game.viewTick++;
    }

    // TODO(baptr): Queue up iv changes in real time with precise offsets.
    sendIV(game.viewTick, viewAccum, PlayerInputHandler.iv);

    procSync(game);

    if (game.server == null) { // Prevent duplicate updates if self-hosted.
      game.updateEntities(delta, false);
    }
    // Interpolate state + ivs if necessary
    // Update game state
  }
}
