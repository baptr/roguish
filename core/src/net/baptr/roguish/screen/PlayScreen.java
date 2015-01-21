package net.baptr.roguish.screen;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import net.baptr.roguish.Entity;
import net.baptr.roguish.PlayerInputHandler;
import net.baptr.roguish.Roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayScreen extends AbstractScreen {
  Roguish game;

  SpriteBatch batch, hudBatch;
  Texture img;
  MapLayers mapLayers;
  OrthogonalTiledMapRenderer renderer;
  Viewport viewport;
  OrthographicCamera camera;

  Color BGColor = new Color(32 / 255f, 29 / 255f, 32 / 255f, 1);

  private ShapeRenderer sh;
  private boolean debugBounds;

  public PlayScreen(Manager g) {
    super(g);

    game = new Roguish();
    input.addProcessor(game);
    input.addProcessor(new PlayerInputHandler());

    batch = new SpriteBatch();
    hudBatch = new SpriteBatch();
    renderer = new OrthogonalTiledMapRenderer(game.map, 1 / 32f, batch);
    mapLayers = game.map.getLayers();

    camera = new OrthographicCamera();
    camera.position.set(4, 9, 0);
    viewport = new FitViewport(12, 10, camera);

    sh = new ShapeRenderer();
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
    return false;
  }

  public void selfHost() throws IOException {
    game.startServer();
    game.connect("localhost");
  }

  public void initConnection(String server) throws IOException {
    game.connect(server);
  }

  public void setName(String name) {
    game.setName(name);
  }

  @Override
  public void render(float delta) {
    game.update(delta);
    Gdx.gl.glClearColor(BGColor.r, BGColor.g, BGColor.b, BGColor.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if (game.player != null) {
      camera.position.set(game.player.pos, 0);
    } else {
      camera.position.set(2, 8, 0);
    }
    camera.update();
    renderer.setView(camera);

    batch.begin();
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(0)); // Floor
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(1)); // North

    // TODO(baptr): This should probably be more efficient.
    Entity[] entities = game.entities.values().toArray(new Entity[0]);
    Arrays.sort(entities, new Comparator<Entity>() {
      public int compare(Entity arg0, Entity arg1) {
        return Float.compare(arg1.pos.y, arg0.pos.y);
      }
    });
    for (Entity e : entities) {
      e.render(batch);
    }

    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(3)); // South
    batch.end();

    hudBatch.begin();
    manager.font.draw(hudBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0,
        20);
    hudBatch.end();

    if (debugBounds) {
      sh.setProjectionMatrix(camera.combined);
      sh.begin(ShapeType.Line);
      game.player.debugBounds(sh);
      sh.end();
    }
  }

  @Override
  public void update(float delta) {
    game.update(delta);
  }

  @Override
  public void resize(int w, int h) {
    super.resize(w, h);
    PlayerInputHandler.centerX = Gdx.graphics.getWidth() / 2;
    PlayerInputHandler.centerY = Gdx.graphics.getHeight() / 2;
    viewport.update(w, h);
  }

}
