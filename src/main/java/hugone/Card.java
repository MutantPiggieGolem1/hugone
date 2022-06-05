package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Image;

class Card implements Feature {
  private Image image;
  private long debounce;
  private boolean skip = false;

  public Card(Image img) {
    this.image = img.scaleToWidth(hugone.util.Utils.WIDTH);
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
}