package hugoneseven.util;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;

import hugoneseven.Constants.Direction;

public class Utils {
  public static final Image NULLIMG = new Image("null.png");

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
  }
  public static HashMap<Direction, Image> arrowimages = new HashMap<Direction, Image>();
  static {
      arrowimages.put(Direction.LEFT , new Image("ARROW_LEFT.png" ));
      arrowimages.put(Direction.RIGHT, new Image("ARROW_RIGHT.png"));
      arrowimages.put(Direction.UP   , new Image("ARROW_UP.png"   ));
      arrowimages.put(Direction.DOWN , new Image("ARROW_DOWN.png" ));
      arrowimages.put(Direction.NONE , new Image("ARROW_NONE.png" ));
  }
  public static final HashMap<Direction,Integer> dirtoint = new HashMap<Direction,Integer>();
  static {
    dirtoint.put(Direction.NONE ,0);
    dirtoint.put(Direction.LEFT ,0);
    dirtoint.put(Direction.UP   ,1);
    dirtoint.put(Direction.DOWN ,2);
    dirtoint.put(Direction.RIGHT,3);
  }

  public static int[] getChange(Direction dir) {
    return getChange(dir, 10);
  };

  public static int[] getChange(Direction dir, int d) {
    int[] pos = new int[] { 0, 0 };
    switch (dir) {
      case UP:
        pos[1] -= d;
        break;
      case DOWN:
        pos[1] += d;
        break;
      case LEFT:
        pos[0] -= d;
        break;
      case RIGHT:
        pos[0] += d;
        break;
      case NONE:
      default:
        break;
    }
    return pos;
  }
}