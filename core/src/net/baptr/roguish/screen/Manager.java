package net.baptr.roguish.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Manager extends com.badlogic.gdx.Game {
  public Skin skin;
  public BitmapFont font;

  public Manager() {
  }

  @Override
  public void create() {
    skin = new Skin(Gdx.files.internal("uiskin.json"));
    font = skin.getFont("default-font");

    setScreen(new MenuScreen(this));
  }
}
