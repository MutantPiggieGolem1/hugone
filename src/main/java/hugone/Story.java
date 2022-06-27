package hugone;

import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import hugone.Constants.Feature;
import hugone.Constants.GameState;

public class Story {
  public Player player;
  private HashMap<String, Menu> menus = new HashMap<String,Menu>();
  private HashMap<String, Card> cards = new HashMap<String,Card>();
  private HashMap<String, Character> characters = new HashMap<String, Character>();
  private HashMap<String, Cutscene> cutscenes = new HashMap<String, Cutscene>();
  private HashMap<String, Area> areas = new HashMap<String, Area>();
  private HashMap<String, Battle> battles = new HashMap<String, Battle>();
  private HashMap<String, Dialogues> dialogues = new HashMap<String, Dialogues>();

  private String current; // id of current
  public String checkpoint; // id of last died

  public final JSONObject data;

  public Story(String filename) {
    String out = "";
    Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream(filename));
    while (s.hasNextLine()) {
      out = out.concat(s.nextLine().trim()); // removing newlines & trailing spaces
    }
    s.close();
    this.data = new JSONObject(out);
  }

  public void init() throws JSONException {
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
    for (String id : JSONObject.getNames(scenes)) {
      JSONObject data = scenes.getJSONObject(id);
      switch (GameState.valueOf(data.getString("type"))) {
        case MENU:
          this.menus.put(id, new Menu(id));
          break;
        case CARD:
          this.cards.put(id, new Card(id));
          break;
        case CUTSCENE:
          this.cutscenes.put(id, new Cutscene(id));
          break;
        case EXPLORATION:
          this.areas.put(id, new Area(id));
          break;
        case BATTLE:
          this.battles.put(id, new Battle(id));
          break;
        default: // add all the story elements, must be sorted to fit strict typing
          System.out.println("!WARNING! Unrecognized type for story data loading! " + data.getString("type"));
      }
    }

    this.current = "STARTMENU";
    this.getCurrent().init();
  }

  public Character getCharacter(String id) {
    return this.characters.get(id);
  }

  public Dialogues getDialogue(String id) {
    return this.dialogues.get(id);
  }

  public void next() { // advance story state
    try {
      Feature cur = this.getCurrent();
      this.current = cur.getNext();
      cur.close();
      this.getCurrent().init();
    } catch (JSONException e) {
      System.out.println("!WARNING! Could not advance storyline."+e.toString());
    }
  }

  public GameState currentState() {
    try {
      return GameState.valueOf(this.data.getJSONObject("scenes").getJSONObject(this.current).getString("type"));
    } catch (JSONException e) {
      System.out.println("!WARNING! Could not determine story state. "+ this.current);
      return null;
    }
  }

  public Feature getCurrent() {
    switch (this.currentState()) {
      case MENU:
        return this.menus.get(current);
      case CARD:
        return this.cards.get(current);
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