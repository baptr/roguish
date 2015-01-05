package net.baptr.roguish;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.baptr.roguish.Player;

public class Roguish extends ApplicationAdapter {
  SpriteBatch batch;
  Texture img;
  TiledMap map;
  MapLayers mapLayers;
  OrthogonalTiledMapRenderer renderer;
  Viewport viewport;
  OrthographicCamera camera;
  Player player;

  class InputController extends InputAdapter {
    @Override
    public boolean keyDown(int keyCode) {
      if (keyCode == Keys.ESCAPE) {
        Gdx.app.exit();
        return true;
      }
      return false;
    }
  }
  InputController inputController;

  @Override
  public void create () {
    batch = new SpriteBatch();
    //img = new Texture("rogueliketiles.png");
    map = new TmxMapLoader().load("goblin_cave_test.tmx");
    renderer = new OrthogonalTiledMapRenderer(map, 1/32f, batch);
    mapLayers = map.getLayers();

    camera = new OrthographicCamera();
    camera.position.set(4, 9, 0);
    viewport = new FitViewport(12, 10, camera);

    inputController = new InputController();
    player = new Player(2, 8);
    Gdx.input.setInputProcessor(new InputMultiplexer(inputController, player));

    player.setColMap(colMap());
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
    viewport.update(w, h);
  }

  @Override
  public void render () {
    Gdx.gl.glClearColor(32/255f, 29/255f, 32/255f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    camera.position.set(player.x, player.y, 0);
    camera.update();
    renderer.setView(camera);
    batch.begin();
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(0));
    // TODO(baptr): Figure out if it's better to use different batches for different sprite sheets.
    player.render(batch);
    renderer.renderTileLayer((TiledMapTileLayer)mapLayers.get(1));
    batch.end();
  }
}
