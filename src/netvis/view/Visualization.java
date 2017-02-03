package netvis.view;

import javafx.scene.Node;
import netvis.model.DataController;
import netvis.model.PacketListener;
import netvis.model.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Visualization implements PacketListener {
    protected final List<Packet> LIST_OF_PACKETS;
    protected List<Packet> newPackets;
    private final DataController dataController;
    protected boolean haveAllDataChanged;
    protected boolean isShown;
    protected Node controls;

    public Visualization(DataController dataController) {
        dataController.addPacketListener(this);
        LIST_OF_PACKETS = Collections.synchronizedList(dataController.getPackets());
        newPackets = new ArrayList<>();
        this.dataController = dataController;
        haveAllDataChanged = false;
        isShown = false;
    }

    public abstract Node getVisualization();
    public abstract String getName();
    public abstract String getDescription();
    protected abstract Node initializeControls();

    public void activate() {
        isShown = true;
        newPackets = LIST_OF_PACKETS;
        haveAllDataChanged = true;
    }

    public void deactivate() {
        isShown = false;
    }

    public boolean isShown() {
        return isShown;
    }

    public Node getControls() {
        return controls;
    }

    @Override
    public void allDataChanged(List<Packet> allPackets, int updateInterval, int intervalsComplete) {
        if (isShown) {
            newPackets = allPackets;
            haveAllDataChanged = true;
        }
    }

    @Override
    public void newPacketsArrived(List<Packet> newPackets) {
        if (isShown) {
            this.newPackets = newPackets;
        }
    }

    @Override
    public void interrupt() {
        deactivate();
    }
}
