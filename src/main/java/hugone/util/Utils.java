package hugone.util;

import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;

import hugone.Constants.Direction;

public class Utils {
  public static final Image NULLIMG = new Image("null.png");
  public static final Image ARROWTAILIMAGE = new Image("ARROW_TAIL.png");
  public static final java.awt.Image ICONIMG = new Image("icon.png").getImage();
  private static long prevns;

  public static <T> String toString(T[] arr) {
    String o = arr[0].toString();
    for (T i : Arrays.copyOfRange(arr, 1, arr.length)) {
      o += "," + i.toString();
    }
    return o;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(ArrayList<T> arr) {
    return (T[]) arr.toArray();
  }

  @SuppressWarnings("unchecked")
  public static <T> ArrayList<T> toArray(JSONArray jsonarr) throws JSONException {
    ArrayList<T> out = new ArrayList<T>();
    for (int i = 0; i < jsonarr.length(); i++) {
      out.add((T) jsonarr.get(i));
    }
    return out;
  }

  public static String readFile(String filepath) throws FileNotFoundException {
    String out = "";
    File file = new File(filepath);
    Scanner myReader = new Scanner(file);
    while (myReader.hasNextLine()) {
      out = out.concat(myReader.nextLine().trim()); // removing newlines & trailing spaces
    }
    myReader.close();
    return out;
  }

  public static final HashMap<Integer, Direction> dirkeys = new HashMap<Integer, Direction>();
  static {
    dirkeys.put(KeyEvent.VK_W, Direction.UP);
    dirkeys.put(KeyEvent.VK_S, Direction.DOWN);
    dirkeys.put(KeyEvent.VK_A, Direction.LEFT);
    dirkeys.put(KeyEvent.VK_D, Direction.RIGHT);
  }
  public static final HashMap<Direction,Integer> keydirs = new HashMap<Direction,Integer>();
  static {
    keydirs.put(Direction.UP,   KeyEvent.VK_W);
    keydirs.put(Direction.DOWN, KeyEvent.VK_S);
    keydirs.put(Direction.LEFT, KeyEvent.VK_A);
    keydirs.put(Direction.RIGHT,KeyEvent.VK_D);
    keydirs.put(Direction.NONE, KeyEvent.VK_ESCAPE);
  }
  public static HashMap<Direction, Image> arrowimages = new HashMap<Direction, Image>();
  static {
      arrowimages.put(Direction.LEFT , new Image("ARROW_LEFT.png" ));
      arrowimages.put(Direction.RIGHT, new Image("ARROW_RIGHT.png"));
      arrowimages.put(Direction.UP   , new Image("ARROW_UP.png"   ));
      arrowimages.put(Direction.DOWN , new Image("ARROW_DOWN.png" ));
      arrowimages.put(Direction.NONE , new Image("ARROW_NONE.png" ));
  }
  public static HashMap<Direction, Image> longarrowimages = new HashMap<Direction, Image>();
  static {
      longarrowimages.put(Direction.LEFT , new Image("ARROW_LEFT_HOLD.png" ));
      longarrowimages.put(Direction.RIGHT, new Image("ARROW_RIGHT_HOLD.png"));
      longarrowimages.put(Direction.UP   , new Image("ARROW_UP_HOLD.png"   ));
      longarrowimages.put(Direction.DOWN , new Image("ARROW_DOWN_HOLD.png" ));
      longarrowimages.put(Direction.NONE , new Image("ARROW_NONE_HOLD.png" ));
  }
  public static final HashMap<Direction,Integer> dirtoint = new HashMap<Direction,Integer>();
  static {
    dirtoint.put(Direction.NONE ,0);
    dirtoint.put(Direction.LEFT ,0);
    dirtoint.put(Direction.UP   ,1);
    dirtoint.put(Direction.DOWN ,2);
    dirtoint.put(Direction.RIGHT,3);
  }

  public static Rectangle getChange(Direction dir) {
    return getChange(new Rectangle(), dir, 10);
  };
  public static Rectangle getChange(Rectangle r, Direction dir, int d) {
    r = (Rectangle)r.clone();
    switch (dir) {
      case UP:
        r.translate(0,-d);
        break;
      case DOWN:
        r.translate(0,d);
        break;
      case LEFT:
        r.translate(-d,0);
        break;
      case RIGHT:
        r.translate(d,0);
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
    return (int)Math.floor(1e9/(now-pre));
  }

  public static int clamp(int v, int h, int l) {
    return Math.min(Math.max(v,l),h);
  }
}