package net.baptr.roguish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class NetworkServer extends Listener {
  Server server;
  int maxConnId = 0;
  List<PlayerConnection> conns = new ArrayList<PlayerConnection>();
  // TODO(baptr): Queue?
  List<Network.InputVector> ivs = new ArrayList<Network.InputVector>();
  List<PlayerConnection> pendingSyncs = new ArrayList<PlayerConnection>();

  public NetworkServer() throws IOException {
    server = new Server() {
      protected Connection newConnection() {
        return new PlayerConnection();
      }
    };

    Network.register(server);

    server.addListener(this);
    server.bind(Network.PORT);
    server.start();
  }

  static class PlayerConnection extends Connection {
    String version;
    Network.Player player;
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
      pc.player.id = maxConnId++;
      System.out.println("Connected: " + pc.player.name);

      // TODO(baptr): lock?
      conns.add(pc);
      // Announce join to other connected players.
      for (PlayerConnection oc : conns) {
        oc.sendTCP(pc.player);
      }

      // Queue a full sync.
      pendingSyncs.add(pc);
    }

    if (o instanceof Network.InputVector) {
      Network.InputVector iv = (Network.InputVector)o;
      System.out.println("Updated (" + iv.x + ", " + iv.y + ")");
      if (iv.playerId != pc.player.id) {
        System.out.printf("%s is a liar! (%d != %d) in InputVector\n",
            pc.player.name, iv.playerId, pc.player.id);
        // pc.close();
      }
      iv.playerId = pc.player.id;
      // Queue use of update.
      ivs.add(iv);
    }
  }

  @Override
  public void disconnected(Connection c) {
    PlayerConnection pc = (PlayerConnection)c;
    System.out.println("Disconnected: " + pc.player.name);
    conns.remove(pc);

    // Announce to other connected players.
    Network.Part d = new Network.Part();
    d.player = pc.player;
    for (PlayerConnection oc : conns) {
      oc.sendTCP(d);
    }
  }

  public void update(Roguish game, float delta) {
    // Process IVs
    // Update simTick if necessary
    // Update positions/run AI
    // Send syncs if necessary
  }
}