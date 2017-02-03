package netvis.model.filters;

import netvis.model.Packet;
import netvis.model.PacketFilter;

import java.util.List;

public class IPFilter implements PacketFilter {
    private List<String> whiteList;
    private List<String> blackList;

    public IPFilter(List<String> whiteList, List<String> blackList) {
        this.whiteList = whiteList;
        this.blackList = blackList;
    }

    @Override
    public boolean isFilterTestPassed(Packet packet) {
        String sourceIP = packet.SOURCE_IP;
        String destinationIP = packet.DESTINATION_IP;
        return ((whiteList.isEmpty() || whiteList.contains(sourceIP) || whiteList.contains(destinationIP))
                && (!blackList.contains(sourceIP) && !blackList.contains(destinationIP)));
    }

    @Override
    public String getName() {
        return "IP Filter";
    }
}
