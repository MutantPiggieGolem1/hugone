package hugone;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.InteractableObject;
import hugone.util.Image;
import hugone.util.Utils;

public class Furniture implements InteractableObject {
  public final String id;
  protected Area area;
  private Image image;
  private String item;
  private Dialogues dialogue;
  private Integer[] location;
  private Integer[] dimensions;
  private Rectangle rect;
  private boolean collide;
  private boolean interacted = false;

  public Furniture(JSONObject data, Area area) throws JSONException {
    this.area = area;

    this.id = data.has("objectid") ? data.getString("objectid") : null;
    this.item = data.has("item") ? data.getString("item") : null;
    this.collide = data.has("collide") ? data.getBoolean("collide") : true;
    if (data.has("dialogue")) {
      this.dialogue = App.story.getDialogue(data.getString("dialogue"));
      this.dialogue.setParent(area);
    }

    JSONArray location = data.getJSONArray("location"); // top left
    JSONArray dimension = data.getJSONArray("dimensions");
    this.location = Utils.toArray(location).toArray(new Integer[location.length()]);
    this.dimensions = Utils.toArray(dimension).toArray(new Integer[dimension.length()]);
    this.image = new Image(data.getString("image"));
    this.image.scaleToWidth(this.dimensions[0]);
    this.dimensions[1] = this.image.getHeight(); // auto rescale height bound
    this.rect = new Rectangle(this.location[0], this.location[1], this.dimensions[0], this.dimensions[1]);
  }

  public boolean collidesWith(Rectangle r) {
    return this.collide && this.rect.intersects(r);
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

class Exit extends Furniture {
  public Exit(JSONObject data, Area area) {
    super(data,area);
  }

  @Override
  public void onInteraction() {
    if (App.player.inventory.containsAll(this.area.getFind())) {
      this.area.exit();
    } else {
      this.area.setDialogue(App.story.getDialogue("cant_exit"));
    }
  }
}