package hugone;

import java.awt.Graphics2D;
//import java.awt.RenderingHints;

import javax.swing.JFrame;

public class DrawingCanvas extends javax.swing.JComponent {
  public JFrame parent;
  public long prevtime = 0;
  private boolean draw = true;

  public DrawingCanvas(JFrame f) {
    this.parent = f;
    this.setBackground(java.awt.Color.GRAY);
  }

  public void startDraw() { // begin the draw loop!
    while (this.draw) {
      if (!this.parent.isShowing()) continue; // dont render in background
      if (System.currentTimeMillis() - this.prevtime >= 1000.0 / Constants.FPS) {
        this.repaint();
        this.prevtime = System.currentTimeMillis();
      }
    }
  }

  public void stopDraw() {
    this.draw = false;
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    App.render(g2);
    App.postRender(g2);
    g.dispose();
    g2.dispose();
  }
}