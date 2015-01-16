package net.baptr.roguish;

public class Player extends Entity {
  protected static final float SPEED = 3;

  public Player(int id, int x, int y) {
    super(id);
    loadSprite("lady48.png");
    pos.set(x, y);
  }

  private void updateVel() {
    vel.set(PlayerInputHandler.iv);
    vel.nor().scl(SPEED);
  }

  @Override
  public void update(float delta) {
    PlayerInputHandler.update();
    updateVel();
    super.update(delta);
  }
}
