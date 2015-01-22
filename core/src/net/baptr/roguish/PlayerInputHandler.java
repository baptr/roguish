package net.baptr.roguish;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

// TODO(rjr): Should this stay static?
public class PlayerInputHandler extends InputAdapter {

  public static int mouseX;
  public static int mouseY;
  public static boolean mouseDown;
  public static boolean lastMouseDown;
  public static boolean usingMouse;
  public static int centerX;
  public static int centerY;

  public static Vector2 iv = new Vector2();

  public static boolean isDown() {
    return mouseDown;
  }

  public static boolean isPressed() {
    return mouseDown && !lastMouseDown;
  }

  public static boolean isReleased() {
    return !mouseDown && lastMouseDown;
  }

  public static void update() {
    lastMouseDown = mouseDown;

    if (isDown()) {
      usingMouse = true;

      double angle = MathUtils.atan2(mouseX - centerX, centerY - mouseY);
      if (angle >= -0.419 && angle < 0.419) { // Up
        iv.x = 0;
        iv.y = 1;
      } else if (angle >= 0.419 && angle < 1.325) { // Up-right
        iv.x = 1;
        iv.y = 1;
      } else if (angle >= 1.325 && angle < 1.815) { // Right
        iv.x = 1;
        iv.y = 0;
      } else if (angle >= 1.815 && angle < 2.720) { // Down-right
        iv.x = 1;
        iv.y = -1;
      } else if (angle >= 2.720 || angle < -2.720) { // Down
        iv.x = 0;
        iv.y = -1;
      } else if (angle >= -2.72 && angle < -1.815) { // Down-left
        iv.x = -1;
        iv.y = -1;
      } else if (angle >= -1.815 && angle < -1.325) { // Left
        iv.x = -1;
        iv.y = 0;
      } else if (angle >= -1.325 && angle < -0.419) { // Up-left
        iv.x = -1;
        iv.y = 1;
      }
    }

    if (usingMouse && !isDown()) {
      iv.setZero();
    }
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    mouseX = screenX;
    mouseY = screenY;
    mouseDown = true;
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    mouseX = screenX;
    mouseY = screenY;
    mouseDown = false; // TODO(baptr): Multitouch?
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    mouseX = screenX;
    mouseY = screenY;
    mouseDown = true;
    return true;
  }

  public boolean keyDown(int keyCode) {
    usingMouse = false;
    switch (keyCode) {
      case Keys.LEFT:
        iv.x -= 1;
        break;
      case Keys.RIGHT:
        iv.x += 1;
        break;
      case Keys.DOWN:
        iv.y -= 1;
        break;
      case Keys.UP:
        iv.y += 1;
        break;
      default:
        return false;
    }
    return true;
  }

  @Override
  public boolean keyUp(int keyCode) {
    switch (keyCode) {
      case Keys.LEFT:
        iv.x += 1;
        break;
      case Keys.RIGHT:
        iv.x -= 1;
        break;
      case Keys.DOWN:
        iv.y += 1;
        break;
      case Keys.UP:
        iv.y -= 1;
        break;
      default:
        return false;
    }
    return true;
  }
}