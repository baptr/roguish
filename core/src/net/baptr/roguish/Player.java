package net.baptr.roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player extends InputAdapter {

  private static final int CAST_COLS = 7;
  private static final int POKE_COLS = 8;
  private static final int WALK_COLS = 9;
  private static final int FLIP_COLS = 6;
  private static final int FIRE_COLS = 13;
  private static final int DEAD_COLS = 9;

  private static final int DIRECTIONS = 4;
  private static final int WALK_OFFSET = 2;

  private static final float SPEED = 3;

  public enum Direction {
    UP, LEFT, DOWN, RIGHT
  };

  // TODO(baptr): Factor most of this out to Entity.
  Animation[] walkAnimations;
  Texture walkSheet;
  TextureRegion[][] walkFrames;
  TextureRegion currentFrame;
  Rectangle bounds; // character bounding box
  Rectangle tmpRect; // used for bounds checks

  float stateTime;
  Vector2 v, iv;
  float x, y;
  Direction dir;
  Sprite sprite;

  Rectangle mapBounds;
  boolean[][] colMap;

  boolean usingMouse;
  Roguish game;

  public Player() {
    bounds = new Rectangle(0, 0, 0.9f, 0.7f);
    tmpRect = new Rectangle(0, 0, 1, 1);
    v = new Vector2();
  };

  public Player(Roguish game) {
    bounds = new Rectangle(0, 0, 0.9f, 0.7f);
    tmpRect = new Rectangle(0, 0, 1, 1);
    v = new Vector2();
    this.game = game;
  }

  public Player(int _x, int _y, Roguish game) {
    this(game);
    walkSheet = new Texture(Gdx.files.internal("lady48.png"));
    TextureRegion[][] tmp = TextureRegion.split(walkSheet, 48, 48);
    walkFrames = new TextureRegion[DIRECTIONS][WALK_COLS];
    walkAnimations = new Animation[DIRECTIONS];
    for (int i = 0; i < DIRECTIONS; i++) {
      for (int j = 0; j < WALK_COLS; j++) {
        walkFrames[i][j] = tmp[WALK_OFFSET * 4 + i][j];
      }
      walkAnimations[i] = new Animation(0.125f, walkFrames[i]);
    }
    stateTime = 0f;
    sprite = new Sprite(walkFrames[0][0]);
    sprite.setScale(1 / 32f);
    sprite.setOriginCenter();
    iv = new Vector2();
    x = _x;
    y = _y;
    dir = Direction.DOWN;

  }

  public void setColMap(boolean[][] m) {
    colMap = m;
    mapBounds = new Rectangle(0, 0, m[0].length, m.length);
  }

  // TODO(baptr): Move to PlayerInputHandler with mouse stuff.
  public boolean keyDown(int keyCode) {
    usingMouse = false;
    switch (keyCode) {
      case Keys.LEFT:
        dir = Direction.LEFT;
        iv.x -= 1;
        break;
      case Keys.RIGHT:
        dir = Direction.RIGHT;
        iv.x += 1;
        break;
      case Keys.DOWN:
        dir = Direction.DOWN;
        iv.y -= 1;
        break;
      case Keys.UP:
        dir = Direction.UP;
        iv.y += 1;
        break;
      default:
        return false;
    }
    v.set(iv);
    v.nor().scl(SPEED);
    return true;
  }

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
    if (!iv.isZero()) {
      if (iv.x > 0) {
        dir = Direction.RIGHT;
      }
      if (iv.x < 0) {
        dir = Direction.LEFT;
      }
      if (iv.y > 0) {
        dir = Direction.UP;
      }
      if (iv.y < 0) {
        dir = Direction.DOWN;
      }
    }
    v.set(iv);
    v.nor().scl(SPEED);
    return true;
  }

  public void updateVel() {
    if (MouseHandler.isDown()) {
      usingMouse = true;

      double angle =
          MathUtils.atan2(MouseHandler.x - game.screenCenter.x,
              game.screenCenter.y - MouseHandler.y);
      if (angle >= -0.419 && angle < 0.419) {
        // UP
        dir = Direction.UP;
        iv.y = 1;
      }
      if (angle >= 0.419 && angle < 1.325) {
        // UP-RIGHT
        dir = Direction.RIGHT;
        iv.x = 1;
        iv.y = 1;
      }
      if (angle >= 1.325 && angle < 1.815) {
        // RIGHT
        dir = Direction.RIGHT;
        iv.x = 1;
      }
      if (angle >= 1.815 && angle < 2.72) {
        // DOWN-RIGHT
        dir = Direction.RIGHT;
        iv.x = 1;
        iv.y = -1;
      }
      if (angle >= 2.72 || angle < -2.72) {
        // DOWN
        dir = Direction.DOWN;
        iv.y = -1;
      }
      if (angle >= -2.72 && angle < -1.815) {
        // DOWN-LEFT
        dir = Direction.LEFT;
        iv.x = -1;
        iv.y = -1;
      }
      if (angle >= -1.815 && angle < -1.325) {
        // LEFT
        dir = Direction.LEFT;
        iv.x = -1;
      }
      if (angle >= -1.325 && angle < -0.419) {
        // UP-LEFT
        dir = Direction.LEFT;
        iv.x = -1;
        iv.y = 1;
      }
    }

    if (usingMouse && !MouseHandler.isDown()) {
      iv.setZero();
    }

    v.set(iv);
    v.nor().scl(SPEED);
  }

  private float collide(float dx, float dy) {
    bounds.setCenter(x + dx, y + dy - 0.35f);
    float off = 0;
    for (int i = (int)x - 1; i < x + 1; i++) {
      for (int j = (int)y - 1; j < y + 1; j++) {
        if (blocked(i, j)) {
          tmpRect.x = i;
          tmpRect.y = j;
          if (tmpRect.overlaps(bounds)) {
            if (dx > 0 && i > x) {
              off = bounds.x + bounds.width - tmpRect.x;
              if (off > 0) {
                return dx;
              }
            } else if (dx < 0 && i < x) {
              off = tmpRect.x + tmpRect.width - bounds.x;
              if (off > 0) {
                return dx;
              }
            }
            if (dy > 0 && j > y) {
              off = bounds.y + bounds.height - tmpRect.y;
              if (off > 0) {
                return dy;
              }
            } else if (dy < 0 && j < y) {
              off = bounds.y - (tmpRect.y + tmpRect.height);
              if (off < 0) {
                return dy;
              }
            }
          }
        }
      }
    }
    return off;
  }

  protected void updatePos(float d) {
    float dx = v.x * d;
    float dy = v.y * d;

    dx -= collide(dx, 0);
    dy -= collide(0, dy);

    x += dx;
    y += dy;
  }

  protected boolean blocked(float x, float y) {
    if (y < 0 || y >= colMap.length) {
      return true;
    }
    if (x < 0 || x >= colMap[0].length) {
      return true;
    }
    return colMap[(int)y][(int)x];
  }

  public void render(SpriteBatch batch) {
    float d = Gdx.graphics.getDeltaTime();
    MouseHandler.update();
    updateVel();
    updatePos(d);

    if (v.isZero()) {
      stateTime = 0;
    } else {
      stateTime += d;
    }
    currentFrame = walkAnimations[dir.ordinal()].getKeyFrame(stateTime, true);

    sprite.setCenter(x, y);
    sprite.setRegion(currentFrame);
    sprite.draw(batch);
  }

  public void debugBounds(ShapeRenderer sh) {
    sh.rect(bounds.x, bounds.y, bounds.width, bounds.height);
  }
}
