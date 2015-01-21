package net.baptr.roguish.screen;

import java.io.IOException;

import net.baptr.roguish.PlayerInputHandler;
import net.baptr.roguish.Roguish;

import com.badlogic.gdx.Gdx;

public class PlayScreen extends AbstractScreen {
  /* Ehhh maybe game really needs to be a Screen. :-/ */
  Roguish game;

  public PlayScreen(Manager g) {
    super(g);

    game = new Roguish();
    input.addProcessor(game);
    input.addProcessor(new PlayerInputHandler());
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
    game.render(delta);
    // TODO(baptr): Overlay stage on demand for pause screen?
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
    game.resize(w, h);
  }

}
