package hugoneseven;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import hugoneseven.util.Image;

import java.awt.Graphics2D;

public class Constants {
    public static enum GameState {
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
        BOP // for music battles
        // etc.
    }

    public static enum MoveState {
        STOP,
        MOVE1,
        MOVE2
    }

    public static enum RenderState {
        DEFAULT,
        DIALOGUE
    }

    public static enum BattleState {
        DIALOGUE, // enemy/player is saying something
        FIGHT, // player is hitting notes
        LOSE, // <play death theme>, so sad
        WIN // u win pog
    }

    public static interface InteractableObject {
        public abstract void onInteraction();

        // public void onInteraction(int count);
        public abstract HashSet<List<Integer>> getCoords();
    }

    public static interface Feature {
        public abstract boolean update(); // check for completion

        public abstract void render(Graphics2D g); // draw this feature
    }

    public static final String RESOURCEDIR = "./src/main/resources/";
    public static final double FPS = 60.0;
    public static final double TPS = 20.0;
    public static Image getArrowImage(Direction dir) {
        HashMap<Direction,String> imagepaths = new HashMap<Direction,String>();
        imagepaths.put(Direction.LEFT,"leftarrow.png");
        imagepaths.put(Direction.RIGHT,"rightarrow.png");
        imagepaths.put(Direction.UP,"uparrow.png");
        imagepaths.put(Direction.DOWN,"downarrow.png");
        return new Image(RESOURCEDIR+imagepaths.get(dir));
    }
}