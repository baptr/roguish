package net.baptr.roguish;

import com.badlogic.gdx.math.MathUtils;

public class Monster extends Entity {

  private int heading;

  public Monster(int id) {
    super(id);
    sprite = Sprite.load("skeleton.png");
    pos.set(3, 9);
  }

  @Override
  protected void updateVel() {
    int d = MathUtils.round(MathUtils.randomTriangular(5)) / 5;
    heading = (heading + d + 4) % 4;
    vel.setZero();
    switch (heading) {
      case 0: // Up
        vel.y = 1;
        break;
      case 1: // Left
        vel.x = -1;
        break;
      case 2: // Down
        vel.y = -1;
        break;
      case 3: // Right
        vel.x = 1;
        break;
    }
  }
}