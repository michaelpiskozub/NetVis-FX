package netvis.model.input.tshark.realtime;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;
import netvis.model.Packet;
import netvis.model.RealTimeOpener;
import netvis.model.input.DataFeeder;
import netvis.model.input.PacketCaptureUtilities;
import netvis.model.input.PlayableFeeder;
import netvis.model.input.tshark.PacketStreamThread;
import netvis.model.input.tshark.TSharkUtilities;
import netvis.ui.RealTimePlaybackControls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RealTimeDataFeeder implements DataFeeder, PlayableFeeder {
    private RealTimeOpener realTimeOpener;
    private int packetCounter;
    private int updateInterval;
    private Process tsharkProcess;
    private PacketStreamThread packetDataThread;
    private RealTimePlaybackControls realTimePlaybackControls;
    private int seconds;
    private Timeline timer;
    private boolean isTimerRunning;

    public RealTimeDataFeeder(RealTimeOpener realTimeOpener, TSharkUtilities tSharkUtilities) {
        this.realTimeOpener = realTimeOpener;
        packetCounter = 0;
        updateInterval = 1000;
        try {
            ProcessBuilder tsharkProcessBuilder = new ProcessBuilder(tSharkUtilities.initializeRealTimeCommand());
            tsharkProcess = tsharkProcessBuilder.start();
            packetDataThread = new PacketStreamThread(tsharkProcess.getInputStream());
            packetDataThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        realTimePlaybackControls = new RealTimePlaybackControls(this);
        realTimePlaybackControls.setPlayIcon(true);
        seconds = 0;
        timer = new Timeline(new KeyFrame(Duration.millis(updateInterval), event -> {
            seconds++;
            realTimePlaybackControls.setCurrentTimeLabelText("Current time: " + Integer.toString(seconds) + "s");
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
        isTimerRunning = true;
    }

    private boolean hasNewPackets() {
        return packetCounter < packetDataThread.getPacketLinesSize();
    }

    private void startTimer() {
        timer.play();
        isTimerRunning = true;
    }

    private void stopTimer() {
        timer.stop();
        isTimerRunning = false;
    }

    @Override
    public List<Packet> getNewPackets() {
        if (!isTimerRunning) {
            return null;
        } else {
            List<Packet> list = new ArrayList<>();
            while (hasNewPackets()) {
                list.add(PacketCaptureUtilities.lineToPacket(packetDataThread.getPacketLine(packetCounter)));
                packetCounter++;
                if (list.get(list.size() - 1).TIME > seconds) {
                    break;
                }
            }
            return list;
        }
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Node getControls() {
        return realTimePlaybackControls.getPlaybackControls();
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public void interrupt() {
        stopTimer();
        packetDataThread.setFinished(true);
        tsharkProcess.destroyForcibly();
    }

    @Override
    public void setPacketsPerSecondLabel(String text) {
        realTimePlaybackControls.setPacketsPerSecondLabel(text);
    }

    @Override
    public void play() {
        realTimePlaybackControls.setPlayIcon(true);
        realTimeOpener.startRealTime();
    }

    @Override
    public void pause() {
        interrupt();
        realTimePlaybackControls.setPlayIcon(false);
        realTimePlaybackControls.setPacketsPerSecondLabel("Packets / s: 0");
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
}
