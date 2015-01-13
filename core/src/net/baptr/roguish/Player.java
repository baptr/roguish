package net.baptr.roguish;

public class Player extends Entity {
  protected static final float SPEED = 3;

  public Player(int id, int x, int y) {
    super(id);
    loadSprite("lady48.png");
    pos.set(x, y);
  }

  protected void updateVel() {
    PlayerInputHandler.update();
    vel.set(PlayerInputHandler.iv);
    vel.nor().scl(SPEED);
  }

  @Override
  public void update(float delta) {
    updateVel();
    super.update(delta);
  }
}
