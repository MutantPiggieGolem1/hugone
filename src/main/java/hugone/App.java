package hugone;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.Feature;
import hugone.Constants.GameState;
import hugone.util.Utils;

/*
 * Location coords are in TOP LEFT
*/

class App {
  public static HashMap<String,Object> shit = new HashMap<String,Object>(); // stores temporary vars to be relayed to main file

  public static Story story;
  public static Player player;
  public static GameState gamestate;
  public static final int prevtime = 0;

  public static final JFrame f = new JFrame("Hugone - Alpha Version");
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  public static void main(String[] args) {
    try {
      story = new Story(new JSONObject(Utils.readFile(Constants.RESOURCEDIR + "story.json")));
    } catch (Exception e) {
      throw new RuntimeException("Couldn't load story json data.\nDetais: " + e.toString());
    }
    try {
      story.init();
    } catch (JSONException e) {
      throw new RuntimeException("Couldn't initalize story.\nDetais: " + e.toString());
    }
    player = story.player;
    gamestate = story.currentState();

    DrawingCanvas dc = new DrawingCanvas(f);
    f.setIconImage(Utils.ICONIMG);
    f.setSize(Constants.SCREENDIMS);
    f.setFocusable(true);
    f.add(dc);
    f.addKeyListener(player);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //f.addMouseListener(l); for menus

    // Game Loop begins
    executor.scheduleAtFixedRate(() -> {
      try {
        Feature cur = story.getCurrent();
        if (cur.update())
          story.next(); // on completion, advance story
        cur = story.getCurrent();
        switch (story.currentState()) {
          case MENU:
            Menu m = (Menu) cur;
            m.update();
            break;
          case CUTSCENE:
            break;
          case EXPLORATION:
            Area a = (Area) cur;
            if(!a.renderingDialogue()) {
              player.moveLoop();
              a.checkInteracts(player);
            }
            break;
          case BATTLE:
            Battle b = (Battle) cur;
            b.beat();
            break;
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
    }, 0, (long) (1000 / Constants.TPS), TimeUnit.MILLISECONDS);

    f.setVisible(true);
    dc.requestFocus();
    dc.startDraw();
  }

  public static void render(Graphics2D g) {
    Feature cur = story.getCurrent();
    cur.render(g);
    switch (story.currentState()) {
      case MENU:
      break;
      case CUTSCENE:
      break;
      case EXPLORATION:
        player.render(g);
      break;
      case BATTLE:
      break;
      default:
        System.out.println("!WARNING! Unrecognized GameState!");
    }
  }

  public static void postRender(Graphics2D g) {
    g.drawString(String.valueOf(Utils.fps()), 0, 0);
  }
}