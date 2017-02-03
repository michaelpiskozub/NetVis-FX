package netvis.view.util.fx;

import javafx.scene.shape.Box;
import netvis.model.Packet;

public class PacketBox extends Box {
    private Packet packet;

    public PacketBox(Packet packet, double width, double height, double depth) {
        super(width, height, depth);

        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }
}
