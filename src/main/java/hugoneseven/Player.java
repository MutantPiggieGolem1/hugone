package hugoneseven;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;

import hugoneseven.Constants.MoveState;
import hugoneseven.Constants.Direction;
import hugoneseven.Constants.KeyPress;
import hugoneseven.util.Utils;

public class Player extends Character implements KeyListener {
  public ArrayList<String> inventory = new ArrayList<String>();
  private Direction direction = Direction.DOWN;
  private MoveState movestate = MoveState.STOP;
  private int framenum = 0;
  public int money;
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

  public void keyTyped(KeyEvent e) {
  };

  public void keyPressed(KeyEvent e) {
    int kc = e.getKeyCode();
    switch (kc) {
      case KeyEvent.VK_W:
      case KeyEvent.VK_A:
      case KeyEvent.VK_S:
      case KeyEvent.VK_D:
        this.todir = Utils.dirkeys.get(kc);
        this.tomove = MoveState.MOVE1;
        App.story.getCurrent().reccieveKeyPress(e,KeyPress.KEYDOWN);
        break;
      case KeyEvent.VK_SPACE:
        this.spacedown = true;
        break;
    }
  }

  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_W:
      case KeyEvent.VK_A:
      case KeyEvent.VK_S:
      case KeyEvent.VK_D:
        this.tomove = MoveState.STOP;
        App.story.getCurrent().reccieveKeyPress(e,KeyPress.KEYUP);
        break;
      case KeyEvent.VK_SPACE:
        this.spacedown = false;
        break;
    }
  }

  public void render(Graphics2D g) {
    super.render(this.direction, this.movestate, g);
  }

  public boolean spaceDown() {
    if (!this.spacedown)
      return false;
    this.spacedown = false;
    return true;
  }

  public Point facingTowards() {
    Point delta = Utils.getChange(this.direction);
    Point pclone= ((Point)this.pos.clone());
    pclone.translate(delta.x, delta.y);
    return pclone;
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

    if (Math.round(this.framenum % 3) == 0L)
      this.movestate = super.movemap.get(this.movestate); // update the move state
    this.framenum++;

    App.shit.put("ploc1",this.pos);
    App.shit.put("ploc",this.getCenter());
    if (!a.checkCollisions(Utils.getChange(this.getCenter(),this.direction,10)))
      this.pos = this.facingTowards();
  }

  public void teleport(Point loc) {
    this.pos = (Point)loc.clone();
  }
}