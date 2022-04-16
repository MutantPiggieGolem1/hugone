package hugoneseven;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.Constants.Emotion;
import hugoneseven.Constants.Feature;
import hugoneseven.Constants.KeyPress;
import hugoneseven.util.Audio;
import hugoneseven.util.Image;
import hugoneseven.util.Utils;

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
      System.out.println("!WARNING! Audio file failed to load for dialogue. " + e.getMessage());
    }
    try {
      Image img = new Image(data.getString("textbox"));
      // img.scaleToSize(App.f.getWidth());
      this.textbox = img;
    } catch (Exception e) {
      System.out.println("!WARNING! Textbox image failed to load for dialogue. " + e.getMessage());
    }
  }

  public boolean update() {
    return this.audio.isPlayed();
  }

  public void render(Graphics2D g) {
    if (!this.audio.isPlaying() && !this.audio.isPlayed()) {
      this.audio.play();
    }

    this.character.render(this.emotion, g);
    this.textbox.draw(20, App.f.getHeight() - 150, g);
    g.drawString(line, 30, App.f.getHeight() - 100);
  }
}