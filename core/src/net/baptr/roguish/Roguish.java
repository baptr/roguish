package net.baptr.roguish;

import java.io.IOException;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

public class Roguish extends ApplicationAdapter {
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

  class InputController extends InputAdapter {
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
          server = new NetworkServer();
          System.out.println("Server started");
        } catch (IOException e) {
          System.out.println("Failed to start server" + e);
        }
        return true;
      }
      if (keyCode == Keys.C) {
        client = new NetworkClient();
        client.connect("me");
        System.out.println("Connected");
        return true;
      }
      return false;
    }
  }

  InputController inputController;
  private Monster monster;
  private ShapeRenderer sh;
  private boolean debugBounds;

  public int simTick;
  public int viewTick;

  public static final int tickRate = 50; // In ms
  public static final int simOffset = 2; // viewTick + simOffset = simTick

  @Override
  public void create() {
    batch = new SpriteBatch();
    hudBatch = new SpriteBatch();
    map = new TmxMapLoader().load("goblin_cave_test.tmx");
    renderer = new OrthogonalTiledMapRenderer(map, 1 / 32f, batch);
    mapLayers = map.getLayers();

    camera = new OrthographicCamera();
    camera.position.set(4, 9, 0);
    viewport = new FitViewport(12, 10, camera);

    inputController = new InputController();
    player = new Player(2, 8);
    Gdx.input.setInputProcessor(new InputMultiplexer(inputController,
        new PlayerInputHandler()));

    monster = new Monster();
    Entity.colMap = colMap();

    font = new BitmapFont();
    sh = new ShapeRenderer();
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

  @Override
  public void resize(int w, int h) {
    PlayerInputHandler.centerX = Gdx.graphics.getWidth() / 2;
    PlayerInputHandler.centerY = Gdx.graphics.getHeight() / 2;
    viewport.update(w, h);
  }

  private void update(float delta) {
    player.update(delta);
    monster.update(delta);
    if (client != null) {
      client.update(this, delta);
    }
    if (server != null) {
      server.update(this, delta);
    }
  }

  Color BGColor = new Color(32 / 255f, 29 / 255f, 32 / 255f, 1);

  @Override
  public void render() {
    float delta = Gdx.graphics.getDeltaTime();
    update(delta);

    Gdx.gl.glClearColor(BGColor.r, BGColor.g, BGColor.b, BGColor.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    camera.position.set(player.pos, 0);
    camera.update();
    renderer.setView(camera);

    batch.begin();
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(0));
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(1));
    // TODO(baptr): Figure out if it's better to use different batches for
    // different sprite sheets.
    // TODO(baptr): y-sort characters for rendering.
    monster.render(batch);
    player.render(batch);
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(3));
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
}
