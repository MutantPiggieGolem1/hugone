package hugone.util;

import java.io.File;
import javax.sound.sampled.*;

import hugone.Constants;

public class Audio {
  private Clip clip;
  private File inpfile;

  public Audio(String filepath) {
    try {
      this.inpfile = new File(Constants.RESOURCEDIR + filepath);
      if (this.inpfile.canRead()) {
        this.clip = AudioSystem.getClip();
        this.clip.open(AudioSystem.getAudioInputStream(this.inpfile.getAbsoluteFile()));
      } else {
        System.out.println("!WARNING! Audio file failed to read @" + filepath);
      }
    } catch (Exception e) {
      System.out.println("!WARNING! Audio file failed to load @" + filepath);
    }
  };

  public void changeVolume(float db) {
    FloatControl gainControl = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
    gainControl.setValue(db);
  }

  public void reset() {
    try {
      this.stop();
      this.clip = AudioSystem.getClip();
      this.clip.open(AudioSystem.getAudioInputStream(this.inpfile.getAbsoluteFile()));
      clip.setMicrosecondPosition(0);
    } catch (Exception e) {
      System.out.println("!WARNING! Audio file failed to reset!");
    }
  }

  public void play() {
    this.clip.start();
  };

  public void stop() {
    this.clip.stop();
    this.clip.close();
  };

  public boolean isPlaying() {
    return this.clip.isRunning();
  };

  public boolean isPlayed() {
    return this.clip.getLongFramePosition() + 1 >= this.clip.getFrameLength();
  };
};