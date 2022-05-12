package hugone;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;

import hugone.Constants.MoveState;
import hugone.Constants.Direction;
import hugone.Constants.KeyPress;
import hugone.util.Utils;

public class Player extends Character implements KeyListener {
  public ArrayList<String> inventory = new ArrayList<String>();
  private Direction direction = Direction.DOWN;
  private MoveState movestate = MoveState.STOP;
  private int framenum = 0;
  public int money;
  private boolean sprinting = false;
  private boolean spacedown = false;
  private MoveState tomove;
  private Direction todir;

  public Player(String name) throws JSONException {
    super("PLAYER");
    super.setName(name);
  }

  public void addItem(String itemid, int count) {
    String[] temp = new String[count];
    Arrays.fill(temp, itemid);
    inventory.addAll(Arrays.asList(temp));
  }

  public void addItem(String itemid) {
    inventory.add(itemid);
  }

  @Override
  public void keyTyped(KeyEvent e) {
  };

  @Override
  public void keyPressed(KeyEvent e) {
    int kc = e.getKeyCode();
    App.story.getCurrent().reccieveKeyPress(e,KeyPress.KEYDOWN);
    switch (kc) {
      case KeyEvent.VK_W:
      case KeyEvent.VK_A:
      case KeyEvent.VK_S:
      case KeyEvent.VK_D:
        this.todir = Utils.dirkeys.get(kc);
        this.tomove = MoveState.MOVE1;
        break;
      case KeyEvent.VK_X:
        this.sprinting = true;
        break;
      case KeyEvent.VK_SPACE:
        this.spacedown = true;
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    App.story.getCurrent().reccieveKeyPress(e,KeyPress.KEYUP);
    switch (e.getKeyCode()) {
      case KeyEvent.VK_W:
      case KeyEvent.VK_A:
      case KeyEvent.VK_S:
      case KeyEvent.VK_D:
        this.tomove = MoveState.STOP;
        break;
      case KeyEvent.VK_X:
        this.sprinting = false;
        break;
      case KeyEvent.VK_SPACE:
        this.spacedown = false;
        break;
    }
  }

  public void render(Graphics2D g) {
    Point facing = this.facingTowards();
    g.drawLine((int)facing.getX(),(int)facing.getY(),(int)facing.getX(),(int)facing.getY());
    super.render(this.direction, this.movestate, g);
  }

  public boolean spaceDown() {
    if (!this.spacedown) return false;
    this.spacedown = false;
    return true;
  }

  public Point facingTowards() { // WARNING: ONLY FOR INTERACTS
    Point delta = Utils.getChange(this.direction);
    Point pclone= ((Point)this.pos.clone());
    pclone.translate(delta.x, delta.y);
    return pclone;
  }

  private Point walkingTowards(boolean center) {
    return Utils.getChange(center ? this.getCenter() : this.pos,this.direction,this.sprinting ? 15 : 10);
  }

  public void moveLoop() {
    Area a = (Area) App.story.getCurrent();
    if (a.renderingDialogue()) {
      this.movestate = MoveState.STOP;
      return;
    }
    if (this.tomove != null && (this.movestate.equals(MoveState.STOP) || this.tomove.equals(MoveState.STOP)))
      this.movestate = this.tomove;
    if (this.todir != null)
      this.direction = this.todir;
    if (this.movestate.equals(MoveState.STOP)) {
      this.framenum = 0;
      return;
    } // dont move while stopped

    if (Math.round(this.framenum % (this.sprinting ? 1.1 : 2)) == 0L)
      this.movestate = super.movemap.get(this.movestate); // update the move state
    this.framenum++;

    if (!a.checkCollisions(this.walkingTowards(true)))
      this.pos = this.walkingTowards(false);
  }

  public void teleport(Point loc) {
    this.pos = (Point)loc.clone();
  }

  public void stopMovement() {
    this.tomove = null;
    this.todir = null;
    this.movestate = MoveState.STOP;
    this.framenum = 0;
  }
}