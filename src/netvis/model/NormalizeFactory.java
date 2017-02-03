package netvis.model;

import netvis.model.normalizers.*;

import java.util.*;

public class NormalizeFactory {
    public static final NormalizeFactory INSTANCE = new NormalizeFactory();
    private Map<String, Normalizer> normalizers;
    private DataController dataController;

    private NormalizeFactory() {
        normalizers = new LinkedHashMap<>();

        SourceMACNormalizer sourceMACNormalizer = new SourceMACNormalizer();
        SourceIPNormalizer sourceIPNormalizer = new SourceIPNormalizer();
        SourcePortNormalizer sourcePortNormalizer = new SourcePortNormalizer();
        DestinationPortNormalizer destinationPortNormalizer = new DestinationPortNormalizer();
        DestinationIPNormalizer destinationIPNormalizer = new DestinationIPNormalizer();
        DestinationMACNormalizer destinationMACNormalizer = new DestinationMACNormalizer();
        ProtocolNormalizer protocolNormalizer = new ProtocolNormalizer();

        normalizers.put(sourceMACNormalizer.getName(), sourceMACNormalizer);
        normalizers.put(sourceIPNormalizer.getName(), sourceIPNormalizer);
        normalizers.put(sourcePortNormalizer.getName(), sourcePortNormalizer);
        normalizers.put(destinationPortNormalizer.getName(), destinationPortNormalizer);
        normalizers.put(destinationIPNormalizer.getName(), destinationIPNormalizer);
        normalizers.put(destinationMACNormalizer.getName(), destinationMACNormalizer);
        normalizers.put(protocolNormalizer.getName(), protocolNormalizer);
    }

    public Set<String> getNormalizerNames() {
        return normalizers.keySet();
    }

    public List<Normalizer> getNormalizers() {
        return new ArrayList<>(normalizers.values());
    }

    public Normalizer getNormalizer(String name) {
        return normalizers.get(name);
    }

    public void setDataController(DataController dataController) {
        this.dataController = dataController;
    }
}
