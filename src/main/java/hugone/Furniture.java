package hugone;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.InteractableObject;
import hugone.util.Image;
import hugone.util.Utils;

public class Furniture implements InteractableObject {
  public final String id;
  private Area area;
  private Image image;
  private String item;
  private Dialogues dialogue;
  private Integer[] location;
  private Integer[] dimensions;
  private Rectangle rect;
  private boolean collide;
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

    this.rect = new Rectangle(this.location[0], this.location[1], this.dimensions[0], this.dimensions[1]);
    this.collide = data.getBoolean("collide");
  }

  public boolean collidesWith(Point p) {
    return this.collide && this.rect.contains(p);
  }

  public void onInteraction() {
    if (!this.interacted && item != null)
      App.player.addItem(item);
    this.interacted = true;
    this.area.setDialogue(this.dialogue);
  }

  public void render(Graphics2D g) {
    this.image.draw(this.location[0], this.location[1], g);
  }
}