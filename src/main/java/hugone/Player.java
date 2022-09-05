package hugone;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import hugone.Constants.MoveState;
import hugone.Constants.Direction;
import hugone.Constants.GameState;
import hugone.Constants.KeyPress;
import hugone.util.Utils;

class Player extends Character {
  private Direction direction = Direction.DOWN;
  private MoveState movestate = MoveState.STOP;
  private MoveState tomove;
  private Direction todir;
  private int framenum = 0;
  private boolean sprinting = false;
  private boolean spacedown = false;

  public Set<String> inventory;
  // private ArrayList<Character> followers = new ArrayList<Character>();
  
  public Player(String name) throws org.json.JSONException {
    super("PLAYER");
    super.setName(name);

    this.init();
    // this.followers.add(App.story.getCharacter("ENEMY_1"));
  }

  public void addItem(String itemid, int count) {
    String[] temp = new String[count];
    Arrays.fill(temp, itemid);
    inventory.addAll(Arrays.asList(temp));
  }

  public void addItem(String itemid) {
    inventory.add(itemid);
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {
    int kc = e.getKeyCode();
    switch (p) {
      case KEYDOWN:
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
        break;
      case KEYUP:
        switch (kc) {
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
        break;
    }
    
  }

  public void render(Graphics2D g) {
    super.render(this.direction, this.movestate, g);
  }

  public boolean spaceDown() {
    if (!this.spacedown) return false;
    this.spacedown = false;
    return true;
  }

  public Rectangle facingTowards() {
    return facingTowards(false);
  }
  public Rectangle facingTowards(boolean walking) {
    return Utils.getChange(this.pos, this.direction, walking&&this.sprinting ? 15 : 10);
  }

  public void moveLoop() {
    if (!App.story.currentState().equals(GameState.EXPLORATION)) return;
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

    if (Math.round(this.framenum % (this.sprinting ? 1.4 : 2)) == 0L)
      this.movestate = Character.movemap.get(this.movestate); // update the move state
    this.framenum++;

    Rectangle goal = this.facingTowards(true);
    if (!a.collidesWith(goal)) {
      this.pos = goal;
    }
  }

  public void teleport(java.awt.Point loc) {
    this.pos.setLocation(loc);
  }

  public void stopMovement() {
    this.tomove = null;
    this.todir = null;
    this.movestate = MoveState.STOP;
    this.framenum = 0;
  }

  public void init() {
    if (this.maxhealth < 0) System.out.println("!WARNING! Attempted to initialize a healthless player!");
    this.init(this.maxhealth, new HashSet<>());
  }

  public void init(int health, Set<String> hashSet) {
    this.health = health;
    this.inventory = hashSet;
  }
}