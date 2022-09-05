package hugone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import hugone.Constants.Feature;
import hugone.Constants.GameState;
import hugone.util.Utils;

class Story {
  public Player player;
  private Map<String, Menu> menus;
  private Map<String, Card> cards;
  private Map<String, Character> characters;
  private Map<String, Cutscene> cutscenes;
  private Map<String, Area> areas;
  private Map<String, Battle> battles;
  private Map<String, Dialogues> dialogues;

  private String current; // id of current
  public String checkpoint; // id of last died
  public final int version;

  public final JSONObject data;

  public Story(String filename) {
    String out = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename))).lines().collect(Collectors.joining("\n"));
    this.data = new JSONObject(out);
    this.version = this.data.getInt("version");
  }

  public void init() throws JSONException {
    menus = new HashMap<String,Menu>();
    cards = new HashMap<String,Card>();
    characters = new HashMap<String, Character>();
    cutscenes = new HashMap<String, Cutscene>();
    areas = new HashMap<String, Area>();
    battles = new HashMap<String, Battle>();
    dialogues = new HashMap<String, Dialogues>();
    
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
        System.out.println("!WARNING! Invalid CurrentState.");
        return null;
    }
  }

  private static final String savedir = "saves.json";
  public void save() throws IOException {
    JSONObject out = new JSONObject()
      .put("health",this.player.health)
      .put("inventory",this.player.inventory)
      .put("state",this.current)
      .put("checkpoint",this.checkpoint)
      .put("version",this.version);
    String filecontent = Utils.readFile(savedir);
    JSONObject savedata = filecontent == null ? new JSONObject() : new JSONObject(new JSONTokener(filecontent));
    savedata.put(""+System.currentTimeMillis(), out);
    if (Utils.writeFile(savedir, savedata.toString())) return;
    throw new IOException("Could not save game data.");
  }

  public void load(String saveid) throws IOException, JSONException {
    String filecontent = Utils.readFile(savedir);
    if (filecontent == null) throw new IOException("Could not load game data.");
    JSONObject savedata = new JSONObject(new JSONTokener(filecontent)).getJSONObject(saveid);
    if (savedata.getInt("version") != this.version) {
      System.out.println("!WARNING! Attempted to load save with mismatched version! ("+this.version+" / "+savedata.getInt("version")+")");
      return;
    }

    App.player.init(savedata.getInt("health"), Utils.<String>toSet(savedata.getJSONArray("inventory")));
    this.current = savedata.getString("state");
    this.checkpoint = savedata.getString("checkpoint");
    this.getCurrent().init();
  }
}