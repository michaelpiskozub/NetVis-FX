package netvis.view.visualizations.jogl;

import com.jogamp.opengl.util.gl2.GLUT;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import netvis.model.DataController;
import netvis.model.NormalizeFactory;
import netvis.model.Normalizer;
import netvis.ui.controlsfx.Borders;
import netvis.view.VisualizationJOGL;
import netvis.view.util.jogl.ColorPalette;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.*;
import java.awt.*;

public class AttributeDistributionVisualization extends VisualizationJOGL {
    private int logFactor;
    private int resolution;
    private Normalizer normalizer;
    private int[] packetCount;
    private int[] packetCountAnimated;
    private boolean displaySelectionBar = false;
    private boolean mouseClicked = false;
    private int intervalHighlighted;
    private int pastLimit;
    private int startIntervalHighlight;
    private int endIntervalHighlight;
    Color graphColor;
    JComboBox<String> normaliserBox;

    public AttributeDistributionVisualization(DataController dataController) {
        super(dataController, 30);

        logFactor = 6;
        resolution = 100;
        normalizer = NormalizeFactory.INSTANCE.getNormalizer("Source MAC");
        packetCount = new int[resolution + 1];
        packetCountAnimated = new int[resolution + 1];
        intervalHighlighted = 0;
        pastLimit = 30;
        startIntervalHighlight = 0;
        endIntervalHighlight = 0;
        graphColor = new Color(23, 123, 185);
        controls = initializeControls();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glScaled(0.95, 1, 1);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

	    /*
	     * Draw the white background
	     */
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3d(0.95, 0.95, 0.95);
        gl.glVertex2d(-1,-0.8);
        gl.glVertex2d(-1,0.8);
        gl.glVertex2d(1,0.8);
        gl.glVertex2d(1,-0.8);
        gl.glEnd();

        gl.glLineWidth(1);
        gl.glColor3d(0.6, 0.6, 0.6);
        final GLUT glut = new GLUT();
        for (int i = 0; i < 16; i++) {
            gl.glBegin(GL2.GL_LINES);
            double height = (double)i/10 - 0.8;
            gl.glVertex2d(-1 , height);
            gl.glVertex2d(1, height);
            gl.glEnd();

            gl.glRasterPos2d(0, height + 0.02); // set position
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, String.valueOf((int)Math.exp((height+0.8)*logFactor)));

            gl.glRasterPos2d(-0.9, height + 0.02); // set position
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, String.valueOf((int)Math.exp((height+0.8)*logFactor)));
            gl.glRasterPos2d(0.7, height + 0.02); // set position
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, String.valueOf((int)Math.exp((height+0.8)*logFactor)));
        }
        int ii = -1;
        if (LIST_OF_PACKETS.size() > 0) {
            double searchTime = LIST_OF_PACKETS.get(LIST_OF_PACKETS.size()-1).TIME - pastLimit;
            ii = LIST_OF_PACKETS.size() -1;
            while (ii > 0 && LIST_OF_PACKETS.get(ii).TIME > searchTime) ii--;
        }
        if (ii < -1) {
            ii = -1;
        }
        ii++;

        for (int i = 0; i < resolution; i++) {
            packetCount[i] = 0;
        }
        for (int i = ii; i < LIST_OF_PACKETS.size(); i++) {
            int pc = (int) (normalizer.normalize(LIST_OF_PACKETS.get(i))*(double)resolution);
            if (pc >= 0 && pc < resolution) {
                packetCount[pc]++;
            }
        }
        for (int i = 0; i < resolution; i++) {
            packetCountAnimated[i] = packetCount[i] - (packetCount[i] - packetCountAnimated[i])*2/3;
        }
        double currentLog;
        double lastLog;
        currentLog = Math.max(Math.log(packetCountAnimated[0])/logFactor, 0);
        gl.glLineWidth(3);
        for (int i = 1; i <= resolution; i++) {
            lastLog = currentLog;
            currentLog = Math.max(Math.log(packetCountAnimated[i])/logFactor, 0);
            if (packetCountAnimated[i] == 1) {
                currentLog = 0.5 / logFactor;
            }
            gl.glBegin(GL2.GL_POLYGON);
            gl.glColor3d(0.9, 0.9, 0.9);
            gl.glVertex2d(-1 + 2*((double)(i-1)/resolution),-0.8 );
            gl.glVertex2d(-1 + 2*((double)(i-1)/resolution),-0.8 + lastLog);
            gl.glVertex2d(-1 + 2*((double)i/resolution),-0.8 + currentLog);
            gl.glVertex2d(-1 + 2*((double)i/resolution),-0.8);
            gl.glEnd();
            ColorPalette.setColor(gl,
                    ColorPalette.getColorShade(Color.red, graphColor, (currentLog + lastLog) / 4));
            gl.glBegin(GL2.GL_LINE_STRIP);
            gl.glVertex2d(-1 + 2*((double)(i-1)/resolution),-0.8 + lastLog);
            gl.glVertex2d(-1 + 2*((double)i/resolution),-0.8 + currentLog);
            gl.glEnd();
        }
        for (int i = 0; i < resolution; i++) {
            if (packetCountAnimated[i] > 0) {
                currentLog = Math.max(Math.log(packetCountAnimated[i])/logFactor, 0);
                ColorPalette.setColor(gl,
                        ColorPalette.getColorShade(Color.red, graphColor, currentLog/2));

                double numPoints = Math.log(packetCountAnimated[i]*10);
                double c = Math.sqrt((double)packetCountAnimated[i]/(numPoints*numPoints*numPoints))*3;
                for(int j = 0; j < numPoints; j++) {
                    gl.glPointSize((float) ((float)(numPoints - j)*c));
                    gl.glBegin(GL2.GL_POINTS);

                    gl.glVertex2d(-1 + 2*((double)i/resolution) , -0.75 + (double)j*currentLog/(numPoints*2));
                    gl.glEnd();
                }
            }
        }
        gl.glColor3d(1, 1, 1);
        int lowerBarRes = 5;
        float lastOffset = 0;
        gl.glLineWidth(1);
        for (int i = 0; i < lowerBarRes; i++) {
            gl.glBegin(GL2.GL_LINE_STRIP);
            float xAxis = (float)i*2/(lowerBarRes - 1);
            gl.glVertex2f(-1+xAxis, -0.8f);
            gl.glVertex2f(-1+xAxis, -0.9f);
            gl.glEnd();
            if (i == lowerBarRes - 1) {
                lastOffset = 0.14f;
            }
            gl.glRasterPos2f(-0.98f + xAxis -lastOffset, -0.87f); // set
            // position
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, normalizer.denormalize((double) i / (lowerBarRes - 1)));

        }
        if (displaySelectionBar) {
            gl.glColor4d(1, 1, 1, 0.5);
            gl.glLineWidth(4);
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex2d(-1.01 + 2*((double)intervalHighlighted/resolution) , -0.8);
            gl.glVertex2d(-1.01 + 2*((double)(intervalHighlighted+1)/resolution), -0.8);
            gl.glVertex2d(-1.01 + 2*((double)(intervalHighlighted+1)/resolution), 0.8);
            gl.glVertex2d(-1.01 + 2*((double)intervalHighlighted/resolution) , 0.8);

            gl.glEnd();
        }
        if (mouseClicked) {
            gl.glColor4d(1, 1, 1, 0.4);
            gl.glLineWidth(4);
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex2d(-1.01 + 2*((double)startIntervalHighlight/resolution) , -0.8);
            gl.glVertex2d(-1.01 + 2*((double)(endIntervalHighlight+1)/resolution), -0.8);
            gl.glVertex2d(-1.01 + 2*((double)(endIntervalHighlight+1)/resolution), 0.8);
            gl.glVertex2d(-1.01 + 2*((double)startIntervalHighlight/resolution) , 0.8);

            gl.glEnd();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    @Override
    protected Node initializeControls() {
        VBox vbox = new VBox(10);
        Slider timeFilter = new Slider(1, 20, 1);
        timeFilter.setMajorTickUnit(4);
        timeFilter.setMinorTickCount(3);
        timeFilter.setShowTickMarks(true);
        timeFilter.setShowTickLabels(true);
        timeFilter.setSnapToTicks(true);
        timeFilter.valueChangingProperty().addListener((observable, wasChanging, changing) -> {
            if (!changing) {
                pastLimit = (int) (timeFilter.getValue() * 30);
                if (((int) Math.round(timeFilter.getValue())) == 20) {
                    pastLimit = 1000;
                }
            }
        });
        ComboBox<String> normalizerComboBox = new ComboBox<>();
        normalizerComboBox.getItems().addAll(NormalizeFactory.INSTANCE.getNormalizerNames());
        normalizerComboBox.getSelectionModel().selectFirst();
        normalizerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            normalizer = NormalizeFactory.INSTANCE.getNormalizer(newValue);
        });
        vbox.getChildren().addAll(timeFilter, normalizerComboBox);
        return Borders.wrap(vbox).lineBorder().thickness(1).radius(5).build().build();
    }

    @Override
    public String getName() {
        return "Attribute Distribution";
    }

    @Override
    public String getDescription() {
        return "Shows the distribution of a certain packet attribute on a graded log chart. The x axis is split " +
                "into 100 groups. The traffic for each group is summed to give the value shown. The dots are a more " +
                "accurate indicator of the total traffic on that range - the area of the dots is directly " +
                "proportional to the number of packets on that interval. Click or click and drag to limit data to " +
                "a particular range.";
    }
}
