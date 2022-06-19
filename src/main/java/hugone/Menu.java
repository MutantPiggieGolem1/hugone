package hugone;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONObject;

import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.Constants.RenderState;
import hugone.util.Image;

@SuppressWarnings("unused")
public class Menu implements Feature { // TODO: Fix duplicate menu buttons
    private JFrame parent;
    private String id;
    private RenderState renderstate;
    private Image background;
    private JButton[] buttons;
    private int selectedbutton;

    private final Rectangle NORMALBUTTON, CENTERBUTTON;
    private final int SPACING = 400;
    private boolean animating = false;

    private Card card;
    private long debounce;

    private String next;

    public Menu(String id, JFrame f) {
        this.parent = f;
        JSONObject data = App.story.data.getJSONObject("menus").getJSONObject(id);

        this.id = id;
        this.background = new Image(data.getString("background")).scaleToWidth(hugone.util.Utils.WIDTH);
        
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
                        this.next = "intro";
                    });
                break;
                case 1: // settings
                    button.addActionListener((ActionEvent e) -> {
                        // this.next = "settings";
                    });
                break;
                case 2: // gallery
                    button.addActionListener((ActionEvent e) -> {
                        this.next = "gallery";
                    });
                break;
            }
            button.setBorder(null);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false); 
            button.setOpaque(false);
            button.setHideActionText(true);
            button.setMultiClickThreshhold(Constants.DEBOUNCE);
            button.setFocusable(false);
            button.setVisible(false);
            this.parent.add(button);
            this.buttons[i] = button;
        }
        
        int width = buttondim.getInt(0);
        int height= buttondim.getInt(1);
        this.NORMALBUTTON = new Rectangle(buttonloc.getInt(0), buttonloc.getInt(1), width, height);
        int cwidth = (int)Math.ceil(width*1.4);
        int cheight= (int)Math.ceil(height*1.4);
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
        }
        this.debounce = 0;
    }

    @Override
    public boolean update() {
        this.debounce++;
        for (JButton b : this.buttons) {
            b.setVisible(true); // neither does this line ;-;
        }
        this.animate();
        return this.next != null;
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
        if (this.card != null) this.card.reccieveKeyPress(e, keydown);
        if (!keydown.equals(KeyPress.KEYDOWN)) return;
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
                if (this.debounce < Constants.DEBOUNCE) return;
                this.buttons[this.selectedbutton].doClick();
                this.debounce = 0;
            break;
        }
    }

    public void close() {
        for (JButton b : this.buttons) {
            b.setFocusable(false);
            b.setVisible(false);
            b.setEnabled(false);
        }
        this.next = null;
    }

    private void lockPlace() {
        this.animating = false;
        for (int i = 0; i < this.buttons.length; i++) {
            JButton b = this.buttons[i];
            if (i==this.selectedbutton) {
                b.setBounds(this.CENTERBUTTON);
            } else {
                b.setBounds(this.calculateX(i),this.NORMALBUTTON.y,this.NORMALBUTTON.width,this.NORMALBUTTON.height);
            }
        }
    }

    private void animate() { // TODO: Rescale Image & Button simultaneously
        if (!this.animating) return;
        int match = 0;
        for (int i = 0; i < this.buttons.length; i++) {
            JButton b = this.buttons[i];
            if (i==this.selectedbutton) {
                if (b.getBounds().equals(this.CENTERBUTTON)) {match++;continue;}
                b.setBounds(
                    expoDelta(b.getX(), this.CENTERBUTTON.x), 
                    expoDelta(b.getY(), this.CENTERBUTTON.y), 
                    linearDelta(b.getWidth(), this.CENTERBUTTON.getWidth()),
                    linearDelta(b.getHeight(), this.CENTERBUTTON.getHeight())
                );
            } else {
                int goal = this.calculateX(i);
                if (b.getX() == goal) {match++;continue;}
                b.setBounds(
                    expoDelta(b.getX(), goal), 
                    expoDelta(b.getY(), this.NORMALBUTTON.y), 
                    linearDelta(b.getWidth(), this.NORMALBUTTON.getWidth()),
                    linearDelta(b.getHeight(), this.NORMALBUTTON.getHeight())
                );
            }
        }
        if (match >= this.buttons.length) this.animating = false;
    }

    private int calculateX(int index) { // get x pos for a button
        return (int)((index-this.selectedbutton) // dist from selected
            * (this.NORMALBUTTON.getWidth()+this.SPACING) // width + spacing
            + this.NORMALBUTTON.getX()); 
    }

    private int linearDelta(int cur, double goal) {
        int spd = 5;
        if (Math.abs(cur-goal) < 2*spd) return (int)goal; // snap
        return cur > goal ? cur - spd : cur + spd;
    }

    private int expoDelta(int cur, int goal) {
        double mul = 0.11;
        if (Math.abs(cur-goal) < mul*50) return goal; // snap
        return (int)(cur+(goal-cur)*mul);
    }

    @Override
    public String getNext() {
        return this.next;
    }
}