package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.*;
import hugone.util.*;

public class Battle implements Feature {
  protected static final int leftmargin = 400;
  protected static final int rightmargin = 100;
  protected static final int verticalmargin = 20;

  private String id;

  private Character enemy;
  private Cutscene introscene;
  private Card introcard;
  private Image background;
  private Audio song;
  private Image overlay;
  private Image missimage;
  private Audio misssound;
  private Audio losetrack;

  private List<Note> notedb = new ArrayList<Note>();
  private Iterator<Note> notes;
  private List<Note> renderedNotes;
  private Integer notedamage;
  private int notespeed;
  private int bpm;
  private int beat;

  private BattleState state = BattleState.INTRO;;
  private int debouncebeat;
  private long lasttime;
  private Emotion enemem = Emotion.BOP;
  private Point enemyloc;
  private String next;

  private int deaths = 0;
  private int maxdeaths;
  private String save;

  public Battle(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("battles").getJSONObject(id);

    this.id = id;
    this.enemy = App.story.getCharacter(data.getString("enemy"));
    this.notedamage = data.getInt("damage");
    this.notespeed = data.getInt("speed");
    this.bpm = data.getInt("bpm");

    this.maxdeaths = data.getInt("maxdeaths");
    this.save = data.getString("save");
    try {
      this.song = new Audio(data.getString("song"));
      this.losetrack = new Audio("faintcourage.wav");
      this.background = new Image(data.getString("background"));
      if (data.has("overlay")) {this.overlay = new Image(data.getString("overlay"));}
      this.misssound = new Audio(data.getString("misssound"));
      this.missimage = new Image(data.getString("missimage"));
      this.missimage.scaleToWidth(Utils.WIDTH);
      this.introcard = new Card(new hugone.util.Image(data.getString("introcard")),this.id);
      this.introscene = new Cutscene(new hugone.util.Video(data.getString("introscene"),App.f));
    } catch (Exception e) {
      System.out.println("!WARNING! Could not load battle data.\n"+e.toString());
      System.exit(1);
    }

    JSONArray notes = data.getJSONArray("notes");
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
          this.notedb.add(notelen > 1 ? new HoldNote(this, notedir, notelen) : new Note(this, notedir));
          break;
        case "DEATH":
          for (int o = 0; o < notelen; o++) {
            this.notedb.add(new DeathNote(this));
          }
          break;
        case "NONE":
        case "REST":
          for (int o = 0; o < notelen; o++) {
            this.notedb.add(null);
          }
          break;
        default:
          System.out.println("!WARNING! Unrecognized battle note type. " + strnote);
      }
    }
  }

  public void init() {
    this.state = BattleState.INTRO;
    this.renderedNotes = new ArrayList<Note>();
    this.beat = 0;
    this.enemyloc = new Point(50,App.f.getHeight()-200);
    this.notes = this.notedb.iterator();
    this.introcard.init();
    this.introscene.init();
    this.debouncebeat = 0;

    if (this.deaths >= this.maxdeaths) {
      this.next = this.save;
    }
  }

  public boolean update() {
    if (this.state.equals(BattleState.FIGHT)) this.enemyloc.setLocation(50, Math.sin(this.beat)*50+70);
    return this.next != null;
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

        for (Note n : new ArrayList<Note>(this.renderedNotes)) { // draw all the notes
          if (n == null) continue;
          n.render(g);
        }

        g.drawString(String.valueOf(App.player.health), 0, App.f.getHeight());

        this.background.draw(0, 0, g); // draw in the image bg
        break;
      case LOSE:
        this.song.stop();
        App.story.checkpoint = this.id;
        if (this.next != "death") {
          this.deaths++;
          this.next = "death";
        }
        break;
      case WIN:
        this.song.stop();
        this.next = App.story.data.getJSONObject("scenes").getJSONObject(this.id).getString("next");
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

  public static int getNoteSize() {
    return (Utils.WIDTH - (Battle.leftmargin+Battle.rightmargin))/8;
  }

  public int getBeat() {
    return this.beat;
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {
    if (this.introscene != null) this.introscene.reccieveKeyPress(e, p);
    if (this.introcard  != null) this.introcard.reccieveKeyPress(e, p);
    if (!this.state.equals(BattleState.FIGHT)) return;
    for (Note n : new ArrayList<Note>(this.renderedNotes)) { // create a copy to avoid concurrent modification
      if (Utils.keydirs.get(n.direction) != e.getKeyCode()) continue;
      if (n instanceof HoldNote h) {
        switch (p) {
          case KEYDOWN:
            if (Math.abs(n.getY()-this.getEndY()) > Constants.Battle.HITMARGIN && this.debouncebeat < 1) break;
            this.debouncebeat = 0;
          case KEYUP:
            h.hit(p);
            continue;
        }
      } else {
        if (Math.abs(n.getY()-this.getEndY()) > Constants.Battle.HITMARGIN && this.debouncebeat < 1) continue;
        this.debouncebeat = 0;
        n.hit();
      }
    }
  }

  @Override
  public void close() {
    this.losetrack.reset();
    this.introcard.close();
    this.introscene.close();
    this.song.reset();
    this.next = null;
  }

  @Override
  public String getNext() {
    return this.next;
  }

  class Note {
    protected final Direction direction;
    protected Battle parent;
    protected Image image;
    protected Point location;
    protected boolean hit;
    private long lasttime;

    public Note(Battle p, Direction dir) {
      this.parent = p;
      this.direction = dir;
      this.image = Utils.arrowimages.get(dir).scaleToWidth(Battle.getNoteSize());
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
      this.tailimage = Utils.arrowtailimage
        .stretchToHeight((this.notelength-1)*Battle.getNoteSize());
    }

    @Override
    public void spawn() {
      super.spawn();
      this.keydownon = -1;
    }

    public void hit(KeyPress p) {
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

      this.tailimage.draw(
        this.location.x + this.tailimage.getWidth()/2,
        this.location.y - this.notelength*Battle.getNoteSize() + Battle.getNoteSize()/2,
      g);
      this.image.draw(this.location.x, this.location.y, g);
      this.image.draw(this.location.x, this.location.y - this.notelength*Battle.getNoteSize(), g);
    }

    @Override
    protected boolean pastBound() {
      return this.keydownon < 0 ? super.pastBound() : this.location.y-Battle.getNoteSize()*this.notelength > this.parent.getEndY();
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
}