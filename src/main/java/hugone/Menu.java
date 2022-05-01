package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Image;

@SuppressWarnings("unused")
public class Menu implements Feature {
    private JFrame parent;
    private String id;
    private Image background;
    private JButton[] buttons;
    private int selectedbutton;
    private Point buttonloc;
    private Dimension buttondim;

    private final Rectangle CENTERBUTTON;
    private final int SPACING = 500;
    private boolean animating;

    public Menu(String id, JFrame f) {
        this.parent = f;
        JSONObject data = App.story.data.getJSONObject("menus").getJSONObject(id);

        this.id = id;
        this.background = new Image(data.getString("background"));
        
        JSONArray buttondim = data.getJSONArray("buttondim");
        this.buttondim = new Dimension(buttondim.getInt(0),buttondim.getInt(1));
        JSONArray buttonloc = data.getJSONArray("buttonloc");
        this.buttonloc = new Point(buttonloc.getInt(0),buttonloc.getInt(1));
        JSONArray buttons = data.getJSONArray("buttons");
        this.buttons = new JButton[buttons.length()];
        for (int i = 0; i < this.buttons.length; i++) {
            JSONObject buttondata = buttons.getJSONObject(i);
            ImageIcon buttonicon = new ImageIcon(new Image(buttondata.getString("image")).getImage().getScaledInstance((int)this.buttondim.getWidth(), (int)this.buttondim.getHeight(), 0));
            JButton button = new JButton(buttondata.getString("title"),buttonicon);
            switch (buttondata.getInt("func")) {
                case 0: // return to menu
                    button.addActionListener((ActionEvent e) -> {
                        App.story.start();
                    });
                break;
                case 1: // settings

                break;
                case 2: // gallery
                    
                break;
            }
            button.setBorderPainted(false); 
            button.setContentAreaFilled(false); 
            button.setFocusPainted(false); 
            button.setOpaque(false);
            button.setFocusable(false);
            button.setVisible(false);     
            this.buttons[i] = button;
        }
        this.CENTERBUTTON = new Rectangle((int)this.buttonloc.getX(), (int)this.buttonloc.getY(), (int)(this.buttondim.getWidth()*1.2), (int)(this.buttondim.getHeight()*1.2));
    }

    @Override
    public void init() {
        int x = (int)this.buttonloc.getX();
        int y = (int)this.buttonloc.getY();
        this.selectedbutton = this.buttons.length/2; // the centre button
        for (JButton b : this.buttons) {
            b.setBounds(x,y,(int)this.buttondim.getWidth(),(int)this.buttondim.getHeight());
            x+=this.buttondim.getWidth()+this.SPACING;
            b.setVisible(true);
            b.setEnabled(true);
            this.parent.add(b);
        }
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public void render(Graphics2D g) {
        this.background.draw(0,0,g);
        for (JButton b : this.buttons) {
            b.paint(g);
        }
    }

    @Override
    public void reccieveKeyPress(KeyEvent e, KeyPress keydown) {
        if (keydown.equals(KeyPress.KEYUP)) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                this.selectedbutton = hugone.util.Utils.clamp(this.selectedbutton-1,this.buttons.length-1,0);
                this.animating = true;
                this.animate();
            break;
            case KeyEvent.VK_D:
                this.selectedbutton = hugone.util.Utils.clamp(this.selectedbutton+1,this.buttons.length-1,0);
                this.animating = true;
                this.animate();
            break;
            case KeyEvent.VK_SPACE:

            break;
        }
    }

    public void close() {
        for (JButton b : this.buttons) {
            b.setVisible(false);
            b.setEnabled(false);
        }
    }

    private void animate() { // linear interpolation for position and resizing
        if (!this.animating) return;
        int match = 0;
        for (int i = 0; i < this.buttons.length; i++) {
            JButton b = this.buttons[i];
            if (i==this.selectedbutton) {
                if (b.getBounds().equals(this.CENTERBUTTON)) {match++;continue;}
                b.setBounds(this.CENTERBUTTON);
            } else {
                if (b.getX() == this.calculateX(i)) {match++;continue;}
                b.setBounds(this.calculateX(i),(int)this.CENTERBUTTON.getY(), (int)this.CENTERBUTTON.getWidth(), (int)this.CENTERBUTTON.getHeight());
            }
        }
        if (match >= this.buttons.length) this.animating = false;
    };

    private int calculateX(int index) { // get x pos for a button
        return (int)((index-this.selectedbutton) // dist from selected
            * (this.CENTERBUTTON.getWidth()+this.SPACING) // width + spacing
            + this.CENTERBUTTON.getX()); 
    }
}