package hugoneseven;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.util.Image;
import hugoneseven.util.Utils;

public class Furniture implements InteractableObject {
  public final String id;
  private Area area;
  private Image image;
  private String item;
  private Dialogues dialogue;
  private Integer[] location;
  private Integer[] dimensions;
  private HashSet<List<Integer>> allcoords = new HashSet<List<Integer>>();
  private boolean interacted = false;

  public Furniture(JSONObject data, Area area) throws JSONException {
    this.id = data.getString("objectid");
    this.item = data.getString("item");
    this.area = area;
    this.dialogue = App.story.getDialogue(data.getString("dialogue"));
    this.dialogue.setParent(area);

    JSONArray location= data.getJSONArray("location");
    JSONArray dimension= data.getJSONArray("dimensions");
    for (int x = location.getInt(0); x <= location.getInt(0)+dimension.getInt(0); x++) {
      for (int y = location.getInt(1); y >= location.getInt(1)-dimension.getInt(1); y--) {
        this.allcoords.add(Arrays.asList(x,y));
      }
    }
    this.dimensions = Utils.toArray(Utils.toArray(dimension));
    this.location   = Utils.toArray(Utils.toArray(location));

    this.image = new Image(data.getString("image"));
    double scale = (double)this.dimensions[0]/(double)this.image.getWidth();
    this.image.scale(scale);
    this.dimensions[1] = (int)Math.ceil(scale*this.image.getHeight()); // auto rescale height bound
  }

  public HashSet<List<Integer>> getCoords() {
    return this.allcoords; // top left, bottom right 
  }

  public void onInteraction() {
    if (this.interacted) return;
    this.interacted = true;
    if (item != null) {App.player.addItem(item);};
    this.area.setDialogue(this.dialogue); // tell area to render our dialogue
  }

  public void render(Graphics2D g){
    System.out.println("Drawing Furniture @"+Utils.toString(this.location));
    this.image.draw(this.location[0],this.location[1],g);
  }
}