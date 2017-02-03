package netvis.model.input.csv;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;
import netvis.model.Packet;
import netvis.model.RecordingOpener;
import netvis.model.input.DataFeeder;
import netvis.model.input.PacketCaptureUtilities;
import netvis.model.input.TimeControllableFeeder;
import netvis.ui.RecordedPlaybackControls;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSVDataFeeder implements EventHandler<ActionEvent>, DataFeeder, TimeControllableFeeder {
    private File file;
    private RecordingOpener recordingOpener;
    private int updateInterval;
    private CSVReader csvReader;
    private List<String[]> packetsList;
    private Iterator<String[]> packetLineIterator;
    private RecordedPlaybackControls recordedPlaybackControls;
    private int seconds;
    private int maxSeconds;
    private Timeline timer;
    private boolean isTimerRunning;
    private BooleanProperty goToEndProperty;
    private double speedRate;
    private BooleanProperty pauseProperty;

    public CSVDataFeeder(File file, RecordingOpener recordingOpener, BooleanProperty dataFeederPauseProperty,
                         BooleanProperty dataFeederGoToEndProperty) {
        this.file = file;
        this.recordingOpener = recordingOpener;
        updateInterval = 1000;
        try {
            Reader reader = new FileReader(file);
            csvReader = new CSVReader(reader);
            packetsList = csvReader.readAll();
            packetLineIterator = packetsList.iterator();
            packetLineIterator.next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recordedPlaybackControls = new RecordedPlaybackControls(this);
        recordedPlaybackControls.setPlayIcon(true);
        seconds = 0;
        maxSeconds = (int) Math.ceil(Double.parseDouble(packetsList.get(packetsList.size() - 1)[1]));
        timer = new Timeline(new KeyFrame(Duration.millis(updateInterval), this));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.playFromStart();
        isTimerRunning = true;
        goToEndProperty = new SimpleBooleanProperty(false);
        dataFeederGoToEndProperty.bind(goToEndProperty);
        speedRate = 1.0;
        pauseProperty = new SimpleBooleanProperty(false);
        dataFeederPauseProperty.bind(pauseProperty);
    }

    @Override
    public void handle(ActionEvent event) {
        seconds++;
        recordedPlaybackControls.setCurrentTimeLabelText("Current time: " + Integer.toString(seconds) + "s / " +
                maxSeconds + "s");
    }

    private void startTimer() {
        timer.playFromStart();
        isTimerRunning = true;
    }

    private void stopTimer() {
        timer.stop();
        isTimerRunning = false;
    }

    @Override
    public List<Packet> getNewPackets() {
        if (!packetLineIterator.hasNext()) {
            return null;
        } else if (!isTimerRunning) {
            return null;
        } else {
            List<Packet> list = new ArrayList<>();
            while (packetLineIterator.hasNext()) {
                list.add(PacketCaptureUtilities.lineToPacket(packetLineIterator.next()));
                if (!goToEndProperty.get() && list.get(list.size() - 1).TIME > seconds) {
                    break;
                }
            }
//            recordedPlaybackControls.setPacketsPerSecondLabel("Packets / s: " + list.size());
            if (goToEndProperty.get()) {
                int time = (int) Math.ceil(list.get(list.size() - 1).TIME);
                recordedPlaybackControls.setCurrentTimeLabelText("Current time: " + time + "s / " + maxSeconds + "s");
                recordedPlaybackControls.setPacketsPerSecondLabel("Packets / s: 0");
                stopTimer();
                recordedPlaybackControls.setOnDataEnd();
            }
            return list;
        }
    }

    @Override
    public boolean hasNext() {
        if (!packetLineIterator.hasNext()) {
            stopTimer();
            recordedPlaybackControls.setOnDataEnd();
        }
        return packetLineIterator.hasNext();
    }

    @Override
    public Node getControls() {
        return recordedPlaybackControls.getPlaybackControls();
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public void interrupt() {
        stopTimer();
    }

    @Override
    public void setPacketsPerSecondLabel(String text) {
        recordedPlaybackControls.setPacketsPerSecondLabel(text);
    }

    @Override
    public void play() {
        if (!isTimerRunning) {
            startTimer();
        }
        recordedPlaybackControls.setPlayIcon(true);
        pauseProperty.set(false);
    }

    @Override
    public void pause() {
        if (isTimerRunning) {
            stopTimer();
        }
        recordedPlaybackControls.setPlayIcon(false);
        recordedPlaybackControls.setPacketsPerSecondLabel("Packets / s: 0");
        pauseProperty.set(true);
    }

    @Override
    public void togglePlay() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public boolean isPlaying() {
        return isTimerRunning;
    }

    @Override
    public void faster() {
        updateInterval /= 2;
        timer.getKeyFrames().set(0, new KeyFrame(Duration.millis(updateInterval), this));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.playFromStart();
        speedRate *= 2;
        recordedPlaybackControls.setSpeedRateLabelText("Speed rate: " + speedRate);
    }

    @Override
    public void slower() {
        updateInterval *= 2;
        timer.getKeyFrames().set(0, new KeyFrame(Duration.millis(updateInterval), this));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.playFromStart();
        speedRate /= 2;
        recordedPlaybackControls.setSpeedRateLabelText("Speed rate: " + speedRate);
    }

    @Override
    public void skipToStart() {
        recordingOpener.openCSV(file);
    }

    @Override
    public void skipToEnd() {
        goToEndProperty.set(true);
    }
}
