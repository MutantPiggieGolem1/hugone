package hugoneseven.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

import hugoneseven.Constants;

public class Image extends JPanel {
  private BufferedImage image;
  protected double scale = 1.0;

  public Image(String filepath) {
    try {
      this.image = Utils.getImage(Constants.RESOURCEDIR + filepath);
    } catch (Exception e) {
      System.out.println("!WARNING! Image file failed to load @" + filepath);
      this.image = Utils.NULLIMG.getImage();
    }
  }

  public void setScale(double scale) {
    this.scale = scale;
  }

  public BufferedImage getImage() {
    return this.image;
  }

  public int getWidth() {
    return this.image.getWidth();
  };

  public int getHeight() {
    return this.image.getHeight();
  };

  public void draw(int x, int y, Graphics2D g) {
    g.drawImage(this.image, x, y, (int) Math.round(this.image.getWidth() * this.scale),
        (int) Math.round(this.image.getHeight() * this.scale), null);
  }

  public int getScaleHeight() {
    return (int) Math.round(this.image.getHeight() * this.scale);
  }
}