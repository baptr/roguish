package net.baptr.roguish.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Manager extends com.badlogic.gdx.Game {
  public Skin skin;

  public Manager() {
  }

  @Override
  public void create() {
    skin = new Skin(Gdx.files.internal("uiskin.json"));

    setScreen(new MenuScreen(this));
  }
}
