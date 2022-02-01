package hugoneseven;

import org.json.*;

import hugoneseven.util.Image;
import hugoneseven.util.Utils;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("unused")
class Area implements Feature {
  private String id;
  private Image image;
  private ArrayList<Furniture> furniture = new ArrayList<Furniture>();
  public RenderState renderstate;
  private ArrayList<String> find;
  private Dialogues dialogue;
  private HashSet<List<Integer>> collisions = new HashSet<List<Integer>>();
  private int[] dimensions = new int[2];

  public Area(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("areas").getJSONObject(id);
    JSONArray dims = data.getJSONArray("dimensions");

    this.id = id;
    this.image = new Image(data.getString("image"));
    this.dimensions[0] = dims.getInt(0);
    this.dimensions[1] = dims.getInt(1);

    // setup furniture
    JSONArray furnituredata = data.getJSONArray("furniture");
    for (int i = 0; i<furnituredata.length(); i++) {
      JSONObject furn = furnituredata.getJSONObject(i);
      Furniture furni = new Furniture(furn,this);
      this.furniture.add(furni);
      this.collisions.addAll(furni.getCoords());
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
    switch (this.renderstate) {
      case DIALOGUE:
        if (this.dialogue.update() || App.player.spaceDown()) {
          this.renderstate = RenderState.DEFAULT; // once done / skipped, return to normal state
          this.dialogue.reset();
        } else {
          this.dialogue.render(g);
        }
      case DEFAULT:
      default:
        this.image.draw(0,0,g); // may have to be fixed later
        for (Furniture furniture : this.furniture) {
          furniture.render(g);
        }
    }
  }

  public HashSet<List<Integer>> getCollisions() {
    return this.collisions;
  }
  public boolean checkCollisions(int[] coords) {
    return this.checkCollisions(Arrays.asList(coords[0],coords[1]));
  }
  public boolean checkCollisions(List<Integer> coords) {
    return false;
    //return (coords.get(0) < 0 || coords.get(1) > 0 || coords.get(0) > this.dimensions[0] || coords.get(1) < -this.dimensions[1]) || this.collisions.contains(coords);
  } // out of bounds check + furniture check for redundancy - its O(1) anywways
}