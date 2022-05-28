package hugone.util;

import java.awt.image.BufferedImage;

public class Image extends javax.swing.JPanel {
  public static final Image NULL = new Image("null.png");
  private java.awt.Image image;

  public Image(String filename) {
    try {
      this.image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream(filename));
    } catch (Exception e) {
      System.out.println("!WARNING! Image file failed to load @" + filename);
      this.image = NULL.getImage();
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