package net.baptr.roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

  public enum Direction { UP, LEFT, DOWN, RIGHT };

  Animation[]                     walkAnimations;
  Texture                         walkSheet;
  TextureRegion[][]               walkFrames;
  SpriteBatch                     spriteBatch;
  TextureRegion                   currentFrame;

  float stateTime;
  Vector2 v, iv;
  float x, y;
  Direction dir;
  Sprite sprite;

  boolean[][] colMap;

  public Player(int _x, int _y) {
    walkSheet = new Texture(Gdx.files.internal("lady48.png"));
    TextureRegion[][] tmp = TextureRegion.split(walkSheet, 48, 48);
    walkFrames = new TextureRegion[DIRECTIONS][WALK_COLS];
    walkAnimations = new Animation[DIRECTIONS];
    for (int i = 0; i < DIRECTIONS; i++) {
      for (int j = 0; j < WALK_COLS; j++) {
        walkFrames[i][j] = tmp[WALK_OFFSET*4+i][j];
      }
      walkAnimations[i] = new Animation(0.125f, walkFrames[i]);
    }
    spriteBatch = new SpriteBatch();
    stateTime = 0f;
    sprite = new Sprite(walkFrames[0][0]);
    sprite.setScale(1/32f);
    sprite.setOriginCenter();
    v = new Vector2();
    iv = new Vector2();
    x = _x;
    y = _y;
    dir = Direction.DOWN;
  }

  public void setColMap(boolean[][] m) {
    colMap = m;
  }

  public boolean keyDown(int keyCode) {
    switch(keyCode) {
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
    switch(keyCode) {
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
      if (iv.x > 0) { dir = Direction.RIGHT; }
      if (iv.x < 0) { dir = Direction.LEFT; }
      if (iv.y > 0) { dir = Direction.UP; }
      if (iv.y < 0) { dir = Direction.DOWN; }
    }
    v.set(iv);
    v.nor().scl(SPEED);
    return true;
  }

  public void render(Camera cam) {
    float d = Gdx.graphics.getDeltaTime();
    stateTime += d;
    float dx = v.x * d;
    float dy = v.y * d;
    // TODO(baptr): Cleanup and use character bounds.
    if (x+dx < 0) { dx = 0; }
    if (y+dy < 0) { dy = 0; }
    if (colMap[(int)y][(int)(x+dx)]) { dx = 0; }
    if (colMap[(int)(y+dy)][(int)x]) { dy = 0; }
    x += dx;
    y += dy;
    sprite.setCenter(x, y);
    currentFrame = walkAnimations[dir.ordinal()].getKeyFrame(stateTime, true);
    sprite.setRegion(currentFrame);
    spriteBatch.setProjectionMatrix(cam.combined);
    spriteBatch.begin();
    sprite.draw(spriteBatch);
    spriteBatch.end();
  }
}
