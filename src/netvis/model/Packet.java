package netvis.model;

public class Packet {
    public final int PACKET_NUMBER;
    public final double TIME;
    public final String SOURCE_IP;
    public final String SOURCE_MAC;
    public final int SOURCE_PORT;
    public final String DESTINATION_IP;
    public final String DESTINATION_MAC;
    public final int DESTINATION_PORT;
    public final String PROTOCOL;
    public final int LENGTH;
    public final String INFO;

    public Packet(int packetNumber, double time, String sourceIP, String sourceMAC, int sourcePort,
                  String destinationIP, String destinationMAC, int destinationPort, String protocol, int length,
                  String info) {
        PACKET_NUMBER = packetNumber;
        TIME = time;
        SOURCE_IP = sourceIP;
        SOURCE_MAC = sourceMAC;
        SOURCE_PORT = sourcePort;
        DESTINATION_IP = destinationIP;
        DESTINATION_MAC = destinationMAC;
        DESTINATION_PORT = destinationPort;
        PROTOCOL = protocol;
        LENGTH = length;
        INFO = info;
    }

    @Override
    public String toString() {
        return "{" + PACKET_NUMBER + ", " + TIME + ", " + SOURCE_IP + ", " + SOURCE_MAC + ", " + SOURCE_PORT + ", " +
                DESTINATION_IP + ", " + DESTINATION_MAC + ", " + DESTINATION_PORT + ", " + PROTOCOL + ", " + LENGTH +
                ", " + INFO + "}";
    }

    public int getTimeMillis() {
        return (int) Math.floor((TIME - Math.floor(TIME)) * 1000);
    }
}
