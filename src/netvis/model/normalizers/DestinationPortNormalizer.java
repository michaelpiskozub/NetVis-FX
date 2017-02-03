package netvis.model.normalizers;

import netvis.model.Normalizer;
import netvis.model.Packet;
import netvis.model.DataUtilities;

public class DestinationPortNormalizer extends Normalizer {
    @Override
    public double normalizeFunction(Packet p) {
        return DataUtilities.normalizePort(p.DESTINATION_PORT);
    }

    @Override
    public String denormalizeFunction(double v) {
        return DataUtilities.denormalizePort(v);
    }

    @Override
    public String getName() {
        return "Destination Port";
    }
}
