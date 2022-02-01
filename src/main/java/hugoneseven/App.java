package hugoneseven;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.util.Utils;

/*
 * Prevent audio overlap
 * Add Battles
 * Implement video

 * Location coords are in bottom LEFT
*/

enum GameState {
  CUTSCENE,
  EXPLORATION,
  DIALOGUE,
  BATTLE
}
enum Emotion {
  IDLE,
  HAPPY,
  EXCITED,
  SAD,
  MAD,
  BOP // for music battles
  // etc.
}
enum MoveState {
  STOP,
  MOVE1,
  MOVE2
}
enum RenderState {
  DEFAULT,
  DIALOGUE
}
enum BattleState {
  DIALOGUE, // enemy/player is saying something
  DEMO,     // enemy is doing preview (like fnf)
  FIGHT,    // player is hitting notes
  LOSE,     // <play death theme>, so sad
  WIN       // u win pog
}
interface InteractableObject {
  public abstract void onInteraction();
  //public void onInteraction(int count);
  public abstract HashSet<List<Integer>> getCoords();
}
interface Feature {
  public abstract boolean update(); // check for completion
  public abstract void render(Graphics2D g); // draw this feature
}


class App {
  public static Story story;
  public static Player player;
  public static GameState gamestate;

  public static final int framewidth = 1366;
  public static final int frameheight = 768;

  public static final JFrame f = new JFrame("Hugone Seven - Alpha v0.0.1b");
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  public static void main(String[] args) {
    try {
      story = new Story(new JSONObject(Utils.readFile(Utils.RESOURCEDIR+"story.json")));
    } catch (Exception e) {
      throw new RuntimeException("Couldn't load story json data.\nDetais: "+e.toString());
    }
    try {
      story.init();
    } catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException("Couldn't initalize story.\nDetais: "+e.toString());
    }
    player = story.player;
    gamestate = story.currentState();
     
    DrawingCanvas dc = new DrawingCanvas(framewidth,frameheight);
    f.setSize(framewidth,frameheight);
    dc.requestFocus();
    dc.setBackground(Color.GRAY);
    f.add(dc);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.addKeyListener(player);
    f.setVisible(true);

    // Game Loop begins
    executor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          Feature cur = story.getCurrent();
          if (cur.update()) story.next(); // on completion, advance story
          cur = story.getCurrent();
          if (story.currentState().equals(GameState.EXPLORATION) && !((Area)cur).renderingDialogue()) {
            player.moveLoop();
            if (player.spaceDown()) {
              ((Area)cur).getFurniture().forEach((InteractableObject f) -> {
                if (player.facingTowards(f.getCoords())) f.onInteraction();
              });
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, 0, (long)(1000/Utils.TPS), TimeUnit.MILLISECONDS);

    // Draw Loop
    while (true) {
      while (f.isShowing()) { // dont render in background
        if (System.currentTimeMillis()-dc.prevtime >= 1000.0/Utils.FPS) {
          dc.repaint();
          dc.prevtime = System.currentTimeMillis();
        }
      }
    }
  }

  public static void render(Graphics2D g) {
    Feature cur = story.getCurrent();
    switch (story.currentState()) {
      case CUTSCENE:
        cur.render(g);
      break;
      case EXPLORATION:
        cur.render(g);
        player.render(g);
      break;
      // case BATTLE:
      //   TODO: implement battles
      // break;
      default:
        System.out.println("!WARNING! Unrecognized GameState!");
    }
  }
}