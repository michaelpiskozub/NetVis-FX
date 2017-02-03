package netvis.model.input;

public interface TimeControllableFeeder extends PlayableFeeder {
    void faster();
    void slower();
    void skipToStart();
    void skipToEnd();
}