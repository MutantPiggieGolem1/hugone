package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.BattleState;
import hugone.Constants.Direction;
import hugone.Constants.Emotion;
import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Audio;
import hugone.util.Image;
import hugone.util.Utils;
import hugone.util.Video;

@SuppressWarnings("unused")
class Battle implements Feature {
  protected static final int leftmargin = 400;
  protected static final int rightmargin = 100;
  protected static final int verticalmargin = 20;
  private String id;
  private Character enemy;

  private Audio losetrack;

  private BattleState state;

  private Cutscene introscene;
  private Image introcard;

  private Iterator<Note> notes;
  private ArrayList<Note> renderedNotes = new ArrayList<Note>();
  private Integer notedamage;
  private Image background;
  private Image overlay;
  private Image missimage;
  private Audio misssound;
  public int notespeed;
  public int bpm;
  private int beat;

  private long lasttime;
  private Emotion enemem = Emotion.BOP;
  private Audio song;

  public Battle(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("battles").getJSONObject(id);

    this.id = id;
    this.enemy = App.story.getCharacter(data.getString("enemy"));
    this.notedamage = data.getInt("damage");
    this.notespeed = data.getInt("speed");
    this.bpm = data.getInt("bpm");
    this.beat = 0;
    try {
      this.song = new Audio(data.getString("song"));
      this.losetrack = new Audio("faintcourage.wav");
      this.background = new Image(data.getString("background"));
      if (data.has("overlay")) {this.overlay = new Image(data.getString("overlay"));}
      this.misssound = new Audio(data.getString("misssound"));
      this.missimage = new Image(data.getString("missimage"));
      this.missimage.scaleToWidth(App.f.getWidth());
      this.introcard = new Image(data.getString("introcard"));
      this.introcard.scaleToWidth(App.f.getWidth());
      this.introscene = new Cutscene(new Video(Constants.RESOURCEDIR + data.getString("introscene"),App.f));
    } catch (Exception e) {
      System.out.println("!WARNING! Could not load battle data.\n"+e.toString());
      System.exit(1);
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
        strnote = strnote.substring(0, strnote.length() - 1);
        if (notelen < 1) notelen = 1;
      } catch (NumberFormatException e) {}
      switch (strnote) {
        case "UP":
        case "DOWN":
        case "LEFT":
        case "RIGHT":
          Direction notedir = Direction.valueOf(strnote);
          tn.add(notelen > 1 ? new HoldNote(this, notedir, notelen) : new Note(this, notedir));
          break;
        case "DEATH":
          for (int o = 0; o < notelen; o++) {
            tn.add(new DeathNote(this));
          }
          break;
        case "NONE":
        case "REST":
          for (int o = 0; o < notelen; o++) {
            tn.add(null);
          }
          break;
        default:
          System.out.println("!WARNING! Unrecognized battle note type. " + strnote);
      }
    }
    this.notes = tn.iterator();
  }

  public void init() {
    this.state = BattleState.INTRO;
  }

  public boolean update() {
    return this.state.equals(BattleState.FINISHED);
    // player is dead or song is done
  }

  public void beat() {
    if (!this.state.equals(BattleState.FIGHT)) return; 

    if (App.player.health <= 0) {
      this.state = BattleState.LOSE;
      return;
    } else if (!this.notes.hasNext() && this.renderedNotes.isEmpty()) {
      this.state = BattleState.WIN;
      return;
    }

    if (this.lasttime + (60 * 1000 / this.bpm) >= System.currentTimeMillis()) return; // has enough time passed since the last beat?
    
    this.beat++;

    if (this.notes.hasNext()) { // throw in the next beat
      Note n = this.notes.next();
      if (n != null) {
        n.spawn();
        this.renderedNotes.add(n);
      }
    }
    ArrayList<Note> dq = new ArrayList<Note>();
    for (Note n : this.renderedNotes) { // stop attempting to draw notes that are gone
      if (n.update()) {
        dq.add(n);
        continue;
      }
      n.beat();
    }
    this.renderedNotes.removeAll(dq);
    this.lasttime = System.currentTimeMillis();
    this.enemem = Emotion.BOP; // reset rendering
  }

  // specifically for notes.
  public void hit(Direction dir) {
    this.enemem = Emotion.valueOf("HURT_"+dir.toString());
  }
  public void miss() {
    this.enemem = Emotion.HIT;
    App.player.health -= this.notedamage;
    if (this.misssound.isPlayed()) this.misssound.reset();
    if (!this.misssound.isPlaying()) this.misssound.play();
  }

  public void render(Graphics2D g) {
    switch (this.state) {
      case INTRO:
        if (!this.introscene.update()) {
          this.introscene.render(g); // render intro scene until finished
        } else if (!App.player.spaceDown()) {
          this.introcard.draw(0, 0, g); // render intro card until space pressed
        } else {
          this.state = BattleState.FIGHT;
        }
        break;
      case FIGHT:
        if (!this.song.isPlaying()) {
          this.song.play();
        }
        this.overlay.draw(0,0,g);

        this.enemy.render(this.enemem, g);
        if (this.enemem.equals(Emotion.HIT)) {
          this.missimage.draw(0,0,g);
        }

        for (Note n : this.renderedNotes.toArray(new Note[]{})) { // draw all the notes
          if (n == null) continue;
          n.render(g);
        }

        g.drawString(""+App.player.health, 0, 0);

        this.background.draw(0, 0, g); // draw in the image bg
        break;
      case LOSE:
        this.song.stop();
        if (!this.losetrack.isPlaying())
          this.losetrack.play();
        if (this.losetrack.isPlayed()) {
          this.state = BattleState.FINISHED;
        }
        break;
      case WIN:
        this.song.stop();
        this.state = BattleState.FINISHED;
        break;
      case FINISHED: // placeholder for animation completion
        break;
    }
  }

  public int getSpawnX(Direction direction) {
    float columnwidth = ((float) (App.f.getWidth() - (leftmargin+rightmargin))) / 4.0f;
    return (int) Math.round(leftmargin + columnwidth * Utils.dirtoint.get(direction) + columnwidth / 2.0);
  }

  public int getSpawnY() {
    return verticalmargin;
  }

  public int getEndY() {
    return App.f.getHeight() - verticalmargin;
  }

  public int getBeat() {
    return this.beat;
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {
    for (Note n : this.renderedNotes) {
      if (Utils.keydirs.get(n.direction) == e.getKeyCode() && Math.abs(n.getY()-this.getEndY()) < Constants.Battle.HITMARGIN) {
        if (n instanceof HoldNote) {
          ((HoldNote)n).hit(p);
        } else {
          n.hit();
        }
        return;
      }
    }
  }
}

