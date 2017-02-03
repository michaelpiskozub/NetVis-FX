package netvis.model;

public abstract class Normalizer {
    private double lowerBound = 0;
    private double upperBound = 1;

    public abstract double normalizeFunction(Packet p);
    public abstract String denormalizeFunction(double v);
    public abstract String getName();

    public double normalize(Packet p) {
        double v = this.normalizeFunction(p);
        v = (v - lowerBound) / (upperBound - lowerBound);
        return v;
    }

    public String denormalize(double v) {
        v = v * (upperBound - lowerBound);
        v += lowerBound;
        return denormalizeFunction(v);
    }
}
