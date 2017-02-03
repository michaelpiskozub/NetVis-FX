package netvis.model;

import java.util.List;

public interface PacketListener {
    void allDataChanged(List<Packet> allPackets, int updateInterval, int intervalsComplete);
    void newPacketsArrived(List<Packet> newPackets);
    void interrupt();
}
