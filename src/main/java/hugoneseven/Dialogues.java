package hugoneseven;

import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.util.Audio;
import hugoneseven.util.Image;

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
    for (int i = 0; i<lines.length(); i++) {
      try {
        dialogues.add(new Dialogue(lines.getJSONObject(i)));
      } catch (JSONException e) {
        System.out.println("!WARNING! Subdialogue failed to load for "+id+".");
      }
    };
    this.index = 0;
  }

  public void setParent(Area p) {
    this.area = p;
  }

  public boolean update() {
    return this.index >= this.dialogues.size();
  }

  public void render(Graphics2D g) {
    Dialogue dialogue = this.dialogues.get(this.index);
    if (dialogue.update()) { // advance the dialogue
      this.index++;
      if (this.update()) {return;}; // make sure its not overflowing
      dialogue = this.dialogues.get(this.index);
    };
    dialogue.render(g);
  }
}

class Dialogue {
  Character character;
  Emotion emotion;
  String line;
  Audio audio;
  Image textbox;

  public Dialogue(JSONObject data) throws JSONException {
    this.character = App.story.getCharacter(data.getString("character"));
    this.emotion = Emotion.valueOf(data.getString("emotion"));
    this.line = data.getString("line");
    try {
      this.audio = new Audio(data.getString("audio"));
    } catch (Exception e) {
      System.out.println("!WARNING! Audio file failed to load for dialogue. "+e.getMessage());
    }
    try {
      this.textbox = new Image(data.getString("textbox"));
    } catch (Exception e) {
      System.out.println("!WARNING! Textbox image failed to load for dialogue. "+e.getMessage());
    }
  }

  public boolean update() {
    return this.audio.isPlayed();
  }

  public void render(Graphics2D g) {
    if (!this.audio.isPlaying() && !this.audio.isPlayed()) {this.audio.play();};
    this.textbox.draw(20,-App.frameheight+20,g);
    g.drawString(line,-App.frameheight+30,30);
    this.character.render(this.emotion,g);
  }
}