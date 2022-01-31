package hugoneseven;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.util.Video;

public class Story {
  public Player player;
  private HashMap<String,Character> characters =
    new HashMap<String,Character>();
  private HashMap<String, Cutscene> cutscenes =
    new HashMap<String, Cutscene>();
  private HashMap<String, Area> areas = 
    new HashMap<String, Area>();
  private HashMap<String, Battle> battles =
    new HashMap<String, Battle>();
  private HashMap<String, Dialogues> dialogues = 
    new HashMap<String, Dialogues>();

  private String current; // id of current
  public final JSONObject data;

  public Story(JSONObject data) {
    this.data = data;
  };
  public void init() throws JSONException {
    this.current = "intro";

    // load characters
    JSONObject datacharacters = this.data.getJSONObject("characters");
    for (String id : JSONObject.getNames(datacharacters)) {
      if (id.equals("PLAYER")) {
        this.player = new Player("Hugo");
      } else {
        this.characters.put(id,new Character(id));
      }
    }

    // load dialogue
    JSONObject datadialogue = this.data.getJSONObject("dialogue");
    for (String id : JSONObject.getNames(datadialogue)) {
      this.dialogues.put(id,new Dialogues(id));
    }

    // Load scenes
    JSONObject scenes = this.data.getJSONObject("scenes");
    for (String key : JSONObject.getNames(scenes)) {
      JSONObject value = scenes.getJSONObject(key);
      switch (GameState.valueOf(value.getString("type"))) {
        case CUTSCENE:
          this.cutscenes.put(key, new Cutscene(new Video(value.getString("video"))));
        break;
        case EXPLORATION:
          this.areas.put(key, new Area(value.getString("id")));
        break;
        case BATTLE:
          this.battles.put(key, new Battle(value.getString("id")));
        break;
        default: // add all the story elements, must be sorted in order to fit inside strict typing
          System.out.println("!WARNING! Unrecognized type for story data loading! "+value.getString("type"));
      }
    }
  }

  public Character getCharacter(String id) {
    return this.characters.get(id);
  }
  public Dialogues getDialogue(String id) {
    return this.dialogues.get(id);
  }

  public void next() { // advance story
    try {
      this.current = this.data.getJSONObject("scenes").getJSONObject(this.current).getString("next");
    } catch (JSONException e) {
      System.out.println("!WARNING! Could not advance storyline.");
    }
  }

  public GameState currentState() {
    try {
      return GameState.valueOf(this.data.getJSONObject("scenes").getJSONObject(current).getString("type"));
    } catch (JSONException e) {
      System.out.println("!WARNING! Could not determine story state. "+this.data.getJSONObject("scenes").getJSONObject(current).getString("type"));
      return null;
    }
  }

  public Feature getCurrent() {
    switch (this.currentState()) {
      case CUTSCENE:
        return this.cutscenes.get(current);
      case EXPLORATION:
        return this.areas.get(current);
      case BATTLE:
        return this.battles.get(current);
      default:
      System.out.println("!WARNING! Unrecognized CurrentState.");
        return null;
    }
  }
}