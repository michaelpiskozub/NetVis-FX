package netvis.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import netvis.model.Packet;
import netvis.ui.controlsfx.Borders;
import netvis.ui.controlsfx.PopOver;

import java.util.ArrayList;
import java.util.List;

public class PacketsPopOver extends PopOver {
    private List<Packet> packets;
    private FlowPane flowPane;
    private Font font;
    private VBox vbox;

    public PacketsPopOver() {
        setArrowSize(0);
        setCornerRadius(5);
        setDetachedTitle("Packets");

        packets = new ArrayList<>();
        font = Font.loadFont(getClass().getResourceAsStream("/Helvetica CE Bold.ttf"), 12);
        flowPane = new FlowPane();
        flowPane.setPrefWrapLength(650);
        flowPane.setPrefSize(625, 600);
        flowPane.setPadding(new Insets(10, 10, 10, 10));

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> reset());
        vbox = new VBox();
        ScrollPane scroll = new ScrollPane(flowPane);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        vbox.setMargin(clearButton, new Insets(0, 0, 10, 20));
        vbox.getChildren().addAll(clearButton, scroll);

        setContentNode(vbox);
    }

    public void reset() {
        flowPane.getChildren().clear();
        packets.clear();
    }

    public void addPacketText(Packet packet) {
        if (!packets.contains(packet)) {
            VBox packetTextBox = new VBox();
            packetTextBox.setPrefSize(250, 100);

            Label packetNumberLabel = new Label(packet.PACKET_NUMBER + "");
            packetNumberLabel.setFont(font);
            Label timeLabel = new Label("Time: " + packet.TIME);
            Label sourceIPLabel = new Label("Source IP: " + packet.SOURCE_IP);
            Label sourceMACLabel = new Label("Source MAC: " + packet.SOURCE_MAC);
            Label sourcePortLabel = new Label("Source Port: " + packet.SOURCE_PORT);
            Label destinationIPLabel = new Label("Destination IP: " + packet.DESTINATION_IP);
            Label destinationMACLabel = new Label("Destination MAC: " + packet.DESTINATION_MAC);
            Label destinationPortLabel = new Label("Destination Port: " + packet.DESTINATION_PORT);
            Label protocolLabel = new Label("Protocol: " + packet.PROTOCOL);
            Label lengthLabel = new Label("Length: " + packet.LENGTH);
            Text infoText = new Text("Info: " + packet.INFO);
            infoText.setWrappingWidth(220);

            packetTextBox.getChildren().addAll(packetNumberLabel, timeLabel, sourceIPLabel, sourceMACLabel,
                    sourcePortLabel, destinationIPLabel, destinationMACLabel, destinationPortLabel, protocolLabel,
                    lengthLabel, infoText);
            Node wrappedBox = Borders.wrap(packetTextBox).lineBorder().thickness(1).radius(5).build().build();
            flowPane.getChildren().addAll(wrappedBox);
            packets.add(packet);
        }
    }
}
