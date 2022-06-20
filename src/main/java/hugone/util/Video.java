package hugone.util;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class Video {
    private EmbeddedMediaPlayerComponent component;

    public Video(String filepath, javax.swing.JFrame f) {
        this.component = new EmbeddedMediaPlayerComponent();
        this.component.setFocusable(false);
        f.setVisible(true);
        f.add(this.component);
        this.component.mediaPlayer().media().startPaused(filepath);
        this.component.mediaPlayer().controls().setRepeat(false);
    }

    public void play() {
        this.component.mediaPlayer().controls().play();
    }

    public void stop() {
        this.component.mediaPlayer().controls().stop();
    }

    public boolean isPlaying() {
        return this.component.mediaPlayer().status().isPlaying();
    }

    public boolean isPlayed() {
        return this.component.mediaPlayer().status().position() >= 1.0f;
    }

    public void close() {
        this.component.release();
    }
}