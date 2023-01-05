package hugone;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.Direction;
import hugone.Constants.Emotion;
import hugone.Constants.MoveState;
import hugone.util.Image;

/* Where "Character" should be rendered w/emotion:
*  - Dialogue
*  - Battle
*  - Area, but other view
*/
private val movemap: HashMap[MoveState, MoveState] = new HashMap[MoveState, MoveState]() {
  movemap.put(MoveState.STOP, MoveState.STOP);
  movemap.put(MoveState.MOVE0, MoveState.MOVE1);
  movemap.put(MoveState.MOVE1, MoveState.MOVE2);
  movemap.put(MoveState.MOVE2, MoveState.MOVE0);
  movemap.put(MoveState.RUN1, MoveState.RUN2);
  movemap.put(MoveState.RUN2, MoveState.RUN1);
}
class Character(val id: String) {
  val data: JSONObject = App.story.data.getJSONObject("characters").getJSONObject(id);
  private var name: String = data.getString("name");
  
  // setup emotions
  val images: JSONObject = data.getJSONObject("emotions");
  for (key: String <- JSONObject.getNames(images)) {
    String value = images.getString(key);
    Image img = new Image(value);
    img.scaleToWidth(512);
    this.emotions.put(Emotion.valueOf(key), img);
  }
  
  // setup directions
  if (data.has("directions")) {
    val directionsdata: JSONObject = data.getJSONObject("directions");
    for (direction <- Direction.values()) {
      val dir: String = direction.toString();
      this.directions.put(direction, new HashMap<MoveState, Image>());
      
      if (directionsdata.has(dir)) {
        val dirmovs: JSONObject = directionsdata.getJSONObject(dir);
        for (movestate <- MoveState.values()) {
          val mov: String = movestate.toString();
          
          if (dirmovs.has(mov)) {
            val img: Image = new Image(dirmovs.getString(mov));
            img.scaleToWidth(Constants.CHARACTERSIZE);
            this.directions.get(direction).put(movestate, img);
          } else {
            this.directions.get(direction).put(movestate, Image.NULL);
          }
        }
      }
    }
    val eximg: Image = this.directions.get(Direction.UP).get(MoveState.STOP);
    val h: Integer = Constants.CHARACTERSIZE;
    if (eximg != null) h = eximg.getHeight();
    this.pos = new Rectangle(0,0,Constants.CHARACTERSIZE,h);
  }
  
  if (data.has("health")) {
    this.maxhealth = this.health = data.getInt("health");
  }
  private val emotions: Map[Emotion, Image] = new HashMap[Emotion, Image]();
  private val directions: Map[Direction, Map[MoveState, Image]] = new HashMap[Direction, Map[MoveState, Image]]();
  
  protected var health: int = -1;
  protected var maxhealth: int = -1;
  
  protected var pos: Rectangle = new Rectangle();
  
  private var lastmovestate: MoveState = MoveState.STOP;
  
  def render(e: Emotion, g: Graphics2D): Void = {
    this.render(new java.awt.Point(50,App.f.getHeight()-600),e,g);
  }
  def render(loc: java.awt.Point, emotion: Emotion, g: Graphics2D): Void = {
    val eimg: Image | null = this.emotions.get(emotion);
    if (eimg != null) {
      eimg.draw(loc.x, loc.y, g);
    } else {
      System.out.println("!WARNING! Could not find emotion '"+ emotion.toString() +"' in character '"+ this.id +"'.");
    }
  }
  
  def render(dir: Direction, g: Graphics2D) = this.render(dir, false, g)
  def render(dir: Direction, stop: Boolean, g: Graphics2D) = {
    if (stop) this.lastmovestate = MoveState.STOP;
    this.render(dir, this.lastmovestate, g);
  }
  def render(dir: Direction, mvs: MoveState, g: Graphics2D): Void = this.directions.get(dir).get(mvs).draw(this.pos.x, this.pos.y, g)
  def moveLoop(): Void = this.lastmovestate = movemap.get(this.lastmovestate)
}