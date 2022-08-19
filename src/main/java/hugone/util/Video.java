package hugone.util;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class Video {
    private EmbeddedMediaPlayerComponent component;
    private javax.swing.JFrame f;
    private String filepath;

    public Video(String filepath, javax.swing.JFrame f) {
        this.f = f;
        this.filepath = filepath;
        this.component = new EmbeddedMediaPlayerComponent();
        this.component.setFocusable(false);
        this.component.setVisible(false);
    }

    public void play() {
        this.f.add(this.component);
        this.component.setVisible(true);
        this.f.setVisible(true);
        this.component.mediaPlayer().controls().setRepeat(false);
        if (!this.component.mediaPlayer().media().play(this.filepath)) System.out.println("!WARNING! Video failed to play!");
    }

    public void stop() {
        this.f.remove(this.component);
        this.component.setVisible(false);
        this.component.mediaPlayer().controls().stop();
    }

    public boolean isPlaying() {
        return this.component.mediaPlayer().status().isPlaying();
    }

    public boolean isPlayed() {
        return this.component.mediaPlayer().status().position() >= 1.0f;
    }

    public void close() {
        this.stop();
        this.component.release();
    }
}