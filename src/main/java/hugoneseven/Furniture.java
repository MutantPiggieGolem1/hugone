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
    this.location = Utils.toArray(location).toArray(new Integer[location.length()]);
    this.dimensions = Utils.toArray(dimension).toArray(new Integer[dimension.length()]);

    this.image = new Image(data.getString("image"));
    double scale = this.image.scaleToWidth(this.dimensions[0]);
    this.dimensions[1] = (int) Math.floor(scale * this.image.getHeight()); // auto rescale height bound

    if (!data.getBoolean("collide")) return;
    App.shit.put("furncolcount",0);
    for (int x = this.location[0]; x <= this.location[0] + this.dimensions[0]; x++) {
      for (int y = this.location[1]; y <= this.location[1] + this.dimensions[1]; y++) {
        App.shit.put("furncolcount",(Integer)App.shit.get("furncolcount")+1);
        if (App.shit.get("furncolcount").equals(1)) App.shit.put("furncoord1",Arrays.asList(x, y));
        else if (App.shit.get("furncolcount").equals(this.dimensions[0]*this.dimensions[1])) App.shit.put("furncoord2",Arrays.asList(x, y));
        this.allcoords.add(Arrays.asList(x, y));
      }
    }
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

  @SuppressWarnings("unchecked")
  public void render(Graphics2D g) {
    this.image.draw(this.location[0], this.location[1], g);
    g.drawRect(this.location[0], this.location[1], this.dimensions[0], this.dimensions[1]);
    List<Integer> c1 = (List<Integer>)App.shit.get("furncoord1");
    List<Integer> c2 = (List<Integer>)App.shit.get("furncoord2");
    g.drawLine(c1.get(0),c1.get(1), c2.get(0), c2.get(1));
  }
}