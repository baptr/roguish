package net.baptr.roguish.screen;

import java.net.InetAddress;

import net.baptr.roguish.Roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class ConnectScreen extends AbstractScreen {
  public ConnectScreen(Manager g) {
    super(g);

    Table table = new Table(manager.skin);
    table.setFillParent(true);
    stage.addActor(table);

    final List<String> serverList = new List<String>(manager.skin);
    final TextField serverField = new TextField("localhost", manager.skin);
    final TextField nameField = new TextField("name", manager.skin);

    loadServerList(serverList, serverField);
    loadName(nameField);

    serverList.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeListener.ChangeEvent event, Actor actor) {
        serverField.setText(serverList.getSelected());
      }
    });

    TextButton connectButton = new TextButton("Connect", manager.skin);
    connectButton.addListener(new ClickListener() {
      private PlayScreen playScreen;

      public void clicked(InputEvent e, float x, float y) {
        try {
          InetAddress.getByName(serverField.getText());
        } catch (java.net.UnknownHostException ue) {
          new Dialog("Unknown Host", new Window.WindowStyle())
              .text("Unable to resolve host: " + ue.getMessage()).button("Ok")
              .show(stage);
          Gdx.app.log(Roguish.LOG, "Unknown host: " + ue.getMessage());
          return;
        }

        if (playScreen == null) {
          playScreen = new PlayScreen(manager);
        }
        playScreen.setName(nameField.getText());

        try {
          playScreen.initConnection(serverField.getText());
        } catch (java.io.IOException ioe) {
          new Dialog("Connection Error", manager.skin).text(ioe.getMessage())
              .button("Ok").show(stage);
          Gdx.app.log(Roguish.LOG, "Connection error: " + ioe.getMessage());
          return;
        }

        serverField.getOnscreenKeyboard().show(false);
        saveServerList(serverList, serverField);
        saveName(nameField);
        manager.setScreen(playScreen);
      }
    });

    TextButton backButton = new TextButton("Back", manager.skin);
    backButton.addListener(new ClickListener() {
      public void clicked(InputEvent e, float x, float y) {
        manager.setScreen(new MenuScreen(manager));
      }
    });

    table.add("Name: ").left();
    table.add(nameField).minWidth(50f).spaceBottom(10).row();
    table.add("Recent Servers").colspan(2).left().row();
    table.add(serverList).colspan(2).fillX().spaceBottom(10).row();
    table.add("Server: ").minWidth(75f).left();
    table.add(serverField).minWidth(200f).row();
    table.add(backButton);
    table.add(connectButton).right();
  }

  private void loadName(TextField nameField) {
    String name = preferences.getString("player_name");
    if (!name.isEmpty()) {
      nameField.setText(name);
    }
  }

  private void saveName(TextField nameField) {
    preferences.putString("player_name", nameField.getText());
  }

  private void loadServerList(List<String> serverList, TextField serverField) {
    String save = preferences.getString("server_list");
    Json json = new Json();
    String[] servers = json.fromJson(String[].class, save);
    if (servers != null) {
      serverList.setItems(servers);
      serverField.setText(servers[0]);
    } else {
      serverList.setItems(new String[] {"localhost"});
      serverField.setText("localhost");
    }
  }

  private void saveServerList(List<String> serverList, TextField serverField) {
    Array<String> list = serverList.getItems();
    String current = serverField.getText();
    int idx = list.indexOf(current, false);
    if (idx >= 0) {
      list.swap(0, idx);
    } else {
      list.insert(0, current);
    }
    Json json = new Json();
    String save = json.toJson(list);
    preferences.putString("server_list", save);
  }
}
