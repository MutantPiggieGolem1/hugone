package hugone.util;

import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import hugone.Constants.Direction;

public class Utils {
  public static final java.awt.Image ICONIMG = new Image("icon.png").getImage();
  public static final int WIDTH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
  private static long prevns;

  @SuppressWarnings("unchecked")
  public static <T> Set<T> toSet(JSONArray jsonarr) {
    Set<T> out = new HashSet<>(jsonarr.length());
    for (Object item : jsonarr) out.add((T) item);
    return out;
  }

  public static final HashMap<Integer, Direction> dirkeys = new HashMap<Integer, Direction>();
  static {
    dirkeys.put(KeyEvent.VK_W, Direction.UP);
    dirkeys.put(KeyEvent.VK_S, Direction.DOWN);
    dirkeys.put(KeyEvent.VK_A, Direction.LEFT);
    dirkeys.put(KeyEvent.VK_D, Direction.RIGHT);
  }
  public static final HashMap<Direction, Integer> keydirs = new HashMap<Direction, Integer>();
  static {
    keydirs.put(Direction.UP, KeyEvent.VK_W);
    keydirs.put(Direction.DOWN, KeyEvent.VK_S);
    keydirs.put(Direction.LEFT, KeyEvent.VK_A);
    keydirs.put(Direction.RIGHT, KeyEvent.VK_D);
    keydirs.put(Direction.NONE, KeyEvent.VK_ESCAPE);
  }
  public static HashMap<Direction, Image> arrowimages = new HashMap<Direction, Image>();
  static {
    arrowimages.put(Direction.LEFT, new Image("ARROW_LEFT.png"));
    arrowimages.put(Direction.RIGHT, new Image("ARROW_RIGHT.png"));
    arrowimages.put(Direction.UP, new Image("ARROW_UP.png"));
    arrowimages.put(Direction.DOWN, new Image("ARROW_DOWN.png"));
    arrowimages.put(Direction.NONE, new Image("ARROW_NONE.png"));
  }
  public static Image arrowtailimage = new Image("ARROW_TAIL.png").scaleToWidth(hugone.Battle.getNoteSize()/2);
  public static final HashMap<Direction, Integer> dirtoint = new HashMap<Direction, Integer>();
  static {
    dirtoint.put(Direction.NONE, 0);
    dirtoint.put(Direction.LEFT, 0); 
    dirtoint.put(Direction.UP, 1);
    dirtoint.put(Direction.DOWN, 2);
    dirtoint.put(Direction.RIGHT, 3);
  }
  public static final HashMap<Integer,String> funcmap = new HashMap<Integer,String>();
  static {
    funcmap.put(0, "intro");
    funcmap.put(1, "settings");
    funcmap.put(2, "gallery");
  }

  public static Rectangle getChange(Direction dir) {
    return getChange(new Rectangle(), dir, 10);
  };

  public static Rectangle getChange(Rectangle r, Direction dir, int d) {
    r = (Rectangle) r.clone();
    switch (dir) {
      case UP:
        r.translate(0, -d);
        break;
      case DOWN:
        r.translate(0, d);
        break;
      case LEFT:
        r.translate(-d, 0);
        break;
      case RIGHT:
        r.translate(d, 0);
        break;
      case NONE:
      default:
        break;
    }
    return r;
  }

  public static int fps() {
    long now = System.nanoTime();
    long pre = prevns;
    prevns = now;
    return (int) Math.floor(1e9 / (now - pre));
  }

  public static int clamp(int v, int h, int l) {
    return Math.min(Math.max(v, l), h);
  }

  public static double linearDelta(double cur, double goal) {
    int spd = 5;
    if (Math.abs(cur - goal) < 2 * spd)
      return goal; // snap
    return cur > goal ? cur - spd : cur + spd;
  }

  public static double expoDelta(double cur, double goal) {
    double mul = 0.11;
    if (Math.abs(cur - goal) < mul * 50)
      return goal; // snap
    return (int) (cur + (goal - cur) * mul);
  }

  public static String readFile(String filepath) {
    try (InputStream s = new FileInputStream(filepath)) {
      return new String(s.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      System.out.println("!WARNING! File reading failed! "+filepath);
      return null;
    }
  }

  public static boolean writeFile(String filepath, String toWrite) {
    try (FileWriter writer = new FileWriter(filepath)) {
      writer.write(toWrite);
      writer.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}