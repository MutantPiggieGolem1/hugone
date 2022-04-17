package hugoneseven;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.Constants.Feature;
import hugoneseven.Constants.GameState;
import hugoneseven.util.Utils;

/*
 * Location coords are in TOP LEFT
*/

class App {
  public static HashMap<String,Object> shit = new HashMap<String,Object>();

  public static Story story;
  public static Player player;
  public static GameState gamestate;

  public static final int framewidth = 1366;
  public static final int frameheight = 768;

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

    f.setIconImage(Utils.ICONIMG);
    f.setSize(framewidth, frameheight);
    DrawingCanvas dc = new DrawingCanvas(framewidth, frameheight);
    dc.requestFocus();
    dc.setBackground(Color.GRAY);
    dc.addMouseListener(new javax.swing.event.MouseInputAdapter() {
      @Override
      public void mousePressed(java.awt.event.MouseEvent e) {
        if (story.currentState().equals(GameState.EXPLORATION)) {
          Area a = (Area)story.getCurrent();
          System.out.println("(" + e.getX() + "," + e.getY() + ") - [" + f.getWidth() + "," + f.getHeight() + "] {" + a.checkCollisions(new java.awt.Point(e.getX(),e.getY())) + "}");
        }
      }
    });
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
          if (cur.update())
            story.next(); // on completion, advance story
          cur = story.getCurrent();
          switch (story.currentState()) {
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
          System.exit(1);
        }
      }
    }, 0, (long) (1000 / Constants.TPS), TimeUnit.MILLISECONDS);

    // Draw Loop
    while (true) {
      if (!f.isShowing())
        continue;// dont render in background
      if (System.currentTimeMillis() - dc.prevtime >= 1000.0 / Constants.FPS) {
        dc.repaint();
        dc.prevtime = System.currentTimeMillis();
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
      case BATTLE:
        cur.render(g);
      break;
      default:
        System.out.println("!WARNING! Unrecognized GameState!");
    }
  }

  public static void postRender(Graphics2D g) {

  }
}