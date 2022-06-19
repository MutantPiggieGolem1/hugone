package hugone.util;

import javax.sound.sampled.*;

public class Audio {
  private Clip clip;
  private AudioFormat format;
  private byte[] data;

  private long pausetime = -1;

  public Audio(String filename) {
    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(getClass().getClassLoader().getResourceAsStream(filename)));
      this.data = in.readAllBytes();
      this.format = in.getFormat();

      this.clip = AudioSystem.getClip();
      this.clip.open(this.format, this.data, 0, data.length);
    } catch (Exception e) {
      System.out.println("!WARNING! Audio failed to load @" + filename);
      e.printStackTrace();
      System.exit(1); // can't use null replacement because of stream complications
    }
  }

  public Audio(Audio a) {
    try {
      this.data = a.data;
      this.format = a.format;

      this.clip = AudioSystem.getClip();
      this.clip.open(this.format, this.data, 0, data.length);
    } catch (Exception e) {
      System.out.println("!WARNING! Audio failed to copy!");
      e.printStackTrace();
      System.exit(1);
    }
  }

public void changeVolume(float db) {
    FloatControl gainControl = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
    gainControl.setValue(db);
  }

  public void reset() {
    try {
      this.stop();
      this.clip = AudioSystem.getClip();
      this.clip.open(this.format, this.data, 0, this.data.length);
      this.clip.setMicrosecondPosition(0);
    } catch (Exception e) {
      System.out.println("!WARNING! Audio failed to reset!");
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