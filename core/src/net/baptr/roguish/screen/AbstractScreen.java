package net.baptr.roguish.screen;

import net.baptr.roguish.Roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class AbstractScreen extends InputAdapter implements Screen {
  protected final Manager manager;
  protected final Stage stage;
  protected final InputMultiplexer input;
  protected final Preferences preferences;

  protected final Color bgColor;

  public AbstractScreen(Manager manager) {
    this.manager = manager;
    stage = new Stage();
    input = new InputMultiplexer(stage, this);
    preferences = Gdx.app.getPreferences(Roguish.LOG);
    bgColor = new Color(0, 0, 0, 1);
  }

  protected String getName() {
    return getClass().getSimpleName();
  }

  public void update(float delta) {
    stage.act(delta);
  }

  // Screen implementation
  @Override
  public void show() {
    Gdx.app.log(Roguish.LOG, "Showing screen: " + getName());
    Gdx.input.setInputProcessor(input);
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height);
  }

  @Override
  public void render(float delta) {
    this.update(delta);

    // the following code clears the screen with the given RGB color (black)
    Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // update and draw the stage actors
    stage.draw();
  }

  @Override
  public void hide() {
    Gdx.app.log(Roguish.LOG, "Hiding screen: " + getName());
  }

  @Override
  public void pause() {
    Gdx.app.log(Roguish.LOG, "Pausing screen: " + getName());
    preferences.flush();
  }

  @Override
  public void resume() {
    Gdx.app.log(Roguish.LOG, "Resuming screen: " + getName());
  }

  @Override
  public void dispose() {
    Gdx.app.log(Roguish.LOG, "Disposing screen: " + getName());

    // dispose the collaborators
    stage.dispose();
  }
}
