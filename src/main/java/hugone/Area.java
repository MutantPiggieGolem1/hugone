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

class Area implements Feature {
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
    this.dialogue.init();
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

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {App.player.reccieveKeyPress(e, p);}

  @Override
  public void close() {
    if (this.music != null) this.music.reset();
  }

  @Override
  public String getNext() {
    return App.story.data.getJSONObject("scenes").getJSONObject(this.id).getString("next");
  }
}

class Furniture implements InteractableObject {
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