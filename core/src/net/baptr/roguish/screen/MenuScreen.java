package net.baptr.roguish.screen;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MenuScreen extends AbstractScreen {

  public MenuScreen(Manager g) {
    super(g);
    Table table = new Table(manager.skin);
    table.setFillParent(true);
    stage.addActor(table);

    TextButton startButton = new TextButton("Start Game", manager.skin);
    startButton.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        PlayScreen playScreen = new PlayScreen(manager);
        try {
          playScreen.selfHost();
        } catch (IOException e) {
          new Dialog("Failed to self-host", manager.skin).text(e.getMessage())
              .button("Ok").show(stage);
          return;
        }
        manager.setScreen(playScreen);
      }
    });

    TextButton connectButton = new TextButton("Connect To Game", manager.skin);
    connectButton.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        manager.setScreen(new ConnectScreen(manager));
      }
    });

    TextButton exitButton = new TextButton("Exit", manager.skin);
    exitButton.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        Gdx.app.exit();
      }
    });

    table.add(startButton).minWidth(1f).row();
    table.add(connectButton).minWidth(1f);
    table.row().pad(1f);
    table.add(exitButton).minWidth(1f);
  }
}
