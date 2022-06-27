package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Video;

class Cutscene implements Feature {
  private String id;
  private Video video;
  private boolean skip = false;

  public Cutscene(String id) {
    this.id = id;
    this.video = new Video(App.story.data.getJSONObject("scenes").getJSONObject(this.id).getString("video"),App.f);
  }

  public Cutscene(Video vid) {
    this.id = null;
    this.video = vid;
  }

  public void init() {
    this.video.play();
  }

  public boolean update() {
    return this.skip || this.video.isPlayed();
  }

  public void render(Graphics2D g) {}

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {
    if (p.equals(KeyPress.KEYDOWN) && e.getKeyCode() == KeyEvent.VK_SPACE) this.skip = true;
  }

  @Override
  public void close() {
    this.skip = false;
    this.video.close();
  }

  @Override
  public String getNext() {
    return App.story.data.getJSONObject("scenes").getJSONObject(this.id).getString("next");
  }
}