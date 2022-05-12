package hugone;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONObject;

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

    private final Rectangle NORMALBUTTON, CENTERBUTTON;
    private final int SPACING = 400;
    private boolean animating = false;
    private int buttonspeed = 30;

    public Menu(String id, JFrame f) {
        this.parent = f;
        JSONObject data = App.story.data.getJSONObject("menus").getJSONObject(id);

        this.id = id;
        this.background = new Image(data.getString("background"));
        
        JSONArray buttondim = data.getJSONArray("buttondim");
        JSONArray buttonloc = data.getJSONArray("buttonloc");
        JSONArray buttons = data.getJSONArray("buttons");
        this.buttons = new JButton[buttons.length()];
        for (int i = 0; i < this.buttons.length; i++) {
            JSONObject buttondata = buttons.getJSONObject(i);
            JButton button = new JButton(new ImageIcon(new Image(buttondata.getString("image")).getImage().getScaledInstance(buttondim.getInt(0), buttondim.getInt(1), 0)));
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
        
        int width = buttondim.getInt(0);
        int height= buttondim.getInt(1);
        this.NORMALBUTTON = new Rectangle(buttonloc.getInt(0), buttonloc.getInt(1), width, height);
        int cwidth = (int)Math.ceil(width*1.3);
        int cheight= (int)Math.ceil(height*1.3);
        this.CENTERBUTTON = new Rectangle(buttonloc.getInt(0)-(cwidth-width)/2, buttonloc.getInt(1)+(cheight-height)/2, cwidth, cheight);
    }

    @Override
    public void init() {
        int y = (int)this.NORMALBUTTON.getY();
        this.selectedbutton = this.buttons.length/2; // the centre button
        for (int i = 0; i<this.buttons.length; i++) {
            JButton b = this.buttons[i];
            b.setBounds(this.calculateX(i),y,(int)this.NORMALBUTTON.getWidth(),(int)this.NORMALBUTTON.getHeight());
            b.setVisible(true);
            b.setEnabled(true);
            this.parent.add(b);
        }
    }

    @Override
    public boolean update() {
        this.animate();
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
        if (keydown.equals(KeyPress.KEYUP) || this.animating) return;
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
            b.setFocusable(false);
            b.setVisible(false);
            b.setEnabled(false);
        }
    }

    private void animate() {
        if (!this.animating) return;
        int match = 0;
        for (int i = 0; i < this.buttons.length; i++) {
            JButton b = this.buttons[i];
            if (i==this.selectedbutton) {
                if (b.getBounds().equals(this.CENTERBUTTON)) {match++;continue;}
                b.setBounds(
                    calculateDelta(b.getX(), this.buttonspeed, (int)this.CENTERBUTTON.getX()), 
                    calculateDelta(b.getY(), 5, (int)this.CENTERBUTTON.getY()), 
                    calculateDelta(b.getWidth(), 10, (int)this.CENTERBUTTON.getWidth()),
                    calculateDelta(b.getHeight(), 10, (int)this.CENTERBUTTON.getHeight())
                );
            } else {
                int goal = this.calculateX(i);
                if (b.getX() == goal) {match++;continue;}
                b.setBounds(
                    calculateDelta(b.getX(), this.buttonspeed, goal), 
                    calculateDelta(b.getY(), 5, (int)this.NORMALBUTTON.getY()), 
                    calculateDelta(b.getWidth(), 10, (int)this.NORMALBUTTON.getWidth()),
                    calculateDelta(b.getHeight(), 10, (int)this.NORMALBUTTON.getHeight())
                );
            }
        }
        if (match >= this.buttons.length) this.animating = false;
    };

    private int calculateX(int index) { // get x pos for a button
        return (int)((index-this.selectedbutton) // dist from selected
            * (this.NORMALBUTTON.getWidth()+this.SPACING) // width + spacing
            + this.NORMALBUTTON.getX()); 
    }

    private int calculateDelta(int loc, int spd, int goal) {
        if (loc==goal) return goal;
        return loc > goal ? ((loc - spd > goal) ? loc-spd : goal) : ((loc + spd < goal) ? loc+spd : goal);
    }
}