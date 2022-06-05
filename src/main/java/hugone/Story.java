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
    JSONObject datacards = this.data.getJSONObject("cards");
    for (String id : JSONObject.getNames(datacards)) {
      this.cards.put(id,new Card(new hugone.util.Image(datacards.getString(id))));
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
    for (String id : JSONObject.getNames(scenes)) {
      JSONObject data = scenes.getJSONObject(id);
      switch (GameState.valueOf(data.getString("type"))) {
        case MENU:
          this.menus.put(id,new Menu(id,App.f));
          break;
        case CUTSCENE:
          this.cutscenes.put(id, new Cutscene(new hugone.util.Video(data.getString("video"),App.f)));
          break;
        case EXPLORATION:
          this.areas.put(id, new Area(data.getString("id")));
          break;
        case BATTLE:
          this.battles.put(id, new Battle(data.getString("id")));
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

  public Card getCard(String id) {
    return this.cards.get(id);
  }

  public void start() { // for startmenu
    this.getCurrent().close();
    this.current = "intro";
  }

  public void next() { // advance story
    if (this.currentState().equals(GameState.MENU)) {
      System.out.println("!WARNING! Attempted to advance menustate!");
      return;
    }
    try {
      this.getCurrent().close();
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
          + this.data.getJSONObject("scenes").getJSONObject(this.current).getString("type"));
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