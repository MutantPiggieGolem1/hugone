package hugone;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.InteractableObject;
import hugone.util.Image;

public class Furniture implements InteractableObject {
  public final String id;
  protected Area area;
  private Image image;
  private String item;
  private Dialogues dialogue;
  private Rectangle pos;
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
    this.image = new Image(data.getString("image"));
    this.image.scaleToWidth(dimension.getInt(0));
    this.pos = new Rectangle(location.getInt(0), location.getInt(1), dimension.getInt(0), this.image.getHeight());
  }

  public boolean collidesWith(Rectangle r) {
    return this.collide && this.pos.intersects(r);
  }

  public void onInteraction() {
    if (!this.interacted && item != null)
      App.player.addItem(item);
    this.interacted = true;
    this.area.setDialogue(this.dialogue);
  }

  public void render(Graphics2D g) {
    this.image.draw(this.pos.x, this.pos.y, g);
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