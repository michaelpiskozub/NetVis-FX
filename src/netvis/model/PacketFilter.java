package netvis.model;

public interface PacketFilter {
    boolean isFilterTestPassed(Packet packet);
    String getName();
}
