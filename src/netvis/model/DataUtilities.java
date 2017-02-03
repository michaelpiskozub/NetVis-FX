package netvis.model;

import java.util.HashMap;
import java.util.Random;

public class DataUtilities {
    public static final int MIN_PORT = 0;
    public static final int MAX_PORT = 65535;
    public static final HashMap<String, Double> MAC_MAP = new HashMap<>();
    public static final HashMap<String, Double> PROTOCOL_MAP = new HashMap<>();
    private static Random doubleGen = new Random();

    public static double normalizeIP(String ip) {
        int i = 0;
        int c = 0;
        double f = 0;
        while (ip.length() > i) {
            if (ip.charAt(i) < '0' || ip.charAt(i) > '9') {
                return 0;
            }
            c = c * 10 + (ip.charAt(i) - '0');
            i++;
            if (ip.length() == i || ip.charAt(i) == '.') {
                f = f * 256 + c;
                c = 0;
                i++;
            }
        }
        f = f / (256 * 256);
        f = f / (256 * 256);
        return f;
    }

    public static String denormalizeIP(double nv) {
        nv = nv * 256 * 256 * 256 * 256;
        long nvi = (long) nv;
        if (nvi == (long) 65536*65536) {
            nvi -= 1;
        }
        String s = "";
        for (int i = 0; i < 4; i++) {
            s = String.valueOf(nvi % 256) + s;
            nvi /= 256;
            if (i != 3) {
                s = '.' + s;
            }
        }
        return s;
    }

    public static double normalizePort(int port) {
        return (double) port / MAX_PORT;
    }

    public static String denormalizePort(double v) {
        return String.valueOf((int) (v * MAX_PORT));
    }

    public static double normalizeMAC(String address) {
        if (MAC_MAP.containsKey(address)) {
            return MAC_MAP.get(address);
        } else {
            double value = 0.5;
            while (MAC_MAP.containsValue(value)) {
                value = doubleGen.nextDouble();
            }
            MAC_MAP.put(address, value);
            return value;
        }
    }

    public static double normalizeProtocol(String protocol) {
        if (PROTOCOL_MAP.containsKey(protocol)) {
            return PROTOCOL_MAP.get(protocol);
        } else {
            double value = 0.5;
            while (PROTOCOL_MAP.containsValue(value)) {
                value = doubleGen.nextDouble();
            }
            PROTOCOL_MAP.put(protocol, value);
            return value;
        }
    }
}
