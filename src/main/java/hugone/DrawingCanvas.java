package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

import hugone.Constants.KeyPress;

public class DrawingCanvas extends javax.swing.JComponent implements KeyListener, MouseListener {
  public JFrame parent;
  public long prevtime = 0;
  private boolean draw = true;

  public DrawingCanvas(JFrame f) {
    this.parent = f;
    this.setFocusable(true);
    this.requestFocus();
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
    
    App.render(g2);
    App.postRender(g2);
    g.dispose();
    g2.dispose();
  }

  @Override
  public void keyTyped(KeyEvent e) {
  };

  @Override
  public void keyPressed(KeyEvent e) {
    App.story.getCurrent().reccieveKeyPress(e,KeyPress.KEYDOWN);
  }

  @Override
  public void keyReleased(KeyEvent e) {
    App.story.getCurrent().reccieveKeyPress(e,KeyPress.KEYUP);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (App.story.getCurrent() instanceof Menu m) {
      m.reccieveMousePress(e);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}
}