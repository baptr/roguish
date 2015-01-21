package net.baptr.roguish;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class Roguish extends InputAdapter {
  public Player player;
  NetworkServer server;
  NetworkClient client;

  public Map<Integer, RemotePlayer> players =
      new HashMap<Integer, RemotePlayer>();
  public Map<Integer, Entity> entities = new HashMap<Integer, Entity>();

  public TiledMap map;
  String playerName = "me";

  public void setName(String name) {
    playerName = name;
  }

  public void startServer() throws IOException {
    if (client != null) {
      System.out.println("Not starting server, already a client");
      return;
    }
    server = new NetworkServer();
    System.out.println("Server started");
  }

  public void connect(String server) throws IOException {
    if (client != null && client.client.isConnected()) {
      System.out.println("Already connected!");
      return;
    }
    client = new NetworkClient();
    client.connect(playerName);
    System.out.println("Connected");
  }

  public int simTick;
  public int viewTick;
  public int maxEntityId;

  public static final float tickRate = 0.050f; // 50ms
  public static final int simOffset = 2; // viewTick + simOffset = simTick
  public static final String LOG = Roguish.class.getSimpleName();

  public Roguish() {
    map = new TmxMapLoader().load("goblin_cave_test.tmx");
    Entity.colMap = colMap();

    // TODO(baptr): Only make the monster server side; send it to the client.
    /* As it is, I'm relying on both sides reserving id 0 for the skeleton. */
    addEntity(new Monster(nextEntityId()));
  }

  protected void addEntity(Entity e) {
    entities.put(e.id, e);
  }

  private boolean[][] colMap() {
    TiledMapTileLayer l = (TiledMapTileLayer)(map.getLayers().get(0));
    int h = l.getHeight();
    int w = l.getWidth();
    boolean[][] map = new boolean[h][w];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        TiledMapTileLayer.Cell cell = l.getCell(x, y);
        map[y][x] = (cell == null);
      }
    }
    return map;
  }

  public void update(float delta) {
    // TODO(baptr): Do local update prediction.
    if (client != null) {
      client.update(this, delta);
    }
    if (server != null) {
      server.update(this, delta);
    }
  }

  public void updateEntities(float delta) {
    for (Entity e : entities.values()) {
      e.update(delta);
    }
  }

  private int nextEntityId() { // TOOD(baptr): Synchronized
    return maxEntityId++;
  }

  public int addPlayer(String name) {
    return addPlayer(name, nextEntityId());
  }

  public int addPlayer(String name, int id) {
    RemotePlayer rp = new RemotePlayer(id, 2, 8);
    players.put(rp.id, rp);
    entities.put(rp.id, rp);
    System.out.printf("Welcome %s!\n", name);
    return id;
  }
}
