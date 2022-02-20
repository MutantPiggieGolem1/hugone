package hugoneseven;

import org.json.*;

import hugoneseven.Constants.Feature;
import hugoneseven.Constants.InteractableObject;
import hugoneseven.Constants.RenderState;
import hugoneseven.util.Image;
import hugoneseven.util.Utils;
import java.awt.Graphics2D;
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

  public Area(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("areas").getJSONObject(id);
    JSONArray dims = data.getJSONArray("dimensions");

    this.id = id;
    this.image = new Image(data.getString("image"));
    this.dimensions[0] = dims.getInt(0);
    this.dimensions[1] = dims.getInt(1);
    this.image.setScale(this.dimensions[0] / this.image.getImage().getWidth());

    // setup furniture
    JSONArray furnituredata = data.getJSONArray("furniture");
    for (int i = 0; i < furnituredata.length(); i++) {
      JSONObject furn = furnituredata.getJSONObject(i);
      Furniture furni = new Furniture(furn, this);
      this.furniture.add(furni);
    }

    // misc vars
    this.find = Utils.toArray(data.getJSONArray("find"));
    this.renderstate = RenderState.DEFAULT;
  }

  public ArrayList<Furniture> getFurniture() {
    return this.furniture;
  }

  public boolean update() {
    return App.player.inventory.containsAll(find);
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

  private boolean collideFurniture(List<Integer> coords) {
    for (Furniture f : this.furniture) {
      if (f.getCoords().contains(coords)) {
        return true;
      }
      ;
    }
    return false;
  }

  public boolean checkCollisions(int[] coords) {
    return this.checkCollisions(Arrays.asList(coords[0], coords[1]));
  }

  public boolean checkCollisions(List<Integer> coords) {
    return (coords.get(0) < 0 || coords.get(1) < 0 || coords.get(0) > this.dimensions[0]
        || coords.get(1) > this.dimensions[1]) || this.collideFurniture(coords);
  }

  public boolean renderingDialogue() {
    return this.renderstate.equals(RenderState.DIALOGUE);
  }
}