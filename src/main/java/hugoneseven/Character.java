package hugoneseven;

import java.awt.Graphics2D;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.enums.Direction;
import hugoneseven.util.Image;
import hugoneseven.util.Utils;

/* Where "Character" should be rendered w/emotion:
 *  - Dialogue
 *  - Battle
 *  - Area, but other view
*/

public class Character {
  private String name;
  private String id;
  private HashMap<Emotion,Image> emotions = new HashMap<Emotion,Image>();
  private HashMap<Direction,HashMap<MoveState,Image>> directions = new HashMap<Direction,HashMap<MoveState,Image>>();

  protected int health = -1;
  protected int[] pos = new int[]{0,0}; // placeholder
  public final HashMap<MoveState,MoveState> movemap = new HashMap<MoveState,MoveState>();

  private MoveState lastmovestate = MoveState.STOP;

  public Character(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("characters").getJSONObject(id);
    
    this.id = id;
    this.name = data.getString("name");

    // setup emotions
    JSONObject images = data.getJSONObject("emotions");
    for (String key : JSONObject.getNames(images)) {
      String value = images.getString(key);
      this.emotions.put(Emotion.valueOf(key),new Image(value));
    }

    // setup directions
    if (data.has("directions")) {
      JSONObject directionsdata = data.getJSONObject("directions");
      for (Direction direction : Direction.values()) {
        String dir = direction.toString();
        this.directions.put(direction,new HashMap<MoveState,Image>());

        if (directionsdata.has(dir)) {
          JSONObject dirmovs = directionsdata.getJSONObject(dir);
          for (MoveState movestate : MoveState.values()) {
            String mov = movestate.toString();

            if (dirmovs.has(mov)) {
              this.directions.get(direction).put(movestate,new Image(dirmovs.getString(mov)));
            } else {
              this.directions.get(direction).put(movestate,Utils.NULLIMG);
            }
          }
        }
      }
    }

    if (data.has("health")){
      this.health = data.getInt("health");
    }

    movemap.put(MoveState.STOP,MoveState.STOP);
    movemap.put(MoveState.MOVE1,MoveState.MOVE2);
    movemap.put(MoveState.MOVE2,MoveState.MOVE1);
  }

  protected void setName(String name) {
    this.name = name;
  }
  public String getName(){
    return this.name;
  }

  public void render(Emotion emotion,Graphics2D g){
    Image eimg = this.emotions.get(emotion);
    if (eimg != null) {eimg.draw(50,App.f.getHeight()-(200+eimg.getImage().getHeight()),g);} // placeholder coords
    else {System.out.println("!WARNING! Emotion image failed to draw! Character: "+this.id+" Emotion: "+emotion.toString());}
  }
  
  public void render(Direction dir,Graphics2D g){
    this.render(dir,false,g);
  }
  public void render(Direction dir, boolean stop,Graphics2D g){
    if (stop) {
      this.lastmovestate = MoveState.STOP;
    }
    this.render(dir,this.lastmovestate,g);
  }
  public void render(Direction dir, MoveState mvs,Graphics2D g) {
    this.directions.get(dir).get(mvs).draw(this.pos[0],this.pos[1],g);
  }

  public void moveLoop() {
    this.lastmovestate = movemap.get(this.lastmovestate); // update the move state
  }
}