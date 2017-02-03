package netvis.view.util.jogl;

import javax.media.opengl.GL2;
import java.awt.*;

public class ColorPalette {
    // Static constants to be used on initiation
    public static final int SCHEME_QUALITATIVE = 0;
    public static final int SCHEME_SEQUENTIAL = 1;
    public static final int SCHEME_DIVERGENT = 2;

    protected final int SCHEME;
    protected int currentIndex = 0;

    /**
     * A color palette to provide colors for data visualisation.
     *
     * @param colorScheme
     *            The color scheme to use. Please provide a ColorPalette
     *            constant.
     */
    public ColorPalette(int colorScheme) {
        SCHEME = colorScheme;
    }

    /**
     * Calculates the color which is a mix of the two provided, according to a
     * given ratio.
     *
     * @param color1
     *            This color will become more apparent with higher ratios
     * @param color2
     *            This color will become more apparent with lower ratios
     * @param ratio
     *            The ratio between components of colors 1 and 2
     * @return The mixed color according to the specified parameters
     */
    public static Color getColorShade(Color color1, Color color2, double ratio) {
        assert (ratio >= 0 && ratio <= 1);

        int red = (int) Math.round(color1.getRed() * ratio + color2.getRed() * (1 - ratio));
        int green = (int) Math.round(color1.getGreen() * ratio + color2.getGreen() * (1 - ratio));
        int blue = (int) Math.round(color1.getBlue() * ratio + color2.getBlue() * (1 - ratio));

        return new Color(red, green, blue);
    }

    // Define colors to be picked from (Qualitative, Sequential, Divergent)
    public static final Color[][] colorSchemes = {
            {
                    new Color(141, 211, 199), new Color(255, 255, 179), new Color(190, 186, 218),
                    new Color(251, 128, 114), new Color(128, 177, 211), new Color(253, 180, 98),
                    new Color(179, 222, 105), new Color(252, 205, 229), new Color(217, 217, 217),
                    new Color(188, 128, 189), new Color(204, 235, 197), new Color(255, 237, 111) },
            { new Color(255, 255, 204), new Color(255, 237, 160), new Color(254, 217, 118),
                    new Color(254, 178, 76), new Color(253, 141, 60), new Color(252, 78, 42),
                    new Color(227, 26, 28), new Color(189, 0, 38), new Color(128, 0, 38) },
            { new Color(178, 24, 43), new Color(214, 96, 77), new Color(244, 165, 130),
                    new Color(253, 219, 199), new Color(247, 247, 247), new Color(209, 229, 240),
                    new Color(146, 197, 222), new Color(67, 147, 195), new Color(33, 102, 172) }};

    public Color getNextColor() {
        Color thisColor = colorSchemes[SCHEME][currentIndex];
        currentIndex = (currentIndex + 1) % colorSchemes[SCHEME].length;
        return thisColor;
    }

    public static void setColor(GL2 gl, Color color) {
        gl.glColor3d(normalise(color.getRed()), normalise(color.getGreen()),
                normalise(color.getBlue()));
    }

    public static void setColor(GL2 gl, Color color, double transparency){
        gl.glColor4d(normalise(color.getRed()), normalise(color.getGreen()),
                normalise(color.getBlue()), transparency);
    }

    public void setNextColor(GL2 gl) {
        setColor(gl, getNextColor());
    }

    protected static double normalise(int x) {
        return (x / 256.0);
    }
}
