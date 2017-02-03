package netvis.model.input.dummy;

import javafx.scene.Node;
import netvis.model.Packet;
import netvis.model.input.DataFeeder;
import netvis.model.input.TimeControllableFeeder;
import netvis.ui.RecordedPlaybackControls;

import java.util.ArrayList;
import java.util.List;

public class DummyDataFeeder implements DataFeeder, TimeControllableFeeder {
    private RecordedPlaybackControls recordedPlaybackControls;

    public DummyDataFeeder() {
        recordedPlaybackControls = new RecordedPlaybackControls(this);
        recordedPlaybackControls.toggleDisablePlaybackButtons();
        recordedPlaybackControls.setSpeedRateLabelText("Speed rate: 0.0");
        recordedPlaybackControls.setInfoLabelText("No data available yet.");
    }

    @Override
    public List<Packet> getNewPackets() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Node getControls() {
        return recordedPlaybackControls.getPlaybackControls();
    }

    @Override
    public int getUpdateInterval() {
        return 1000;
    }

    @Override
    public void interrupt() {}

    @Override
    public void setPacketsPerSecondLabel(String text) {}

    @Override
    public void faster() {}

    @Override
    public void slower() {}

    @Override
    public void skipToStart() {}

    @Override
    public void skipToEnd() {}

    @Override
    public void play() {}

    @Override
    public void pause() {}

    @Override
    public void togglePlay() {}

    @Override
    public boolean isPlaying() {
        return false;
    }
}
