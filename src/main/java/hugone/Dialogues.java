package hugone;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.Emotion;
import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Audio;
import hugone.util.Image;

@SuppressWarnings("unused")
class Dialogues implements Feature {
  public String id;
  private ArrayList<Dialogue> dialogues = new ArrayList<Dialogue>();
  private int index;
  private Area area;

  public Dialogues(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("dialogue").getJSONObject(id);

    this.id = id;

    // load individual lines
    JSONArray lines = data.getJSONArray("lines");
    for (int i = 0; i < lines.length(); i++) {
      try {
        this.dialogues.add(new Dialogue(lines.getJSONObject(i)));
      } catch (Exception e) {
        System.out.println("!WARNING! Subdialogue failed to load for " + id + ".");
      }
    }
    this.index = 0;
  }

  public void init() {
    
  }

  public void setParent(Area p) {
    this.area = p;
  }

  public void reset() {
    this.index = 0;
    for (Dialogue d : this.dialogues) {
      d.audio.reset();
    }
  }

  public boolean update() {
    return this.index >= this.dialogues.size();
  }

  public void render(Graphics2D g) {
    Dialogue dialogue = this.dialogues.get(this.index);
    if (dialogue.update()) { // advance the dialogue
      this.index++;
      if (this.update()) {
        return;
      } // make sure its not overflowing
      dialogue = this.dialogues.get(this.index);
    }

    dialogue.render(g);
  }

  @Override
  public void reccieveKeyPress(KeyEvent e, KeyPress p) {}

  @Override
  public void close() {
    this.reset();
  }

  @Override
  public String getNext() {
    return App.story.data.getJSONObject("scenes").getJSONObject(this.id).getString("next");
  }
}

class Dialogue {
  private Character character;
  private Emotion emotion;
  private String line;
  protected Audio audio;
  private Image textbox;

  public Dialogue(JSONObject data) throws JSONException {
    this.character = App.story.getCharacter(data.getString("character"));
    this.emotion = Emotion.valueOf(data.getString("emotion"));
    this.line = data.getString("line");
    try {
      this.audio = new Audio(data.getString("audio"));
    } catch (Exception e) {
      System.out.println("!WARNING! Audio failed to load for dialogue. " + e.getMessage());
    }
    this.textbox = new Image(data.getString("textbox")).scaleToWidth(hugone.util.Utils.WIDTH-50).stretchToHeight(200);
  }

  public boolean update() {
    return this.audio.isPlayed();
  }

  public void render(Graphics2D g) {
    if (!this.audio.isPlaying() && !this.audio.isPlayed()) {
      this.audio.play();
    }

    this.character.render(this.emotion, g);
    this.textbox.draw(20, App.f.getHeight() - 300, g);
    g.drawString(line, 50, App.f.getHeight() - 200);
  }
}