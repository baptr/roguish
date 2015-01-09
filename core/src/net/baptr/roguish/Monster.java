package net.baptr.roguish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Monster extends Player {
  private static final int DIRECTIONS = 4;
  private static final int WALK_COLS = 9;
  private static final int WALK_OFFSET = 2;

  Sprite sprite;
  Animation[] anims;
  float stateTime;

  Vector2 pos;
  Vector2 vel;
  int dir;

  public Monster(Roguish game) {
    Texture walkSheet = new Texture(Gdx.files.internal("skeleton.png"));
    TextureRegion[][] tmp = TextureRegion.split(walkSheet, 48, 48);
    TextureRegion[][] walkFrames = new TextureRegion[DIRECTIONS][WALK_COLS];
    anims = new Animation[DIRECTIONS];
    for (int i = 0; i < DIRECTIONS; i++) {
      for (int j = 0; j < WALK_COLS; j++) {
        walkFrames[i][j] = tmp[WALK_OFFSET * 4 + i][j];
      }
      anims[i] = new Animation(0.125f, walkFrames[i]);
    }
    stateTime = 0f;
    sprite = new Sprite(walkFrames[0][0]);
    sprite.setScale(1 / 32f);
    sprite.setOriginCenter();

    pos = new Vector2(3, 9);
    x = 3;
    y = 9;
    vel = new Vector2();
    dir = 3;
  }

  protected void updatePos(float delta) {
    int d = MathUtils.round(MathUtils.randomTriangular(5)) / 5;
    dir = (dir + d + DIRECTIONS) % DIRECTIONS;
    v.setZero();
    switch (dir) {
      case 0: // Up
        v.y = 1;
        break;
      case 1: // Left
        v.x = -1;
        break;
      case 2: // Down
        v.y = -1;
        break;
      case 3: // Right
        v.x = 1;
        break;
    }
    super.updatePos(delta);
  }

  public void render(SpriteBatch batch) {
    float d = Gdx.graphics.getDeltaTime();
    updatePos(d);
    stateTime += d;

    sprite.setCenter(x, y);
    sprite.setRegion(anims[dir].getKeyFrame(stateTime, true));
    sprite.draw(batch);
  }
}
