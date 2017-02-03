package netvis.view.util.fx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Planet extends Sphere {
    private String name;
    private ColorPair colors;
    private PhongMaterial material;
    private IntegerProperty hits;
    private IntegerProperty totalLength;
    private String type;

    public Planet(double radius, ColorPair colors) {
        super(radius);

        this.colors = colors;
        material = new PhongMaterial();
        material.setDiffuseColor(colors.getDiffuseColor());
        material.setSpecularColor(colors.getSpecularColor());
        setMaterial(material);
        hits = new SimpleIntegerProperty(0);
        totalLength = new SimpleIntegerProperty(0);
        type = "";
    }

    public Planet(double radius, ColorPair colors, String type) {
        this(radius, colors);
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getDiffuseColor() {
        return colors.getDiffuseColor();
    }

    public Color getSpecularColor() {
        return colors.getSpecularColor();
    }

    public ColorPair getColorPair() {
        return colors;
    }

    public int getHits() {
        return hits.get();
    }

    public void setHits(int hits) {
        this.hits.set(hits);
    }

    public void increaseHitsBy(int inc) {
        hits.set(hits.get() + inc);
    }

    public IntegerProperty getHitsProperty() {
        return hits;
    }

    public int getTotalLength() {
        return totalLength.get();
    }

    public void setTotalLength(int totalLength) {
        this.totalLength.set(totalLength);
    }

    public void increaseTotalLengthBy(int inc) {
        totalLength.set(totalLength.get() + inc);
    }

    public IntegerProperty getTotalLengthProperty() {
        return totalLength;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
