package hugone;

import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class Constants {
    public static enum GameState {
        MENU,
        CUTSCENE,
        EXPLORATION,
        BATTLE,
        DEATH, CARD
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
        MOVE0, MOVE1, MOVE2,
        RUN1, RUN2
    }

    public static enum RenderState {
        DEFAULT, DIALOGUE
    }

    public static enum BattleState {
        INTRO, // introduction scene is playing
        FIGHT, // player is hitting notes
        LOSE, // <play death theme>, so sad
        WIN // you win!
    }

    public static enum KeyPress {
        KEYDOWN,
        KEYUP
    }

    public static interface InteractableObject {
        public abstract void onInteraction();
        public abstract boolean collidesWith(java.awt.Rectangle r);
    }

    public static interface Feature {
        public void init(); // initalize/begin showing this feature

        public boolean update(); // check for this feature's completion

        public void render(Graphics2D g); // draw this feature on the screen

        public void close(); // stop/cleanup this feature's resources

        public String getNext(); // fetch the next feature in sequence

        public abstract void reccieveKeyPress(KeyEvent e, KeyPress keydown); // pass through keypresses from a single listener
    }

    private static final java.awt.DisplayMode defaultdisplaymode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
    public static final int REFRESHRATE = defaultdisplaymode.getRefreshRate();
    public static final double FPS = REFRESHRATE; // 60.0 usually
    public static final double TPS = 20.0;

    public class Battle {
        public static final int MINNOTEMOVE = 2;
        public static final int HITMARGIN = 150;
    }
    public static final int CHARACTERSIZE = 96;
    public static final long DEBOUNCE = 10;
}