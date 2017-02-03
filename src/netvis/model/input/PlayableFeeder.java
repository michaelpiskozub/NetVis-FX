package netvis.model.input;

public interface PlayableFeeder {
    void play();
    void pause();
    void togglePlay();
    boolean isPlaying();
}
