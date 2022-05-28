package hugone.util;

// import javax.media.Controller;
// import javax.media.Manager;
// import javax.media.MediaLocator;
// import javax.media.Player;
import javax.swing.JFrame;

// import hugone.Constants;

// public class Video {
//     Player player;

//     public Video(String filepath, JFrame f) {
//         MediaLocator mediapath = new MediaLocator("file:///" + Constants.RESOURCEDIR + filepath);
//         try {
//             this.player = Manager.createRealizedPlayer(mediapath);
//             if (this.player.getVisualComponent() != null) {
//                 f.add(this.player.getVisualComponent()); // pull out the video component, add it to the jframe
//             } else {
//                 System.out.println("!WARNING! Video file failed to embed. "+this.player.toString());
//                 System.exit(1);
//             }
//         } catch (Exception e) {
//             //System.out.println("!WARNING! Video file failed to load @" + mediapath.toString());
//             e.printStackTrace();
//             System.exit(1);
//         }
//     }

//     public void play() {
//         this.player.start();
//     }

//     public boolean isPlaying() {
//         return this.player.getState() == Controller.Started;
//     }

//     public boolean isPlayed() {
//         return this.player.getDuration().getNanoseconds() <= this.player.getMediaNanoseconds();
//     }

//     public void close() {
//        this.player.close();
//     }
// }
public class Video {

    public Video(String filepath, JFrame f) {

    }

    public void play() {
    }

    public boolean isPlaying() {
        return false;
    }

    public boolean isPlayed() {
        return true;
    }

    public void close() {
    }
}