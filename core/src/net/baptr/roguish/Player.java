package net.baptr.roguish;

public class Player extends Entity {
  protected static final float SPEED = 3;

  public Player(int id, int x, int y) {
    super(id);
    sprite = Sprite.load("lady48.png");
    pos.set(x, y);
  }

  @Override
  protected void updateVel() {
    PlayerInputHandler.update();
    vel.set(PlayerInputHandler.iv);
    vel.nor().scl(SPEED);
  }
}