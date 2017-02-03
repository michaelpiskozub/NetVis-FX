package netvis.model.normalizers;

import netvis.model.Normalizer;
import netvis.model.Packet;
import netvis.model.DataUtilities;

public class SourceMACNormalizer extends Normalizer {
    @Override
    public double normalizeFunction(Packet p) {
        if (DataUtilities.MAC_MAP.containsKey(p.SOURCE_MAC)) {
            return DataUtilities.MAC_MAP.get(p.SOURCE_MAC);
        } else {
            double value = DataUtilities.normalizeIP(p.SOURCE_IP);
            value += (Math.random() / 100);
            if (value < 0) {
                value = -value;
            }
            if (value > 1) {
                value = 2 - value;
            }
            DataUtilities.MAC_MAP.put(p.SOURCE_MAC, value);
            return value;
        }
    }

    @Override
    public String denormalizeFunction(double v) {
        return String.valueOf(v);
    }

    @Override
    public String getName() {
        return "Source MAC";
    }
}
