package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import org.json.JSONObject;

import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Image;

class Card implements Feature {
  private String id;
  private Image image;
  private long debounce;
  private boolean skip = false;
  private String next;

  public Card(String id) {
    this.id = id;
    JSONObject data = App.story.data.getJSONObject("scenes").getJSONObject(this.id);
    this.image = new Image(data.getString("image")).scaleToWidth(hugone.util.Utils.WIDTH);
    if (!this.id.equals("death")) this.next = data.getString("next");
  }

  public Card(Image img, String next) {
    this.id = null;
    this.image = img;
    this.next = next;
  }

  public void init() {
    this.debounce = 0;
  }

  public boolean update() {
    this.debounce++;
    return this.skip;
  }

  public void render(Graphics2D g) {
    this.image.draw(0,0,g);
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {
    if (this.debounce >= Constants.DEBOUNCE && p.equals(KeyPress.KEYDOWN) && e.getKeyCode() == KeyEvent.VK_SPACE) this.skip = true;
  }

  @Override
  public void close() {
    this.skip = false;
  }

  @Override
  public String getNext() {
    if (this.id.equals("death")) {App.player.respawn();return App.story.checkpoint;}
    return this.next;
  }
}