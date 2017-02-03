package netvis.view.util.fx;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ColorGenerator {
    public static final int COLLECTION_WINDOWS_8 = 0;
    public static final int COLLECTION_DESIGNER = 1;
    public static final int COLLECTION_UNIFORM_HUE = 2;
    public static final int COLLECTION_RANDOM_HUE = 3;
    public static final int COLLECTION_FEW_BRIGHT = 4;
    public static final int COLLECTION_ONE_RED = 5;
    public static final int ORDER_NORMAL = 0;
    public static final int ORDER_RANDOM = 1;
    private List<ColorPair> colors;
    private final int NUMBER_OF_COLORS = 40;
    private int colorIndex;
    private boolean shouldShuffle;

    public ColorGenerator(int collectionType, int orderType) {
        colors = new ArrayList<>();
        colorIndex = 0;

        switch (orderType) {
            case 0:
                shouldShuffle = false;
                break;
            case 1:
                shouldShuffle = true;
                break;
        }

        switch (collectionType) {
            case 0:
                addWindows8Colors();
                prepareColorsLarge();
                break;
            case 1:
                addDesignerColors();
                prepareColorsLarge();
                break;
            case 2:
                addUniformHueColors();
                prepareColorsLarge();
                break;
            case 3:
                addRandomHueColors();
                prepareColorsLarge();
                break;
            case 4:
                addFewBrightColors();
                prepareColorsSmall();
                break;
            case 5:
                addOneRedColor();
                break;
        }

        prepareColorsLarge();
    }

    private void prepareColorsLarge() {
        if (shouldShuffle) {
            Collections.shuffle(colors, new Random(System.nanoTime()));
        }
        List<ColorPair> mirroredColors = new ArrayList<>(colors);
        if (shouldShuffle) {
            Collections.shuffle(mirroredColors, new Random(System.nanoTime()));
        }
        colors.addAll(mirroredColors);
    }

    private void prepareColorsSmall() {
        List<ColorPair> mirroredColors1 = new ArrayList<>(colors);
        if (shouldShuffle) {
            Collections.shuffle(colors, new Random(System.nanoTime()));
        }
        List<ColorPair> mirroredColors2 = new ArrayList<>(colors);
        List<ColorPair> mirroredColors3 = new ArrayList<>(colors);
        if (shouldShuffle) {
            Collections.shuffle(mirroredColors1, new Random(System.nanoTime()));
            Collections.shuffle(mirroredColors2, new Random(System.nanoTime()));
            Collections.shuffle(mirroredColors3, new Random(System.nanoTime()));
        }
        colors.addAll(mirroredColors1);
        colors.addAll(mirroredColors2);
        colors.addAll(mirroredColors3);
    }

    private void addWindows8Colors() {
        colors.add(new ColorPair(Color.hsb(70, 1.0, 0.77), Color.hsb(70, 1.0, 0.82)));
        colors.add(new ColorPair(Color.hsb(90, 0.86, 0.66), Color.hsb(90, 0.86, 0.71)));
        colors.add(new ColorPair(Color.hsb(120, 1.0, 0.54), Color.hsb(120, 1.0, 0.59)));
        colors.add(new ColorPair(Color.hsb(179, 1.0, 0.67), Color.hsb(179, 1.0, 0.72)));
        colors.add(new ColorPair(Color.hsb(200, 0.88, 0.89), Color.hsb(200, 0.88, 0.94)));
        colors.add(new ColorPair(Color.hsb(220, 1.0, 0.94), Color.hsb(220, 1.0, 0.99)));
        colors.add(new ColorPair(Color.hsb(265, 1.0, 0.95), Color.hsb(265, 1.0, 1.0)));
        colors.add(new ColorPair(Color.hsb(280, 1.0, 0.95), Color.hsb(280, 1.0, 1.0)));
        colors.add(new ColorPair(Color.hsb(317, 0.53, 0.95), Color.hsb(317, 0.53, 1.0)));
        colors.add(new ColorPair(Color.hsb(328, 1.0, 0.85), Color.hsb(328, 1.0, 0.9)));
        colors.add(new ColorPair(Color.hsb(346, 1.0, 0.64), Color.hsb(346, 1.0, 0.69)));
        colors.add(new ColorPair(Color.hsb(5, 1.0, 0.9), Color.hsb(5, 1.0, 0.95)));
        colors.add(new ColorPair(Color.hsb(25, 1.0, 0.95), Color.hsb(25, 1.0, 1.0)));
        colors.add(new ColorPair(Color.hsb(40, 0.96, 0.94), Color.hsb(40, 0.96, 0.99)));
        colors.add(new ColorPair(Color.hsb(53, 1.0, 0.89), Color.hsb(53, 1.0, 0.94)));
        colors.add(new ColorPair(Color.hsb(32, 0.66, 0.51), Color.hsb(32, 0.66, 0.56)));
        colors.add(new ColorPair(Color.hsb(105, 0.26, 0.53), Color.hsb(105, 0.26, 0.58)));
        colors.add(new ColorPair(Color.hsb(209, 0.26, 0.53), Color.hsb(209, 0.26, 0.58)));
        colors.add(new ColorPair(Color.hsb(271, 0.3, 0.54), Color.hsb(271, 0.3, 0.59)));
        colors.add(new ColorPair(Color.hsb(45, 0.42, 0.53), Color.hsb(45, 0.42, 0.58)));
    }

    private void addDesignerColors() {
        colors.add(new ColorPair(Color.hsb(206, 0.63, 0.54), Color.hsb(206, 0.63, 0.59)));
        colors.add(new ColorPair(Color.hsb(64, 0.49, 0.95), Color.hsb(64, 0.49, 1.0)));
        colors.add(new ColorPair(Color.hsb(150, 0.57, 0.87), Color.hsb(150, 0.57, 0.92)));
        colors.add(new ColorPair(Color.hsb(29, 0.63, 0.95), Color.hsb(29, 0.63, 1.0)));
        colors.add(new ColorPair(Color.hsb(27, 0.3, 0.59), Color.hsb(27, 0.3, 0.64)));
        colors.add(new ColorPair(Color.hsb(99, 0.8, 0.9), Color.hsb(99, 0.8, 0.95)));
        colors.add(new ColorPair(Color.hsb(3, 0.8, 0.9), Color.hsb(3, 0.8, 0.95)));
        colors.add(new ColorPair(Color.hsb(325, 0.8, 0.9), Color.hsb(325, 0.8, 0.95)));
        colors.add(new ColorPair(Color.hsb(180, 0.6, 0.9), Color.hsb(180, 0.6, 0.95)));
        colors.add(new ColorPair(Color.hsb(47, 0.94, 0.95), Color.hsb(47, 0.94, 1.0)));
        colors.add(new ColorPair(Color.hsb(358, 0.6, 0.9), Color.hsb(358, 0.6, 0.95)));
        colors.add(new ColorPair(Color.hsb(291, 0.44, 0.37), Color.hsb(291, 0.44, 0.42)));
        colors.add(new ColorPair(Color.hsb(199, 0.84, 0.71), Color.hsb(199, 0.84, 0.76)));
        colors.add(new ColorPair(Color.hsb(343, 0.7, 0.95), Color.hsb(343, 0.7, 1.0)));
        colors.add(new ColorPair(Color.hsb(84, 0.31, 0.95), Color.hsb(84, 0.31, 1.0)));
        colors.add(new ColorPair(Color.hsb(220, 0.01, 0.93), Color.hsb(220, 0.01, 0.98)));
        colors.add(new ColorPair(Color.hsb(349, 0.92, 0.69), Color.hsb(349, 0.92, 0.74)));
        colors.add(new ColorPair(Color.hsb(145, 0.78, 0.95), Color.hsb(145, 0.78, 1.0)));
        colors.add(new ColorPair(Color.hsb(228, 0.75, 0.82), Color.hsb(228, 0.75, 0.87)));
        colors.add(new ColorPair(Color.hsb(197, 1.0, 0.95), Color.hsb(197, 1.0, 1.0)));
    }

    private void addUniformHueColors() {
        for (int i = 0; i < NUMBER_OF_COLORS / 2; i++) {
            double iNormalized = i * (1.0 / (NUMBER_OF_COLORS / 2)) * 360.0;

            double diffuseBrightness = 0.95;
            double specularBrightness = 1.0;

            colors.add(new ColorPair(
                    Color.hsb(iNormalized, 0.9, diffuseBrightness),
                    Color.hsb(iNormalized, 0.9, specularBrightness))
            );
        }
    }

    private void addRandomHueColors() {
        Random random = new Random(System.currentTimeMillis());
        final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;
        for (int i = 0; i < NUMBER_OF_COLORS / 2; i++) {
            double hue = random.nextDouble() + GOLDEN_RATIO_CONJUGATE;
            hue %= 1;
            hue *= 360.0;

            double diffuseBrightness = 0.95;
            double specularBrightness = 1.0;

            colors.add(new ColorPair(Color.hsb(hue, 0.9, diffuseBrightness), Color.hsb(hue, 0.9, specularBrightness)));
        }
    }

    private void addFewBrightColors() {
        colors.add(new ColorPair(Color.hsb(0, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(118, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(60, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(205, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(0, 0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(39, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(153, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(172, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(240, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
        colors.add(new ColorPair(Color.hsb(320, 1.0, 1.0), Color.hsb(180, 0.06, 1.0)));
    }

    private void addOneRedColor() {
        for (int i = 0; i < 40; i++) {
            colors.add(new ColorPair(Color.RED, Color.ORANGE));
        }
    }

    public ColorPair getNextColor() {
        return colors.get(colorIndex++);
    }

    public ColorPair getColor(int index) {
        return colors.get(index);
    }

    public int getIndexOfColor(Color color) {
        return colors.lastIndexOf(color);
    }
}
