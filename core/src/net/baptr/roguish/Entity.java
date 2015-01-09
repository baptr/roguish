package net.baptr.roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Entity {

  private static final int CAST_COLS = 7;
  private static final int POKE_COLS = 8;
  private static final int WALK_COLS = 9;
  private static final int FLIP_COLS = 6;
  private static final int FIRE_COLS = 13;
  private static final int DEAD_COLS = 9;

  private static final int DIRECTIONS = 4;
  private static final int WALK_OFFSET = 2;

  private static final float BOUNDS_Y_OFFSET = -0.35f;

  public enum Direction {
    UP, LEFT, DOWN, RIGHT
  };

  Animation[] walkAnimations;
  Texture walkSheet;
  TextureRegion[][] walkFrames;
  TextureRegion currentFrame;
  Rectangle bounds; // character bounding box
  Rectangle tmpRect; // used for bounds checks

  float stateTime;
  Vector2 vel;
  Vector2 pos;
  Direction dir;
  Sprite sprite;

  static boolean[][] colMap;

  public Entity() {
    bounds = new Rectangle(0, 0, 0.9f, 0.7f);
    tmpRect = new Rectangle(0, 0, 1, 1);
    vel = new Vector2();
    pos = new Vector2();
    dir = Direction.DOWN;
  };

  protected void loadSprite(String filename) {
    walkSheet = new Texture(Gdx.files.internal(filename));
    TextureRegion[][] tmp = TextureRegion.split(walkSheet, 48, 48);
    walkFrames = new TextureRegion[DIRECTIONS][WALK_COLS];
    walkAnimations = new Animation[DIRECTIONS];
    for (int i = 0; i < DIRECTIONS; i++) {
      for (int j = 0; j < WALK_COLS; j++) {
        walkFrames[i][j] = tmp[WALK_OFFSET * 4 + i][j];
      }
      walkAnimations[i] = new Animation(0.125f, walkFrames[i]);
    }
    sprite = new Sprite(walkFrames[0][0]);
    sprite.setScale(1 / 32f);
    sprite.setOriginCenter();
  }

  private float collide(float dx, float dy) {
    bounds.setCenter(pos.x + dx, pos.y + dy + BOUNDS_Y_OFFSET);
    int x = (int)pos.x;
    int y = (int)pos.y;
    float off = 0;
    for (int i = x - 1; i <= x + 1; i++) {
      for (int j = y - 1; j <= y + 1; j++) {
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
    float dx = vel.x * d;
    float dy = vel.y * d;

    dx -= collide(dx, 0);
    dy -= collide(0, dy);

    pos.x += dx;
    pos.y += dy;

    if (dx > 0) {
      dir = Direction.RIGHT;
    } else if (dx < 0) {
      dir = Direction.LEFT;
    } else if (dy > 0) {
      dir = Direction.UP;
    } else if (dy < 0) {
      dir = Direction.DOWN;
    }
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
    updatePos(d);

    if (vel.isZero()) {
      stateTime = 0;
    } else {
      stateTime += d;
    }
    currentFrame = walkAnimations[dir.ordinal()].getKeyFrame(stateTime, true);

    sprite.setCenter(pos.x, pos.y);
    sprite.setRegion(currentFrame);
    sprite.draw(batch);
  }

  public void debugBounds(ShapeRenderer sh) {
    sh.rect(bounds.x, bounds.y, bounds.width, bounds.height);
  }
}