class Note {
  public final Direction direction;
  protected Battle parent;
  protected Image image;
  protected Point location;
  protected boolean hit;
  private long lasttime;

  public Note(Battle p, Direction dir) {
    this.parent = p;
    this.direction = dir;
    this.image = Utils.arrowimages.get(dir);
    this.image.scaleToWidth((App.f.getWidth() - (Battle.leftmargin+Battle.rightmargin))/8.0f);
  }

  public void spawn() {
    this.hit = false;
    this.location = new Point( this.parent.getSpawnX(this.direction), this.parent.getSpawnY() );
    this.lasttime = System.currentTimeMillis();
  }

  public void beat() {
    if (!this.hit && this.pastBound()) {
      this.parent.miss();
      this.hit = true;
    }
  }

  protected void moveDown() {
    int dist = Math.round((System.currentTimeMillis()-this.lasttime)*this.parent.notespeed)/(60*1000/this.parent.bpm);
    if (dist > Constants.Battle.MINNOTEMOVE) {
      this.location.translate(0, dist);
      this.lasttime = System.currentTimeMillis();
    }
  }

  public void render(Graphics2D g) {
    if (this.hit) return;
    this.moveDown();

    this.image.draw(this.location.x, this.location.y, g);
  }

  public boolean update() { // when should i stop attempting to render this note?
    return this.hit;
  }

  public void hit() {
    this.parent.hit(this.direction);
    this.hit = true;
  }

  public int getY() {
    return this.location.y;
  }

  protected boolean pastBound() {
    return this.location.y > this.parent.getEndY();
  }
}

class HoldNote extends Note {
  Integer holdtime;
  Image tailimage = Utils.ARROWTAILIMAGE;
  int keydownon;

  public HoldNote(Battle p, Direction dir, int leng) {
    super(p, dir);
    this.holdtime = leng;
    this.image = Utils.longarrowimages.get(dir);
  }

  public void hit(KeyPress p) { // TODO: fix hold system
    switch (p) {
      case KEYDOWN:
        if (this.keydownon > 0) break;
        this.keydownon = this.parent.getBeat();
        break;
      case KEYUP:
        // the note was held for long enough
        if (this.keydownon > 0 && Math.abs( (super.parent.getBeat()-this.keydownon) - this.holdtime ) <= 1) {this.hit();}
        System.out.println("Battle Beat: ["+super.parent.getBeat()+"] | Button held for: ["+(super.parent.getBeat()-this.keydownon)+"] | Note Length: ["+this.holdtime+"] | Hold Difference: ["+((super.parent.getBeat()-this.keydownon) - this.holdtime)+"] Valid: "+(Math.abs( (super.parent.getBeat()-this.keydownon) - this.holdtime ) <= 1)+";");
        this.keydownon = -1;
        break;
    }
  }

  @Override
  public void render(Graphics2D g) {
    if (this.hit) return;
    this.moveDown();

    this.image.draw(this.location.x, this.location.y, g);
    
    for (int i = 1; i<this.holdtime; i++) {
      this.tailimage.draw(this.location.x, (int) (this.location.y-(Math.floor(this.parent.notespeed*i*this.image.getHeight())/(1000/this.parent.bpm))), g);
    }
  }

  @Override
  protected boolean pastBound() {
    return this.location.y > this.parent.getEndY() && this.keydownon < 0; // both past bound and not holding (allows too long notes)
  }
}

class DeathNote extends Note {
  public DeathNote(Battle p) {
    super(p, Direction.NONE);
  }

  @Override
  public void hit() {
    // L cant hit this bozo
  }
}