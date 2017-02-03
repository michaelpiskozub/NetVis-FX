package netvis.view.util.fx;

import javafx.scene.paint.Color;

public class ColorPair {
    private Color diffuseColor;
    private Color specularColor;

    public ColorPair(Color diffuseColor, Color specularColor) {
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public Color getSpecularColor() {
        return specularColor;
    }
}
