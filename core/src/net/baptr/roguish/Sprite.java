package net.baptr.roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Sprite extends com.badlogic.gdx.graphics.g2d.Sprite {
  // TODO(baptr): Other animations
  private static final int CAST_COLS = 7;
  private static final int POKE_COLS = 8;
  private static final int WALK_COLS = 9;
  private static final int FLIP_COLS = 6;
  private static final int FIRE_COLS = 13;
  private static final int DEAD_COLS = 9;

  private static final int DIRECTIONS = 4;
  private static final int WALK_OFFSET = 2;

  private static final int TILE_WIDTH = 48;
  private static final int TILE_HEIGHT = 48;

  Animation[] walkAnimations;
  TextureRegion[][] walkFrames;

  public Sprite(Texture sheet) {
    TextureRegion[][] tmp = TextureRegion.split(sheet, TILE_WIDTH, TILE_HEIGHT);
    walkFrames = new TextureRegion[DIRECTIONS][WALK_COLS];
    walkAnimations = new Animation[DIRECTIONS];
    for (int i = 0; i < DIRECTIONS; i++) {
      for (int j = 0; j < WALK_COLS; j++) {
        walkFrames[i][j] = tmp[WALK_OFFSET * 4 + i][j];
      }
      walkAnimations[i] = new Animation(0.125f, walkFrames[i]);
    }
    // Init the actual sprite.
    setRegion(walkFrames[0][0]);
    setSize(TILE_WIDTH, TILE_HEIGHT);
    setScale(1 / 32f);
    setOriginCenter();
  }

  public static Sprite load(String filename) {
    // TODO(baptr): Cache per filename.
    return new Sprite(new Texture(Gdx.files.internal(filename)));
  }

  public TextureRegion getFrame(int dir, float stateTime) {
    return walkAnimations[dir].getKeyFrame(stateTime, true);
  }
}