package hugoneseven;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Button;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import hugoneseven.Constants.Feature;
import hugoneseven.Constants.KeyPress;
import hugoneseven.util.Image;

public class Menu implements Feature {
    private JFrame parent;
    private String id;
    private Image background;
    private Button[] buttons;
    private Point buttonloc;
    private Dimension buttondim;

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
        this.buttons = new Button[buttons.length()];
        for (int i = 0; i < buttons.length(); i++) {
            JSONObject buttondata = buttons.getJSONObject(i);
            Button button = new Button(buttondata.getString("title"));
            switch (buttondata.getInt("func")) {
                case 0: // return to menu
                    button.addActionListener((ActionEvent e) -> {
                        App.story.start();
                    });
                break;
                case 1: // credits
                break;
            }
            f.add(button);
            this.buttons[i] = button;
        }
    }

    @Override
    public void init() {
        int x = (int)this.buttonloc.getX();
        int y = (int)this.buttonloc.getY();
        for (Button b : this.buttons) {
            b.setBounds(x,y,(int)this.buttondim.getWidth(),(int)this.buttondim.getHeight());
            x+=this.buttondim.getWidth()*1.2;
        }
    }

    @Override
    public boolean update() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void render(Graphics2D g) {
        for (Button b : this.buttons) {
            b.repaint();
        }
        this.background.draw(0,0,g);
    }

    @Override
    public void reccieveKeyPress(KeyEvent e, KeyPress keydown) {
        if (keydown.equals(KeyPress.KEYUP)) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            break;
            case KeyEvent.VK_RIGHT:
            break;
            case KeyEvent.VK_SPACE:
            break;
        }
    }
}

/*
{
    "bgimage": "img.png",
    "bgmusic": "mus.wav",
    * "cards": [
        {
            "title": "Respawn?",
            "image": null,
            "function": "startmenu"
        },
        {
            "title": "Settings",
            "image": "settings.png",
            "function": "settings"
        }
    ]
}
*/