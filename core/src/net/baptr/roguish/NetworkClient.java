package net.baptr.roguish;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class NetworkClient extends Listener {
  Client client;
  int id;

  public NetworkClient() {
    client = new Client();
    client.start();

    Network.register(client);
    client.addListener(this);
  }

  public boolean connect(String name) {
    try {
      client.connect(100, "localhost", Network.PORT);
    } catch (IOException e) {
      System.out.println("Unable to connect" + e);
      return false;
    }
    Network.Join j = new Network.Join();
    j.user = name;
    j.version = "0";
    client.sendTCP(j);
    return true;
  }

  @Override
  public void received(Connection c, Object o) {
    System.out.println(o.getClass().getName());
  }

  @Override
  public void disconnected(Connection c) {
    System.out.println("Disconnected");
  }

  public void update(Roguish game, float delta) {
    Network.InputVector iv = new Network.InputVector();
    iv.tick = game.viewTick; // TODO(baptr): Or is it simTick?
    iv.x = PlayerInputHandler.iv.x;
    iv.y = PlayerInputHandler.iv.y;
    client.sendTCP(iv);

    // Process any Syncs
    // Increment viewTick if necessary
    // Interpolate state + ivs if necessary
    // Update game state
  }
}
