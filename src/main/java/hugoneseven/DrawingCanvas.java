package hugoneseven;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.BasicStroke;

import javax.swing.JComponent;

@SuppressWarnings("unused")
public class DrawingCanvas extends JComponent {
  private final int width;
  private final int height;
  public long prevtime = 0;
  
  public DrawingCanvas(int w, int h) {
    this.width = w;
    this.height = h;
  }

  RenderingHints rh = new RenderingHints(
    RenderingHints.KEY_ANTIALIASING,
    RenderingHints.VALUE_ANTIALIAS_ON
  );

  @Override
  public void paintComponent(Graphics g) {
    g.setColor(Color.BLUE);
    g.drawString("yeet", 20, 20);
    g.drawLine(0,0,App.f.getWidth(),App.f.getHeight());

    App.render((Graphics2D)g);
    g.dispose();
  }
}