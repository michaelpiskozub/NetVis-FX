package netvis.model.input.tshark;

import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.ArrayList;

public class TSharkUtilities {
    private StringProperty tsharkPathProperty;
    private StringProperty captureInterfaceProperty;

    public TSharkUtilities(StringProperty tsharkPathProperty, StringProperty captureInterfaceProperty) {
        this.tsharkPathProperty = tsharkPathProperty;
        this.captureInterfaceProperty = captureInterfaceProperty;
    }

    public ArrayList<String> initializeRealTimeCommand() {
        ArrayList<String> realTimeCommand = new ArrayList<>();
        realTimeCommand.add(tsharkPathProperty.get());
        realTimeCommand.add("-i");
        realTimeCommand.add(captureInterfaceProperty.get());
        realTimeCommand.add("-l");
        realTimeCommand.add("-T");
        realTimeCommand.add("fields");
        realTimeCommand.add("-E");
        realTimeCommand.add("separator=,");
        realTimeCommand.add("-E");
        realTimeCommand.add("quote=d");
        realTimeCommand.add("-e");
        realTimeCommand.add("frame.number");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.Time");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.SourceIP");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.SourceMAC");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.SourcePort");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.DestinationIP");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.DestinationMAC");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.DestinationPort");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.Protocol");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.Length");
        realTimeCommand.add("-e");
        realTimeCommand.add("_ws.col.Info");

        return realTimeCommand;
    }

    public ArrayList<String> initializePCAPCommand(File file) {
        ArrayList<String> pcapCommand = new ArrayList<>();
        pcapCommand.add(tsharkPathProperty.get());
        pcapCommand.add("-r");
        pcapCommand.add(file.getAbsolutePath());
        pcapCommand.add("-T");
        pcapCommand.add("fields");
        pcapCommand.add("-E");
        pcapCommand.add("separator=,");
        pcapCommand.add("-E");
        pcapCommand.add("quote=d");
        pcapCommand.add("-e");
        pcapCommand.add("frame.number");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.Time");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.SourceIP");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.SourceMAC");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.SourcePort");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.DestinationIP");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.DestinationMAC");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.DestinationPort");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.Protocol");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.Length");
        pcapCommand.add("-e");
        pcapCommand.add("_ws.col.Info");

        return pcapCommand;
    }


}
