package netvis.model.normalizers;

import netvis.model.Normalizer;
import netvis.model.Packet;
import netvis.model.DataUtilities;

public class ProtocolNormalizer extends Normalizer {
    @Override
    public double normalizeFunction(Packet p) {
        return DataUtilities.normalizeProtocol(p.PROTOCOL);
    }

    @Override
    public String denormalizeFunction(double v) {
        return String.valueOf(v);
    }

    @Override
    public String getName() {
        return "Protocol";
    }
}
