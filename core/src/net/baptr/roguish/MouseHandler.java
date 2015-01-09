package net.baptr.roguish;

import com.badlogic.gdx.InputAdapter;

// TODO(rjr): I put mouse handling in this random class, but not 100% sure
// about it. might want to put ALL handling here, including buttons. Also left
// all the static crap from my platformer because I have it a static thing. woo.
public class MouseHandler extends InputAdapter {

  public static int x;
  public static int y;
  public static boolean down;
  public static boolean pdown;

  public static boolean isDown() {
    return down;
  }

  public static boolean isPressed() {
    return down && !pdown;
  }

  public static boolean isReleased() {
    return !down && pdown;
  }

  public static void update() {
    pdown = down;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    x = screenX;
    y = screenY;
    down = true;
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    x = screenX;
    y = screenY;
    down = false; // TODO(baptr): Multitouch?
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    x = screenX;
    y = screenY;
    down = true;
    return true;
  }
}
