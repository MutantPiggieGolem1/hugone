package hugoneseven;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

import hugoneseven.Constants.Feature;
import hugoneseven.Constants.GameState;
import hugoneseven.util.Video;

public class Story {
  public Player player;
  private HashMap<String, Menu> menus = new HashMap<String,Menu>();
  private HashMap<String, Character> characters = new HashMap<String, Character>();
  private HashMap<String, Cutscene> cutscenes = new HashMap<String, Cutscene>();
  private HashMap<String, Area> areas = new HashMap<String, Area>();
  private HashMap<String, Battle> battles = new HashMap<String, Battle>();
  private HashMap<String, Dialogues> dialogues = new HashMap<String, Dialogues>();

  private String current; // id of current
  //private Menu winscreen,deathscreen;

  public final JSONObject data;

  public Story(JSONObject data) {
    this.data = data;
  };

  public void init() throws JSONException {
    this.start();
    
    // load menus
    JSONObject datamenus = this.data.getJSONObject("menus");
    for (String id : JSONObject.getNames(datamenus)) {
      this.menus.put(id,new Menu(id,App.f));
    }

    // load characters
    JSONObject datacharacters = this.data.getJSONObject("characters");
    for (String id : JSONObject.getNames(datacharacters)) {
      if (id.equals("PLAYER")) {
        this.player = new Player("Hugo");
      } else {
        this.characters.put(id, new Character(id));
      }
    }

    // load dialogue
    JSONObject datadialogue = this.data.getJSONObject("dialogue");
    for (String id : JSONObject.getNames(datadialogue)) {
      this.dialogues.put(id, new Dialogues(id));
    }

    // Load scenes
    JSONObject scenes = this.data.getJSONObject("scenes");
    for (String key : JSONObject.getNames(scenes)) {
      JSONObject value = scenes.getJSONObject(key);
      switch (GameState.valueOf(value.getString("type"))) {
        case CUTSCENE:
          this.cutscenes.put(key, new Cutscene(new Video(value.getString("video"),App.f)));
          break;
        case EXPLORATION:
          this.areas.put(key, new Area(value.getString("id")));
          break;
        case BATTLE:
          this.battles.put(key, new Battle(value.getString("id")));
          break;
        default: // add all the story elements, must be sorted to fit strict typing
          System.out.println("!WARNING! Unrecognized type for story data loading! " + value.getString("type"));
      }
    }
  }

  public Character getCharacter(String id) {
    return this.characters.get(id);
  }

  public Dialogues getDialogue(String id) {
    return this.dialogues.get(id);
  }

  public void start() {
    this.current = "intro";
  }

  public void next() { // advance story
    try {
      this.current = this.data.getJSONObject("scenes").getJSONObject(this.current).getString("next");
      this.getCurrent().init();
    } catch (JSONException e) {
      System.out.println("!WARNING! Could not advance storyline.");
    }
  }

  public GameState currentState() {
    try {
      return GameState.valueOf(this.data.getJSONObject("scenes").getJSONObject(current).getString("type"));
    } catch (JSONException e) {
      System.out.println("!WARNING! Could not determine story state. "
          + this.data.getJSONObject("scenes").getJSONObject(current).getString("type"));
      return null;
    }
  }

  public Feature getCurrent() {
    switch (this.currentState()) {
      case MENU:
        return this.menus.get(current);
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