package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.*;
import hugone.util.*;

@SuppressWarnings("unused")
class Battle implements Feature {
  protected static final int leftmargin = 400;
  protected static final int rightmargin = 100;
  protected static final int verticalmargin = 20;

  private String id;
  private Character enemy;
  private Image background;
  private Audio song;
  private Image overlay;
  private Image missimage;
  private Audio misssound;
  private Audio losetrack;
  private BattleState state;

  private Cutscene introscene;
  private Card introcard;

  private Iterator<Note> notes;
  private ArrayList<Note> renderedNotes = new ArrayList<Note>();
  private Integer notedamage;
  public int notespeed;
  public int bpm;
  private int beat;

  private int debouncebeat;
  private long lasttime;
  private Emotion enemem = Emotion.BOP;
  private Point enemyloc;  

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
      this.missimage.scaleToWidth(Utils.WIDTH);
      this.introcard = new Card(new Image(data.getString("introcard")));
      this.introscene = new Cutscene(new Video(data.getString("introscene"),App.f));
    } catch (Exception e) {
      System.out.println("!WARNING! Could not load battle data.\n"+e.toString());
      System.exit(1);
    }

    JSONArray notes = data.getJSONArray("notes");
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
    this.enemyloc = new Point(50,App.f.getHeight()-200);
    this.introcard.init();
    this.introscene.init();
    this.debouncebeat = 0;
  }

  public boolean update() {
    if (this.state.equals(BattleState.FIGHT)) this.enemyloc.setLocation(50, Math.sin(this.beat)*50+70);
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
    this.debouncebeat++;

    if (this.notes.hasNext()) { // throw in the next beat
      Note n = this.notes.next();
      if (n != null) {
        n.spawn();
        this.renderedNotes.add(n);
      }
    }
    ArrayList<Note> dq = new ArrayList<Note>(); // deletion queue to prevent concurrent modification
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
    new Audio(this.misssound).play();
  }

  public void render(Graphics2D g) {
    switch (this.state) {
      case INTRO:
        if (!this.introscene.update()) {
          this.introscene.render(g); // render intro scene until finished
        } else if (!this.introcard.update()) {
          this.introcard.render(g); // render intro card until space pressed
        } else {
          this.state = BattleState.FIGHT;
        }
        break;
      case FIGHT:
        if (!this.song.isPlaying()) {
          this.song.play();
        }
        this.overlay.draw(0,0,g);

        this.enemy.render(this.enemyloc, this.enemem, g);
        if (this.enemem.equals(Emotion.HIT)) {
          this.missimage.draw(0,0,g);
        }

        for (Note n : this.renderedNotes) { // draw all the notes
          if (n == null) continue;
          n.render(g);
        }

        g.drawString(String.valueOf(App.player.health), 0, App.f.getHeight());

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
    float columnwidth = ((float) (Utils.WIDTH - (leftmargin+rightmargin))) / 4.0f;
    return (int) Math.round(leftmargin + columnwidth * Utils.dirtoint.get(direction) + columnwidth / 2.0);
  }

  public int getSpawnY() {
    return verticalmargin;
  }

  public int getEndY() {
    return App.f.getHeight() - verticalmargin;
  }

  public int getNoteSize() {
    return (Utils.WIDTH - (Battle.leftmargin+Battle.rightmargin))/8;
  }

  public int getBeat() {
    return this.beat;
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {
    if (this.introscene != null) this.introscene.reccieveKeyPress(e, p);
    if (this.introcard != null)  this.introcard.reccieveKeyPress(e, p);
    if (!this.state.equals(BattleState.FIGHT) || this.debouncebeat < 1) return;
    this.debouncebeat = 0;
    for (Note n : new ArrayList<Note>(this.renderedNotes)) {
      if (Utils.keydirs.get(n.direction) != e.getKeyCode()) continue;
      if (n instanceof HoldNote) {
        switch (p) {
          case KEYDOWN:
            if (Math.abs(n.getY()-this.getEndY()) > Constants.Battle.HITMARGIN) break;
          case KEYUP:
            ((HoldNote)n).hit(p);
            return; // only one note per beat per column
        }
      } else {
        if (Math.abs(n.getY()-this.getEndY()) > Constants.Battle.HITMARGIN) continue;
        n.hit();
      }
    }
  }

  @Override
  public void close() {
    this.losetrack.reset();
    this.introscene.close();
    this.song.reset();
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
    this.image = Utils.arrowimages.get(dir).scaleToWidth(p.getNoteSize());
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
  private int notelength;
  private Image tailimage;
  private int keydownon;

  public HoldNote(Battle p, Direction dir, int leng) {
    super(p, dir);
    this.notelength = leng;
    this.tailimage = Utils.arrowtailimages.get(this.direction)
      .scaleToWidth(this.parent.getNoteSize())
      .stretchToLength(this.notelength*this.parent.getNoteSize());
  }

  @Override
  public void spawn() {
    super.spawn();
    this.keydownon = -1;
  }

  public void hit(KeyPress p) { // TODO: Fix hold system
    switch (p) {
      case KEYDOWN:
        if (this.keydownon >= 0) break;
        this.keydownon = this.parent.getBeat();
        System.out.println("Battle Beat: ["+super.parent.getBeat()+"]");
        break;
      case KEYUP:
        if (this.keydownon >= 0 && super.parent.getBeat()-this.keydownon == this.notelength) {super.hit();}
        System.out.println("Battle Beat: ["+super.parent.getBeat()+"] | Button held for: ["+(super.parent.getBeat()-this.keydownon)+"] | Note Length: ["+this.notelength+"] | Hold Difference: ["+((super.parent.getBeat()-this.keydownon) - this.notelength)+"] Valid: "+(super.parent.getBeat()-this.keydownon == this.notelength)+";");
        this.keydownon = -1;
        break;
    }
  }

  @Override
  public void render(Graphics2D g) {
    if (this.hit) return;
    this.moveDown();

    this.tailimage.draw(this.location.x, this.location.y - this.notelength*this.parent.getNoteSize(), g);
    this.image.draw(this.location.x, this.location.y, g);
  }

  @Override
  protected boolean pastBound() {
    return this.keydownon < 0 ? super.pastBound() : this.location.y-this.parent.getNoteSize()*this.notelength > this.parent.getEndY();
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