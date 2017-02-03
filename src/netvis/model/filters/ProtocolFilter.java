package netvis.model.filters;

        import netvis.model.Packet;
        import netvis.model.PacketFilter;

        import java.util.List;

public class ProtocolFilter implements PacketFilter {
    private List<String> whiteList;
    private List<String> blackList;

    public ProtocolFilter(List<String> whiteList, List<String> blackList) {
        this.whiteList = whiteList;
        this.blackList = blackList;
    }

    @Override
    public boolean isFilterTestPassed(Packet packet) {
        String protocol = packet.PROTOCOL;
        return ((whiteList.isEmpty() || whiteList.contains(protocol)) && (!blackList.contains(protocol)));
    }

    @Override
    public String getName() {
        return "ProtocolFilter";
    }
}
