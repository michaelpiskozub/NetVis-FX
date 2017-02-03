package netvis.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import netvis.model.input.DataFeeder;

import java.util.ArrayList;
import java.util.List;

public class DataController implements EventHandler<ActionEvent> {
    private DataFeeder dataFeeder;
    private int updateInterval;
    private final List<PacketListener> packetListeners;
    private final List<PacketFilter> filters;
    private final List<Packet> allPackets;
    private final List<Packet> filteredPackets;
    private int intervalsComplete;
    private Timeline timer;

    public DataController(DataFeeder dataFeeder, int updateInterval) {
        this.dataFeeder = dataFeeder;
        this.updateInterval = updateInterval;
        packetListeners = new ArrayList<>();
        filters = new ArrayList<>();
        allPackets = new ArrayList<>();
        filteredPackets = new ArrayList<>();
        intervalsComplete = 0;
        timer = new Timeline(new KeyFrame(Duration.millis(updateInterval), this));
        timer.setCycleCount(Timeline.INDEFINITE);
        NormalizeFactory.INSTANCE.setDataController(this);
        timer.playFromStart();
    }

    @Override
    public void handle(ActionEvent event) {
        List<Packet> newPackets = dataFeeder.getNewPackets();
        if (newPackets != null) {
            dataFeeder.setPacketsPerSecondLabel("Packets / s: " + newPackets.size());
            allPackets.addAll(newPackets);
            intervalsComplete++;
            applyFilters(newPackets);
            filteredPackets.addAll(newPackets);
            for (PacketListener l : packetListeners) {
                l.newPacketsArrived(newPackets);
            }
        }
        if (!dataFeeder.hasNext()) {
            timer.stop();
        }
    }

    public void interruptAll() {
        timer.stop();
        packetListeners.forEach(PacketListener::interrupt);
        dataFeeder.interrupt();
    }

    public DataFeeder getDataFeeder() {
        return dataFeeder;
    }

    public void setDataFeeder(DataFeeder newDataFeeder) {
        dataFeeder.interrupt();
        dataFeeder = newDataFeeder;
        allPackets.clear();
        applyAllDataChanged();
    }

    public void addPacketListener(PacketListener listener) {
        packetListeners.add(listener);
    }

    public void removePacketListener(PacketListener listener) {
        packetListeners.remove(listener);
    }

    public void addFilter(PacketFilter packetFilter) {
        filters.add(packetFilter);
//        applyAllDataChanged();
    }

    public void removeFilter(PacketFilter packetFilter) {
        filters.remove(packetFilter);
    }

    public List<Packet> getPackets() {
        return filteredPackets;
    }

    private void applyFilters(List<Packet> list) {
        List<Packet> packetsToBeRemoved = new ArrayList<>();
        for (PacketFilter f : filters) {
            for (Packet p : list) {
                if (!f.isFilterTestPassed(p)) {
                    packetsToBeRemoved.add(p);
                }
            }
        }
        list.removeAll(packetsToBeRemoved);
    }

    public void applyAllDataChanged() {
        filteredPackets.clear();
        filteredPackets.addAll(allPackets);
        applyFilters(filteredPackets);
        for (PacketListener l : packetListeners) {
            l.allDataChanged(filteredPackets, updateInterval, intervalsComplete);
        }
        timer.playFromStart();
    }
}
