package hugone;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import hugone.Constants.Feature;
import hugone.Constants.KeyPress;
import hugone.util.Image;
import hugone.util.Utils;

public class Menu implements Feature {
    @SuppressWarnings("unused")
    private String id;
    private Image background;
    private Button[] buttons;
    private int selectedbutton;

    private final Rectangle NORMALBUTTON, CENTERBUTTON;
    private final int SPACING = 400;
    private boolean animating = false;

    private Card card;
    private long debounce;

    private String next;

    public Menu(String id) {
        JSONObject data = App.story.data.getJSONObject("menus").getJSONObject(id);

        this.id = id;
        this.background = new Image(data.getString("background")).scaleToWidth(hugone.util.Utils.WIDTH);
        
        JSONArray buttondim = data.getJSONArray("buttondim");
        JSONArray buttonloc = data.getJSONArray("buttonloc");
        JSONArray buttons = data.getJSONArray("buttons");

        int width = buttondim.getInt(0);
        int height= buttondim.getInt(1);
        this.NORMALBUTTON = new Rectangle(buttonloc.getInt(0), buttonloc.getInt(1), width, height);
        int cwidth = (int)Math.ceil(width*1.2);
        int cheight= (int)Math.ceil(height*1.4);
        this.CENTERBUTTON = new Rectangle(buttonloc.getInt(0)-(cwidth-width)/2, buttonloc.getInt(1)+(cheight-height)/2, cwidth, cheight);

        this.buttons = new Button[buttons.length()];
        for (int i = 0; i < this.buttons.length; i++) {
            JSONObject buttondata = buttons.getJSONObject(i);
            this.buttons[i] = new Button(this,
                new Image(buttondata.getString("image")),
                this.NORMALBUTTON,
                Utils.funcmap.get(buttondata.getInt("func"))
            );
        }
    }

    @Override
    public void init() {
        this.selectedbutton = this.buttons.length/2; // the centre button
        for (int i = 0; i<this.buttons.length; i++) {
            this.buttons[i].lock(this.getRect(i));
        }
        this.debounce = 0;
    }

    @Override
    public boolean update() {
        this.debounce++;
        this.animate();
        return this.next != null;
    }

    @Override
    public void render(Graphics2D g) {
        this.background.draw(0,0,g);
        for (Button b : this.buttons) {
            b.draw(g);
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
                this.buttons[this.selectedbutton].run();
                this.debounce = 0;
            break;
        }
    }

    public void reccieveMousePress(java.awt.event.MouseEvent e) {
        java.awt.Point p = e.getPoint();
        for (Button button : this.buttons) {
            if (button.bounds.contains(p)) {
                button.run();
                e.consume();
                break;
            };
        }
    }

    @Override
    public void close() {
        this.next = null;
    }

    @Override
    public String getNext() {
        return this.next;
    }

    private void animate() {
        if (!this.animating) return;
        int match = 0;
        for (int i = 0; i < this.buttons.length; i++) {
            Button b = this.buttons[i];
            if (i==this.selectedbutton) {
                if (b.interp(this.CENTERBUTTON)) {match++;}
            } else {
                if (b.interp(this.getRect(i))) {match++;}
            }
        }
        if (match >= this.buttons.length) this.animating = false;
    }

    public Rectangle getRect(int i) {
        return new Rectangle(
            (int)((i-this.selectedbutton) * (this.NORMALBUTTON.getWidth()+this.SPACING) + this.NORMALBUTTON.getX()),
            (int)this.NORMALBUTTON.getY(), (int)this.NORMALBUTTON.getWidth(), (int)this.NORMALBUTTON.getHeight()
        );
    }

    class Button {
        private Menu parent;
        private Image img;
        private Rectangle bounds;
        private String func;

        public Button(Menu p, Image img, Rectangle bounds, String func) {
            this.parent = p;
            this.img = img; // ignoring scale because of rectangle image rendering
            this.bounds = new Rectangle(bounds);
            this.func = func;
        }

        public boolean interp(Rectangle b) {
            this.bounds.setBounds(
                (int)Utils.expoDelta(this.bounds.getX(), b.getX()), 
                (int)Utils.expoDelta(this.bounds.getY(), b.getY()), 
                (int)Utils.linearDelta(this.bounds.getWidth(), b.getWidth()),
                (int)Utils.linearDelta(this.bounds.getHeight(), b.getHeight())
            );
            return this.bounds.equals(b);
        }

        public void lock(Rectangle b) {
            this.bounds.setBounds(b);
        }

        public void run() {
            this.parent.next = this.func;
        }

        public void draw(Graphics2D g) {
            this.img.draw(this.bounds, g);
        }
    }
}