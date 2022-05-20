package hugone.util;

import java.awt.image.BufferedImage;
import java.io.File;

public class Image extends javax.swing.JPanel {
  private java.awt.Image image;

  public Image(String filepath) {
    try {
      File f = new File(getClass().getClassLoader().getResource(filepath).toURI());
      if (f.canRead()) {
        this.image = javax.imageio.ImageIO.read(f);
      } else {
        System.out.println("!WARNING! Image file failed to read @" + filepath);
        this.image = Utils.NULLIMG.getImage();
      }
    } catch (Exception e) {
      System.out.println("!WARNING! Image file failed to load @" + filepath);
      this.image = Utils.NULLIMG.getImage();
    }
  }

  public Image scaleToWidth(int width) {
    if (width > 0) {
      this.image = this.image.getScaledInstance(width, Math.round(width / (float)this.image.getWidth(null) * this.image.getHeight(null)), BufferedImage.SCALE_SMOOTH);
    } else {
      System.out.println("!WARNING! Attempted to scale image to width <0!");
    }
    return this;
  }

  public java.awt.Image getImage() {
    return this.image;
  }

  public int getWidth() {
    return this.image.getWidth(null);
  };

  public int getHeight() {
    return this.image.getHeight(null);
  };

  public void draw(int x, int y, java.awt.Graphics2D g) {
    g.drawImage(this.image, x, y, this.image.getWidth(null), this.image.getHeight(null), null);
  }
}