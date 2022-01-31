package hugoneseven.util;

import java.io.File;
import javax.sound.sampled.*;

public class Audio {
  private Clip clip;

  public Audio(String filepath) {
    try {
      this.clip = AudioSystem.getClip();
      this.clip.open(AudioSystem.getAudioInputStream(new File(Utils.RESOURCEDIR+filepath).getAbsoluteFile()));
    } catch (Exception e) {
      System.out.println("!WARNING! Audio file failed to load @"+filepath);
    }
  };
  public void play(){
    this.clip.start();
  };
  public void stop(){
    this.clip.stop();
    this.clip.close();
  };
  public boolean isPlaying(){
    return this.clip.isRunning();
  };
  public boolean isPlayed(){
    return this.clip.getLongFramePosition()+1 >= this.clip.getFrameLength();
  };
};