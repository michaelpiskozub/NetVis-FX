package netvis.model.input;

import netvis.model.Packet;

public class PacketCaptureUtilities {
    public static Packet lineToPacket(String[] line) {
        int no = parseInt(line[0]);
        double time = parseFloat(line[1]);
        int sport;
        if (line[4].equals("")) {
            sport = 0;
        } else {
            sport = parseInt(line[4]);
        }
        int dport;
        if (line[7].equals("")) {
            dport = 0;
        } else {
            dport = parseInt(line[7]);
        }
        int length = parseInt(line[9]);
        return new Packet(no, time, line[2], line[3], sport, line[5], line[6],
                dport, line[8], length, line[10]);
    }

    private static Integer parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private static Float parseFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException nfe) {
            return 0f;
        }
    }
}
