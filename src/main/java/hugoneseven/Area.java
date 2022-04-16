package hugoneseven;

import org.json.*;

import hugoneseven.Constants.Feature;
import hugoneseven.Constants.InteractableObject;
import hugoneseven.Constants.KeyPress;
import hugoneseven.Constants.RenderState;
import hugoneseven.util.Image;
import hugoneseven.util.Utils;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("unused")
class Area implements Feature {
  private final String id;
  private final Image image;
  private ArrayList<Furniture> furniture = new ArrayList<Furniture>();
  private RenderState renderstate;
  private ArrayList<String> find;
  private Dialogues dialogue;
  private int[] dimensions = new int[2];
  private int[] borders = new int[2];
  private int[] startloc = new int[2];

  public Area(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("areas").getJSONObject(id);
    JSONArray dims = data.getJSONArray("dimensions");
    JSONArray start = data.getJSONArray("startlocation");

    this.id = id;
    this.image = new Image(data.getString("image"));
    this.dimensions[0] = dims.getJSONArray(1).getInt(0);
    this.dimensions[1] = dims.getJSONArray(1).getInt(1);
    this.borders[0] = dims.getJSONArray(0).getInt(0);
    this.borders[1] = dims.getJSONArray(0).getInt(1);
    this.startloc[0] = start.getInt(0);
    this.startloc[1] = start.getInt(1);
    this.image.scaleToWidth(this.dimensions[0]);

    // setup furniture
    JSONArray furnituredata = data.getJSONArray("furniture");
    for (int i = 0; i < furnituredata.length(); i++) {
      JSONObject furn = furnituredata.getJSONObject(i);
      Furniture furni = new Furniture(furn, this);
      this.furniture.add(furni);
    }

    // misc vars
    this.find = Utils.toArray(data.getJSONArray("find"));
  }

  public void init() {
    App.player.teleport(this.startloc);
    this.renderstate = RenderState.DEFAULT;
  }

  public ArrayList<Furniture> getFurniture() {
    return this.furniture;
  }

  public boolean update() {
    return App.player.inventory.containsAll(find) && this.renderstate.equals(RenderState.DEFAULT); // all required items, and not rendering dialogue
  }

  public void setDialogue(Dialogues dg) {
    this.dialogue = dg;
    this.renderstate = RenderState.DIALOGUE;
  }

  public void render(Graphics2D g) {
    this.image.draw(0, 0, g);
    for (Furniture furniture : this.furniture) {
      furniture.render(g);
    }
    if (this.renderstate.equals(RenderState.DIALOGUE)) {
      if (this.dialogue.update() || App.player.spaceDown()) {
        this.renderstate = RenderState.DEFAULT; // once done / skipped, return to normal state
        this.dialogue.reset();
      } else {
        this.dialogue.render(g);
      }
    }
  }

  public void checkInteracts(Player p) {
    if (!p.spaceDown())
      return;
    this.furniture.forEach((InteractableObject f) -> {
      if (p.facingTowards(f.getCoords()))
        f.onInteraction();
    });
  }

  public boolean collideFurniture(List<Integer> coords) {
    for (Furniture f : this.furniture) {
      if (f.getCoords().contains(coords)) {
        return true;
      }
    }
    return false;
  }

  public boolean checkCollisions(int[] coords) {
    return this.checkCollisions(Arrays.asList(coords[0], coords[1]));
  }

  public boolean checkCollisions(List<Integer> coords) {
    return coords.get(0) < this.borders[0]   || coords.get(1) < this.borders[1]
        || coords.get(0) > this.dimensions[0]|| coords.get(1) > this.dimensions[1]
        || this.collideFurniture(coords);
  }

  public boolean renderingDialogue() {
    return this.renderstate.equals(RenderState.DIALOGUE);
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {}
}