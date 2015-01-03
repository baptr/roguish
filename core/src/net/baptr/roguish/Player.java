package net.baptr.roguish;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player extends ApplicationAdapter {

  private static final int        CAST_COLS = 7;
  private static final int        POKE_COLS = 8;
  private static final int        WALK_COLS = 9;
  private static final int        FLIP_COLS = 6;
  private static final int        FIRE_COLS = 13;
  private static final int        DEAD_COLS = 9;

  private static final int DIRECTIONS = 4;
  private static final int  WALK_OFFSET = 2;

  public enum Direction { UP, LEFT, DOWN, RIGHT };

  Animation[]                     walkAnimations;
  Texture                         walkSheet;
  TextureRegion[][]               walkFrames;
  SpriteBatch                     spriteBatch;
  TextureRegion                   currentFrame;

  float stateTime;
  float x, y;
  Direction dir;

  public Player() {
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
    x = 50;
    y = 50;
    dir = Direction.DOWN;
  }

  @Override
  public void render() {
    stateTime += Gdx.graphics.getDeltaTime();
    currentFrame = walkAnimations[dir.ordinal()].getKeyFrame(stateTime, true);
    spriteBatch.begin();
    spriteBatch.draw(currentFrame, x, y);
    spriteBatch.end();
  }
}
