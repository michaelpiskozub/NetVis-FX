package netvis.model.normalizers;

import netvis.model.Normalizer;
import netvis.model.Packet;
import netvis.model.DataUtilities;

public class DestinationIPNormalizer extends Normalizer {
    @Override
    public double normalizeFunction(Packet p) {
        return DataUtilities.normalizeIP(p.DESTINATION_IP);
    }

    @Override
    public String denormalizeFunction(double v) {
        return DataUtilities.denormalizeIP(v);
    }

    @Override
    public String getName() {
        return "Destination IP";
    }
}
