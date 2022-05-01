package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Video;

class Cutscene implements Feature {
  Video video;

  public Cutscene(Video vid) {
    this.video = vid;
  }

  public void init() {
    this.video.play();
  }

  public boolean update() {
    return this.video.isPlayed();
  }

  public void render(Graphics2D g) {
    if (!this.video.isPlaying()) {
      this.video.play();
    }
  }

  public void reccieveKeyPress(KeyEvent e, KeyPress p) {}
};