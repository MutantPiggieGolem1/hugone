package hugoneseven;

import java.awt.Point;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.File;

public class Constants {
    public static enum GameState {
        MENU,
        CUTSCENE,
        EXPLORATION,
        BATTLE
    }

    public static enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NONE
    }

    public static enum Emotion {
        IDLE,
        HAPPY,
        EXCITED,
        SAD,
        MAD,

        // for music battles
        BOP,
        HIT,
        HURT_UP,
        HURT_DOWN,
        HURT_LEFT,
        HURT_RIGHT
        
        // etc.
    }

    public static enum MoveState {
        STOP,
        MOVE1,
        MOVE2, RUN1, RUN2
    }

    public static enum RenderState {
        DEFAULT,
        DIALOGUE
    }

    public static enum BattleState {
        INTRO,
        FIGHT, // player is hitting notes
        LOSE, // <play death theme>, so sad
        WIN, // u win pog
        FINISHED // game officially ended
    }

    public static enum KeyPress {
        KEYDOWN,
        KEYUP
    }

    public static interface InteractableObject {
        public abstract void onInteraction();

        // public void onInteraction(int count);
        public abstract boolean collidesWith(Point p);
    }

    public static interface Feature {
        public boolean update(); // check for completion

        public void render(Graphics2D g); // draw this feature

        public abstract void reccieveKeyPress(KeyEvent e, KeyPress keydown); // wasd only

        public void init();
    }

    public static final String RESOURCEDIR = String.join(File.separator,new String[]{System.getProperty("user.dir"),"src","main","resources",""});
    public static final int REFRESHRATE = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
    public static final double FPS = REFRESHRATE; // 60.0 usually
    public static final double TPS = 20.0;

    public static final int HEALTHPERHEART = 25;
    public static final int MINNOTEMOVE = 2;
    public static final int HITMARGIN = 150;
    public static final int CHARACTERSIZE = 64;
}