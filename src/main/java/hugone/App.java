package hugone;

import java.awt.Graphics2D;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JFrame;

import hugone.Constants.Feature;
import hugone.util.Utils;

class App {
  public static Story story;
  public static Player player;

  public static final JFrame f = new JFrame("Pseufaux 1 - Hugone [BETA]");

  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  public static void main(String[] args) {
    try {
      story = new Story("story.json");
      story.init();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Couldn't initalize story.");
    }
    player = story.player;

    DrawingCanvas dc = new DrawingCanvas(f);
    f.setVisible(true);
    f.add(dc);
    f.addKeyListener(dc);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
    f.setFocusable(true);
    f.setIconImage(Utils.ICONIMG);
    f.setResizable(false);    

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
          case CARD:
            cur.update();
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
          case DEATH:
            break;
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
    }, 0, (long) (1000 / Constants.TPS), java.util.concurrent.TimeUnit.MILLISECONDS);
  
    f.setVisible(true);
    f.requestFocus();
    dc.startDraw();
    f.setEnabled(true);
  }

  public static void render(Graphics2D g) {
    story.getCurrent().render(g);
  }

  public static void postRender(Graphics2D g) {
    g.drawString(String.valueOf(Utils.fps()), 0, 0);
  }
}