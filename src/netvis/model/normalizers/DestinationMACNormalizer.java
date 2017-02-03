package netvis.model.normalizers;

import netvis.model.Normalizer;
import netvis.model.Packet;
import netvis.model.DataUtilities;

public class DestinationMACNormalizer extends Normalizer {
    @Override
    public double normalizeFunction(Packet p) {
        return DataUtilities.normalizeMAC(p.DESTINATION_MAC);
    }

    @Override
    public String denormalizeFunction(double v) {
        return String.valueOf(v);
    }

    @Override
    public String getName() {
        return "Destination MAC";
    }
}
