package net.baptr.roguish;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class NetworkClient extends Listener {
  Client client;
  int id = -1;
  Queue<Network.FullSync> syncs = new LinkedList<Network.FullSync>();

  public NetworkClient() {
    client = new Client();
    client.start();

    Network.register(client);
    client.addListener(this);
  }

  public boolean connect(String name) {
    try {
      client.connect(100, "localhost", Network.PORT, Network.PORT);
    } catch (IOException e) {
      System.out.println("Unable to connect" + e);
      return false;
    }
    Network.Join j = new Network.Join();
    j.user = name;
    j.version = "0";
    client.sendTCP(j);

    client.updateReturnTripTime();
    return true;
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
      System.out.printf("Received tick %d full sync\n", sync.tick);
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

  void sendIV(int tick, Vector2 v) {
    Network.InputVector iv = new Network.InputVector();
    iv.playerId = id;
    iv.tick = tick;
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
          if (np.id == id) { // me
            continue;
          }
          if (!game.players.containsKey(np.id)) {
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
    }
  }

  public void update(Roguish game, float delta) {
    sendIV(game.viewTick, PlayerInputHandler.iv); // TODO(baptr): simTick?

    procSync(game);
    // Increment viewTick if necessary
    // Interpolate state + ivs if necessary
    // Update game state
  }
}
