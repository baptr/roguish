package net.baptr.roguish;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
  public static final int PORT = 13824;

  public static void register(EndPoint endpoint) {
    Kryo kryo = endpoint.getKryo();
    kryo.register(Join.class);
    kryo.register(Part.class);
    kryo.register(Player.class);
    kryo.register(Player[].class);
    kryo.register(InputVector.class);
    kryo.register(Entity.class);
    kryo.register(Entity[].class);
    kryo.register(FullSync.class);
  }

  // New client details on connect.
  // Client -> Server.
  public static class Join {
    public String user;
    public String version;
  }

  // Player details on join/sync.
  // Server -> Clients.
  public static class Player {
    public int id;
    public String name;
    // TODO(baptr): Avatar/weapon/stat info.
  }

  // Client disconnects.
  // Server -> Clients.
  public static class Part {
    public Player player; // TODO(baptr): Just send the id/name?
  }

  // Update input results.
  // Client -> Server.
  public static class InputVector {
    public int playerId; // Populated by server if missing.

    // TODO(baptr): Pack tick and offset into a single long?
    public int tick;
    public float offset;

    // Normalized input vector components
    public float x;
    public float y;
    // TODO(baptr): Attack/etc.
  }

  // Dynamic state of a player or monster entity.
  // Server -> Clients.
  public static class Entity {
    public int id;
    public int tick;
    public int type; // TODO(baptr): Enum?

    // Current position.
    public float x;
    public float y;

    // Current velocity, including speed.
    public float vx;
    public float vy;
    // TODO(baptr): HP and stuff
  }

  // Full state sync.
  // Server -> Clients.
  public static class FullSync {
    public int tick;
    public Player[] players;
    public Entity[] entities;
    // TODO(baptr): Map info
  }
}