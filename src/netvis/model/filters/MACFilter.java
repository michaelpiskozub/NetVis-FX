package netvis.model.filters;

import netvis.model.Packet;
import netvis.model.PacketFilter;

import java.util.List;

public class MACFilter implements PacketFilter {
    private List<String> whiteList;
    private List<String> blackList;

    public MACFilter(List<String> whiteList, List<String> blackList) {
        this.whiteList = whiteList;
        this.blackList = blackList;
    }

    @Override
    public boolean isFilterTestPassed(Packet packet) {
        String sourceMAC = packet.SOURCE_MAC;
        String destinationMAC = packet.DESTINATION_MAC;
        return ((whiteList.isEmpty() || whiteList.contains(sourceMAC) || whiteList.contains(destinationMAC))
                && (!blackList.contains(sourceMAC) && !blackList.contains(destinationMAC)));
    }

    @Override
    public String getName() {
        return "MAC Filter";
    }
}
