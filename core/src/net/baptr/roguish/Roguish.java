package net.baptr.roguish;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import net.baptr.roguish.Player;

public class Roguish extends ApplicationAdapter {
  SpriteBatch batch;
  Texture img;
  TiledMap map;
  OrthogonalTiledMapRenderer renderer;
  OrthographicCamera camera;
  Player player;

  class OrthoCamController extends InputAdapter {
    final OrthographicCamera camera;
    final Vector3 curr = new Vector3();
    final Vector3 last = new Vector3(-1, -1, -1);
    final Vector3 delta = new Vector3();

    public OrthoCamController (OrthographicCamera camera) {
      this.camera = camera;
    }

    @Override
    public boolean touchDragged (int x, int y, int pointer) {
      camera.unproject(curr.set(x, y, 0));
      if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
        camera.unproject(delta.set(last.x, last.y, 0));
        delta.sub(curr);
        camera.position.add(delta.x, delta.y, 0);
      }
      last.set(x, y, 0);
      return false;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
      last.set(-1, -1, -1);
      return false;
    }
  }
  OrthoCamController cameraController;

  @Override
  public void create () {
    batch = new SpriteBatch();
    img = new Texture("rogueliketiles.png");
    map = new TmxMapLoader().load("goblin_cave_test.tmx");
    renderer = new OrthogonalTiledMapRenderer(map, 1/32f);

    float w = Gdx.graphics.getWidth();
    float h = Gdx.graphics.getHeight();

    camera = new OrthographicCamera();
    //camera.setToOrtho(false, (w / h) * 20, 20);
    camera.setToOrtho(false, w/32f, h/32f);
    camera.update();

    cameraController = new OrthoCamController(camera);
    Gdx.input.setInputProcessor(cameraController);

    player = new Player();
  }


  @Override
  public void render () {
    Gdx.gl.glClearColor(32/255f, 29/255f, 32/255f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    camera.update();
    renderer.setView(camera);
    renderer.render();
    player.render();
    /*
    batch.begin();
    batch.draw(img, 0, 0);
    batch.end();
    */
  }
}
