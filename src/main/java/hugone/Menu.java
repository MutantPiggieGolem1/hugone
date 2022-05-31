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
import hugone.Constants.RenderState;
import hugone.util.Image;

@SuppressWarnings("unused") // TODO: Fix duplicate menu buttons
public class Menu implements Feature {
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

    public Menu(String id, JFrame f) {
        this.parent = f;
        JSONObject data = App.story.data.getJSONObject("menus").getJSONObject(id);

        this.id = id;
        this.background = new Image(data.getString("background")).scaleToWidth(App.f.getWidth());
        
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
                    button.addActionListener((ActionEvent e) -> {
                        ((Menu)App.story.getCurrent()).setCard(App.story.getCard("gallery"));
                    });
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
        this.debounce = 0;
        this.renderstate = RenderState.DEFAULT;
    }

    private void setCard(Card card) {
        this.card = card;
        this.card.init();
        this.renderstate = RenderState.CARD;
    }

    @Override
    public boolean update() {
        switch (this.renderstate) {
            case DEFAULT:
                this.debounce++;
                for (JButton b : this.buttons) {
                    b.setVisible(true);
                }
                this.animate();
            break;
            case CARD:
                for (JButton b : this.buttons) {
                    b.setVisible(false);
                }
                if (this.card.update()) {
                    this.renderstate = RenderState.DEFAULT;
                    this.card.close();
                    this.card = null;
                }
            break;
            case DIALOGUE:
            break;
        }
        return false;
    }

    @Override
    public void render(Graphics2D g) {
        switch (this.renderstate) {
            case DEFAULT:
                this.background.draw(0,0,g);
                for (JButton b : this.buttons) {
                    b.paint(g);
                }
            break;
            case CARD:
                this.card.render(g);
            break;
            case DIALOGUE:
            break;
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
    }

    private void animate() {
        if (!this.animating) return;
        int match = 0;
        for (int i = 0; i < this.buttons.length; i++) {
            JButton b = this.buttons[i];
            if (i==this.selectedbutton) {
                if (b.getBounds().equals(this.CENTERBUTTON)) {match++;continue;}
                b.setBounds(
                    expoDelta(b.getX(), this.CENTERBUTTON.getX()), 
                    expoDelta(b.getY(), this.CENTERBUTTON.getY()), 
                    linearDelta(b.getWidth(), this.CENTERBUTTON.getWidth()),
                    linearDelta(b.getHeight(), this.CENTERBUTTON.getHeight())
                );
            } else {
                int goal = this.calculateX(i);
                if (b.getX() == goal) {match++;continue;}
                b.setBounds(
                    expoDelta(b.getX(), goal), 
                    expoDelta(b.getY(), this.NORMALBUTTON.getY()), 
                    linearDelta(b.getWidth(), this.NORMALBUTTON.getWidth()),
                    linearDelta(b.getHeight(), this.NORMALBUTTON.getHeight())
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

    private int linearDelta(int cur, double goal) {
        int spd = 5;
        if (Math.abs(cur-goal) < 2*spd) return (int)goal; // snap
        return cur > goal ? cur - spd : cur + spd;
    }

    private int expoDelta(int cur, double goal) {
        return expoDelta(cur, (int)goal);
    }
    private int expoDelta(int cur, int goal) {
        double mul = 0.11;
        if (Math.abs(cur-goal) < mul*100) return goal; // snap
        return (int)(cur+(goal-cur)*mul);
    }
}