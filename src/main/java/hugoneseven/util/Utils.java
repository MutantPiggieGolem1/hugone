package hugoneseven.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import hugoneseven.enums.Direction;

public class Utils {
  public static final String RESOURCEDIR = "./resources/";
  public static final double FPS = 60.0;
  public static final double TPS = 20.0;

  public static final Image NULLIMG = new Image("null.png");

  public static final HashMap<Integer,Direction> dirkeys = new HashMap<Integer,Direction>();
  static {dirkeys.put(KeyEvent.VK_W,Direction.UP);dirkeys.put(KeyEvent.VK_S,Direction.DOWN);dirkeys.put(KeyEvent.VK_A,Direction.LEFT);dirkeys.put(KeyEvent.VK_D,Direction.RIGHT);}

  public static void drawPoint(Graphics2D g, int x, int y) {
    drawPoint(g, x, y, Color.RED);
  }
  public static void drawPoint(Graphics2D g, int x, int y, Color c) {
    Color b4 = g.getColor();
    g.setColor(c);
    g.drawLine(x, y, x, y);
    g.setColor(b4);
  }

  public static <T> String toString(T[] arr) {
    String o = arr[0].toString();
    for (T i : Arrays.copyOfRange(arr,1,arr.length)) {
      o += ","+i.toString();
    }
    return o;
  }
  public static Integer[] toArray(ArrayList<Integer> arr) {
    Integer[] out = new Integer[arr.size()];
    for (int i = 0; i<arr.size(); i++) {
      out[i] = arr.get(i);
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  public static <T> ArrayList<T> toArray(JSONArray jsonarr) throws JSONException {
    ArrayList<T> out = new ArrayList<T>();
    for (int i = 0; i < jsonarr.length(); i++) {
      out.add((T)jsonarr.get(i));
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
  
  public static int[] getChange(Direction dir) {
    int[] pos = new int[]{0,0};
    switch (dir) {
      case UP:
        pos[1] -= 10;
      break;
      case DOWN:
        pos[1] += 10;
      break;
      case LEFT:
        pos[0] -= 10;
      break;
      case RIGHT:
        pos[0] += 10;
      break;
    }
    return pos;
  }
  public static BufferedImage getImage(String filepath) throws IOException {
    return ImageIO.read(new File(filepath));
  }
}