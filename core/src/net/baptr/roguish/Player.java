package net.baptr.roguish;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player extends Entity {
  private static final float SPEED = 3;

  public Player(int x, int y) {
    super();
    loadSprite("lady48.png");
    pos.set(x, y);
  }

  private void updateVel() {
    vel.set(PlayerInputHandler.iv);
    vel.nor().scl(SPEED);
  }

  public void render(SpriteBatch batch) {
    PlayerInputHandler.update();
    updateVel();
    super.render(batch);
  }
}
