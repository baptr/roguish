package net.baptr.roguish;

import com.badlogic.gdx.math.Vector2;

public class RemotePlayer extends Player {
  public Vector2 iv = new Vector2();

  public RemotePlayer(int id, int x, int y) {
    super(id, x, y);
  }

  @Override
  protected void updateVel() {
    vel.set(iv);
    vel.nor().scl(SPEED);
  }
}