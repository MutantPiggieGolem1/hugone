package hugoneseven;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;

import hugoneseven.enums.Direction;
import hugoneseven.util.Utils;

public class Player extends Character implements KeyListener {
  public ArrayList<String> inventory = new ArrayList<String>();
  private Direction direction = Direction.UP;
  private MoveState movestate = MoveState.STOP;
  private int framenum = 0;
  public int money;
  private boolean interacting = false;
  
  public Player(String name) throws JSONException {
    super("PLAYER");
    super.setName(name);
  }

  public void addItem(String itemid, int count){
    String[] temp = new String[count];
    Arrays.fill(temp,itemid);
    inventory.addAll(Arrays.asList(temp));
  }

  public void addItem(String itemid) {
    inventory.add(itemid);
  }

  public void keyTyped(KeyEvent e){};
  public void keyPressed(KeyEvent e) {
    int kc = e.getKeyCode();
    switch (kc) {
      case KeyEvent.VK_W:
      case KeyEvent.VK_A:
      case KeyEvent.VK_S:
      case KeyEvent.VK_D:
        this.direction = Utils.dirkeys.get(kc);
        if (this.movestate.equals(MoveState.STOP)) this.movestate = MoveState.MOVE1;
      break;
      case KeyEvent.VK_SPACE:
        this.interacting = true;
      break;
    }
  }
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_W:
      case KeyEvent.VK_A:
      case KeyEvent.VK_S:
      case KeyEvent.VK_D:
        this.movestate = MoveState.STOP;
      break;
      case KeyEvent.VK_SPACE:
        this.interacting = false;
      break;
    }
  }

  public void render(Graphics2D g) {
    super.render(this.direction,this.movestate,g);
  }

  public boolean spaceDown() {
    return this.interacting;
  }
  public boolean facingTowards(HashSet<List<Integer>> coords) {
    int[] delta = Utils.getChange(this.direction);
    List<Integer> target = Arrays.asList(pos[0]+delta[0],pos[1]+delta[1]);
    return coords.contains(target);
  }
  public void moveLoop() {    
    if (this.movestate.equals(MoveState.STOP)) {this.framenum=0;return;} // dont move while stopped
    if (Math.round(this.framenum%3) == 0L) this.movestate = super.movemap.get(this.movestate); // update the move state
    this.framenum++;
    Area a = ((Area)App.story.getCurrent());

    int[] delta = Utils.getChange(this.direction); // not performant but seems to be no other way. perhaps undo this
    int[] target= new int[]{pos[0]+delta[0],pos[1]+delta[1]};
    if (!this.facingTowards(a.getCollisions()) && !a.checkCollisions(target)) this.pos = target;
  }
}