package hugoneseven;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.Constants.InteractableObject;
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

    JSONArray location = data.getJSONArray("location"); // top left
    JSONArray dimension = data.getJSONArray("dimensions");
    for (int x = location.getInt(0); x <= location.getInt(0) + dimension.getInt(0); x++) {
      for (int y = location.getInt(1); y <= location.getInt(1) + dimension.getInt(1); y++) {
        this.allcoords.add(Arrays.asList(x, y));
      }
    }
    this.dimensions = Utils.toArray(dimension).toArray(new Integer[dimension.length()]);
    this.location = Utils.toArray(location).toArray(new Integer[location.length()]);;

    this.image = new Image(data.getString("image"));
    double scale = this.dimensions[0] / this.image.getWidth();
    this.image.setScale(scale);
    this.dimensions[1] = (int) Math.ceil(scale * this.image.getHeight()); // auto rescale height bound
  }

  public HashSet<List<Integer>> getCoords() {
    return this.allcoords;
  }

  public void onInteraction() {
    if (!this.interacted && item != null)
      App.player.addItem(item);
    this.interacted = true;
    this.area.setDialogue(this.dialogue);
  }

  public void render(Graphics2D g) {
    g.drawRect(this.location[0], this.location[1], this.location[0] + this.dimensions[0],
        this.location[1] + this.dimensions[1]);
    this.image.draw(this.location[0], this.location[1], g);
  }
}