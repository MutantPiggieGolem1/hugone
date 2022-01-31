package hugoneseven.util;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Image extends JPanel {
  private BufferedImage image; 
  protected double scale = 4.0;

  public Image(String filepath) {
    try {
      this.image = Utils.getImage(Utils.RESOURCEDIR+filepath);
    } catch (Exception e) {
      System.out.println("!WARNING! Image file failed to load @"+filepath);
      this.image = Utils.NULLIMG.getImage();
    }
  }

  public void scale(double scale){
    this.scale = scale;
  }
  public BufferedImage getImage() {
    return this.image;
  }

  public void draw(int x, int y, Graphics2D g) {
    g.drawImage(this.image, x, y, this.image.getWidth()*this.scale, this.image.getHeight()*this.scale, x, y, x+this.image.getWidth(), y+this.image.getHeight(), null); //(int)Math.ceil(this.image.getWidth()*this.scale), (int)Math.ceil(this.image.getHeight()*this.scale)
  }
}