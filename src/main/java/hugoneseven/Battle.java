package hugoneseven;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Graphics2D;

import hugoneseven.util.Audio;

@SuppressWarnings("unused")
class Battle implements Feature {
  private String id;
  private Character enemy;
  private BattleState state;
  private Dialogues dialogue;
  private Audio losetrack;
  
  public Battle(String id) throws JSONException {
    JSONObject data = App.story.data.getJSONObject("battles").getJSONObject(id);

    this.id = id;
    this.enemy = App.story.getCharacter(data.getString("enemy"));
    try {
      this.losetrack = new Audio("faintcourage.wav");
    } catch (Exception e) {
      System.out.println("!WARNING! Could not load battle lose track.");
    }
  }

  public boolean update() {
    return this.enemy.health <= 0 || App.player.health <= 0;
  }

  public void render(Graphics2D g) {
    switch (this.state) {
      case DIALOGUE:
        this.dialogue.render(g);
      case DEMO:
        this.enemy.render(Emotion.BOP,g); // possibly change to an animation
        App.player.render(Emotion.MAD,g);
        // play the notes perfectly
      break;
      case FIGHT:
        App.player.render(Emotion.BOP,g); // probably change
        this.enemy.render(Emotion.MAD,g);
        // show notes
      break;
      case LOSE:
        this.losetrack.play();
        if (this.losetrack.isPlayed()) {
          // death happens here
        }
      break;
      case WIN:
        // uh idk
      break;
    } 
  }
}