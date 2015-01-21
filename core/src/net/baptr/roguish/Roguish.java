package net.baptr.roguish;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Roguish extends InputAdapter {
  SpriteBatch batch, hudBatch;
  Texture img;
  TiledMap map;
  MapLayers mapLayers;
  OrthogonalTiledMapRenderer renderer;
  Viewport viewport;
  OrthographicCamera camera;
  Player player;
  BitmapFont font;
  NetworkServer server;
  NetworkClient client;

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

  // InputAdapter
  @Override
  public boolean keyDown(int keyCode) {
    if (keyCode == Keys.ESCAPE) {
      Gdx.app.exit();
      return true;
    }
    if (keyCode == Keys.F1) {
      debugBounds = !debugBounds;
      return true;
    }
    if (keyCode == Keys.S) {
      try {
        startServer();
      } catch (IOException e) {
        System.out.println("Failed to start server" + e);
      }
      return true;
    }
    if (keyCode == Keys.C) {
      try {
        connect("localhost");
      } catch (IOException e) {
        System.out.println("Failed to connect:" + e);
      }
      return true;
    }
    return false;
  }

  private Monster monster;
  private ShapeRenderer sh;
  private boolean debugBounds;

  public int simTick;
  public int viewTick;
  public int maxEntityId;

  public static final float tickRate = 0.050f; // 50ms
  public static final int simOffset = 2; // viewTick + simOffset = simTick
  public static final String LOG = Roguish.class.getSimpleName();

  public Roguish() {
    batch = new SpriteBatch();
    hudBatch = new SpriteBatch();
    map = new TmxMapLoader().load("goblin_cave_test.tmx");
    renderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, batch);
    mapLayers = map.getLayers();

    camera = new OrthographicCamera();
    camera.position.set(4, 9, 0);
    viewport = new FitViewport(12, 10, camera);

    monster = new Monster(nextEntityId());
    Entity.colMap = colMap();

    font = new BitmapFont();
    sh = new ShapeRenderer();

    addEntity(monster);
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

  public void resize(int w, int h) {
    viewport.update(w, h);
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

  Color BGColor = new Color(32 / 255f, 29 / 255f, 32 / 255f, 1);

  public Map<Integer, RemotePlayer> players =
      new HashMap<Integer, RemotePlayer>();
  public Map<Integer, Entity> entities = new HashMap<Integer, Entity>();

  public void render(float delta) {
    /*
     * Gdx.gl.glClearColor(BGColor.r, BGColor.g, BGColor.b, BGColor.a);
     * Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
     */
    if (player != null) {
      camera.position.set(player.pos, 0);
    } else {
      camera.position.set(2, 8, 0);
    }
    camera.update();
    renderer.setView(camera);

    batch.begin();
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(0)); // Floor
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(1)); // North

    // TODO(baptr): y-sort characters for rendering.
    for (Entity e : entities.values()) {
      e.render(batch);
    }

    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(3)); // South
    batch.end();

    hudBatch.begin();
    font.draw(hudBatch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, 20);
    hudBatch.end();

    if (debugBounds) {
      sh.setProjectionMatrix(camera.combined);
      sh.begin(ShapeType.Line);
      player.debugBounds(sh);
      sh.end();
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
