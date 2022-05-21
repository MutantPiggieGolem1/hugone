package hugone.util;

import javax.sound.sampled.*;

public class Audio {
  private Clip clip;
  private long pausetime = -1;

  private AudioInputStream original;

  public Audio(String filename) {
    try {
      original = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResourceAsStream(filename));
      original.mark(Integer.MAX_VALUE);
      this.clip = AudioSystem.getClip();
      this.clip.open(original);
    } catch (Exception e) {
      System.out.println("!WARNING! Audio file failed to load @" + filename);
    }
  };

  public void changeVolume(float db) {
    FloatControl gainControl = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
    gainControl.setValue(db);
  }

  public void reset() {
    try {
      this.stop();
      this.original.reset();
      this.clip = AudioSystem.getClip();
      this.clip.open(this.original);
      this.clip.setMicrosecondPosition(0);
    } catch (Exception e) {
      System.out.println("!WARNING! Audio file failed to reset!");
    }
  }

  public void play() {
    if (this.pausetime > 0) this.clip.setMicrosecondPosition(this.pausetime);
    this.pausetime = -1;
    this.clip.start();
  };

  public void pause() {
    this.pausetime = this.clip.getMicrosecondPosition();
    this.clip.stop();
  }

  public void stop() {
    this.clip.stop();
    this.clip.close();
  }

  public boolean isPlaying() {
    return this.clip.isRunning();
  }

  public boolean isPlayed() {
    return this.clip.getLongFramePosition() + 1 >= this.clip.getFrameLength();
  }
}