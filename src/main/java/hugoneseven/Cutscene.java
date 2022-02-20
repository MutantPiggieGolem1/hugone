package hugoneseven;

import java.awt.Graphics2D;

import hugoneseven.Constants.Feature;
import hugoneseven.util.Video;

class Cutscene implements Feature {
  Video video;

  public Cutscene(Video vid) {
    this.video = vid;
  }

  public boolean update() {
    return this.video.isPlayed();
  }

  public void render(Graphics2D g) {
    if (!this.video.isPlaying()) {
      this.video.play();
    }
    ;
  }
};