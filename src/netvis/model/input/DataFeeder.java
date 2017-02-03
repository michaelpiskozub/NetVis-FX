package netvis.model.input;

import javafx.scene.Node;
import netvis.model.Packet;

import java.util.List;

public interface DataFeeder {
    List<Packet> getNewPackets();
    boolean hasNext();
    Node getControls();
    int getUpdateInterval();
    void interrupt();
    void setPacketsPerSecondLabel(String text);
}
