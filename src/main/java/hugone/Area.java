package hugone;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.Feature;
import hugone.Constants.InteractableObject;
import hugone.Constants.KeyPress;
import hugone.Constants.RenderState;
import hugone.util.Audio;
import hugone.util.Image;
import hugone.util.Utils;

@SuppressWarnings("unused")
class Area implements Feature { // TODO: Allow audio overlap
  private final String id;
  private final Image image;
  private ArrayList<Furniture> furniture = new ArrayList<Furniture>();
  private RenderState renderstate;
  private ArrayList<String> find;
  private Dialogues dialogue;
  private Audio music;
  private Rectangle dimensions;
  private Point startloc;
  private boolean exit = false;

  public Area(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("areas").getJSONObject(id);
    JSONArray dims = data.getJSONArray("dimensions");
    JSONArray start = data.getJSONArray("startlocation");

    this.id = id;
    this.image = new Image(data.getString("image"));
    this.dimensions = new Rectangle(dims.getJSONArray(0).getInt(0),dims.getJSONArray(0).getInt(1),dims.getJSONArray(1).getInt(0),dims.getJSONArray(1).getInt(1));
    this.startloc = new Point(start.getInt(0), start.getInt(1));;
    this.image.scaleToWidth(this.dimensions.width);
    // misc vars
    this.find = Utils.toArray(data.getJSONArray("find"));

    try {
      this.music = new Audio(data.getString("music"));
    } catch (Exception e) {
      System.out.println("!WARNING! Could not load area data.\n"+e.toString());
      System.exit(1);
    }

    // setup furniture
    JSONArray furnituredata = data.getJSONArray("furniture");
    for (int i = 0; i < furnituredata.length(); i++) {
      JSONObject furn = furnituredata.getJSONObject(i);
      Furniture furni = new Furniture(furn, this);
      this.furniture.add(furni);
    }

    JSONObject exitdata = data.getJSONObject("exit");
    this.furniture.add(new Exit(exitdata, this));
  }

  public void init() {
    App.player.teleport(this.startloc);
    this.renderstate = RenderState.DEFAULT;
    if (this.music!=null) this.music.play();
  }

  public ArrayList<Furniture> getFurniture() {
    return this.furniture;
  }

  public ArrayList<String> getFind() {
    return this.find;
  }

  public void exit() {
    this.exit = true;
  }

  public boolean update() {
    if (this.music!=null) {
      if (this.music.isPlaying()) {
        if (this.renderingDialogue()) this.music.pause();
      } else {
        if (!this.renderingDialogue()) this.music.play();
      }
    }
    return this.exit && this.renderstate.equals(RenderState.DEFAULT); // all required items, and not rendering dialogue
  }

  public void setDialogue(Dialogues dg) {
    this.dialogue = dg;
    this.renderstate = RenderState.DIALOGUE;
    App.player.stopMovement();
  }

  public void render(Graphics2D g) {
    this.image.draw(0, 0, g);
    for (Furniture furniture : this.furniture) {
      furniture.render(g);
    }
    App.player.render(g);
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
    if (!p.spaceDown()) return;
    this.furniture.forEach((InteractableObject f) -> {
      if (f.collidesWith(p.facingTowards()))
        f.onInteraction();
    });
  }

  public boolean collideFurniture(Rectangle r) {
    for (Furniture f : this.furniture) {
      if (f.collidesWith(r)) {
        return true;
      }
    }
    return false;
  }

  public boolean collidesWith(Rectangle r) { // true if collision
    return !this.dimensions.contains(r) || this.collideFurniture(r);
  }

  public boolean renderingDialogue() {
    return this.renderstate.equals(RenderState.DIALOGUE);
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {}

  @Override
  public void close() {
    if (this.music != null) this.music.reset();
    
  }
}