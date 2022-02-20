package hugoneseven;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Graphics2D;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import hugoneseven.Constants.BattleState;
import hugoneseven.Constants.Direction;
import hugoneseven.Constants.Emotion;
import hugoneseven.Constants.Feature;
import hugoneseven.util.Audio;
import hugoneseven.util.Image;
import hugoneseven.util.Utils;

@SuppressWarnings("unused")
class Battle implements Feature {
  private String id;
  private Character enemy;

  private Audio losetrack;

  private BattleState state;
  private Dialogues dialogue;

  private Iterator<Note> notes;
  private ArrayList<Note> renderedNotes = new ArrayList<Note>();
  private Integer notedamage;
  private Image background;

  public Battle(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("battles").getJSONObject(id);

    System.out.println(data.toString());

    this.id = id;
    this.enemy = App.story.getCharacter(data.getString("enemy"));
    this.notedamage = data.getInt("damage");
    try {
      this.losetrack = new Audio("faintcourage.wav");
      this.background = new Image(Constants.RESOURCEDIR+data.getString("background"));
    } catch (Exception e) {
      System.out.println("!WARNING! Could not load battle data.");
    }

    JSONArray notes = null;
    try {
      notes = new JSONArray(Utils.readFile(Constants.RESOURCEDIR + data.getString("notes"))); // load in the notes
    } catch (FileNotFoundException e1) {
      System.out.println("!WARNING! Could not load battle notes.");
    }
    ArrayList<Note> tn = new ArrayList<Note>();
    for (int i = 0; i < notes.length(); i++) {
      String strnote = notes.getString(i);
      Integer notelen = 1;
      try {
        notelen = Integer.parseInt(strnote.substring(strnote.length() - 1));
        strnote = strnote.substring(0,strnote.length() - 1);
        if (notelen < 1) notelen = 1;
      } catch (NumberFormatException e) {}
      switch (strnote) {
        case "UP":
        case "DOWN":
        case "LEFT":
        case "RIGHT":
          Direction notedir = Direction.valueOf(strnote);
          tn.add(notelen > 1 ? new HoldNote(notedir, notelen) : new Note(notedir));
          break;
        case "DEATH":
          tn.add(new DeathNote());
          break;
        case "NONE":
          for (int o = 0; o < notelen; o++) {
            tn.add(null);
          }
          break;
        default:
          System.out.println("!WARNING! Unrecognized battle note type. "+strnote);
      }
    }
    this.notes = tn.iterator();
  }

  public boolean update() {
    return App.player.health <= 0 || !this.notes.hasNext(); // player is dead or song is done
  }

  public void beat() {
    if (this.notes.hasNext()) { // throw in the next beat
      Note n = this.notes.next();

      this.renderedNotes.add(n);
      n.spawn();
    }

    ArrayList<Note> dq = new ArrayList<Note>();
    for (Note n : this.renderedNotes) { // stop attempting to draw notes that are gone
      if (n.update()) {dq.add(n);continue;}
      n.move();
    }
    this.renderedNotes.removeAll(dq);
  }

  public void miss() { // specifically for notes.
    App.player.health -= this.notedamage;
    // render effects here?
  }

  public void render(Graphics2D g) {
    switch (this.state) {
      case DIALOGUE:
        this.dialogue.render(g);
      case FIGHT:
        App.player.render(Emotion.BOP, g); // probably change
        App.player.renderHealth(g);
        this.enemy.render(Emotion.MAD, g);
        
        for (Note n : this.renderedNotes) { // draw all the notes
          n.render(g);
        }

        this.background.draw(0,0,g); // draw in the image bg
        break;
      case LOSE:
        this.losetrack.play();
        if (this.losetrack.isPlayed()) {
          // death happens here
        }
        break;
      case WIN:
        // uh idk
        break;
    }
  }
}

class Note { // TODO: Implement note
  Direction direction;
  Image image;

  public Note(Direction dir) {
    this.direction = dir;
    this.image = Constants.getArrowImage(dir);
  }

  public void move() {
  }

  public void render(Graphics2D g) {
  }

  public void spawn() {
  }

  public boolean update() {
    return false;
  }
}

class HoldNote extends Note {
  Integer holdtime;

  public HoldNote(Direction dir, int leng) {
    super(dir);
    this.holdtime = leng;
  }
}

class DeathNote extends Note {
  public DeathNote() {
    super(Direction.NONE);
  }
}