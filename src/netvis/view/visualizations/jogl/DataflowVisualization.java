package netvis.view.visualizations.jogl;

import com.jogamp.opengl.util.gl2.GLUT;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import netvis.model.DataController;
import netvis.model.NormalizeFactory;
import netvis.model.Normalizer;
import netvis.model.Packet;
import netvis.ui.controlsfx.Borders;
import netvis.view.VisualizationJOGL;
import netvis.view.util.jogl.ColorPalette;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import java.awt.*;
import java.util.List;

/**
 * <pre>
 * Data Flow Visualisation
 *
 * Saturation indicates time (the gray lines are past lines); the coloured lines
 * are more recent.
 *
 * All the lines are transparent therefore if a line stands out it means lots of
 * packets go through that line.
 *
 * The red triangles show traffic. They shrink with each iteration by some
 * percentage and grow linearly with each packet on that interval. They also
 * have an upper limit.
 */
public class DataflowVisualization extends VisualizationJOGL {
    private List<Normalizer> normPasses;
    private float[][] trafficMeasure;
    private GLUT glut;
    private int colorPasser;
    private int visHighlighted;
    private boolean displaySelectionBar;
    private Color fColor;
    private Color lColor;
    private int pastLimit;

    public DataflowVisualization(DataController dataController) {
        super(dataController, 30);

        normPasses = NormalizeFactory.INSTANCE.getNormalizers();
        trafficMeasure = new float[normPasses.size()][100];
        colorPasser = 0;
        displaySelectionBar = false;
        fColor = Color.green;
        lColor = Color.blue;
        pastLimit = 30;
        controls = initializeControls();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Global settings.
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glScaled(1.8, 1.8, 1);
        gl.glTranslated(-0.5, -0.5, 0);
        gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
        gl.glLineWidth(1);
        glut = new GLUT();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        gl.glColor4d(0.2, 0.2, 0.2, 0.5);
        gl.glRasterPos2f(0.5f, 1.01f);
        glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "Dataflow");

        gl.glColor3d(0.2, 0.2, 0.2);
        gl.glRasterPos2f(0, 1.01f);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "Brightness: Time, Opacity: Volume");

        for (int j = 1; j < normPasses.size(); j++) {
            gl.glColor3d(((double) (j % 2)) / 30 + 0.8, ((double) (j % 2)) / 30 + 0.8,
                    ((double) (j % 2)) / 30 + 0.82);
            gl.glRectd(((double) j - 1) / (normPasses.size() - 1), 0,
                    ((double) j) / (normPasses.size() - 1), 1);
        }
        float conditional = 0;
        for (int i = 0; i < normPasses.size(); i++) {
            gl.glColor3d(0.2, 0.2, 0.2);
            if (i == normPasses.size() - 1) {
                conditional = -0.05f;
            } else {
                conditional = 0f;
            }
            gl.glRasterPos3f(((float) i * 1.2f) / normPasses.size() + conditional, -0.04f, 0);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, normPasses.get(i).getName());
        }
        Packet p;

        gl.glColor3d(0, 0, 0);
        int ii = -1;
        if (LIST_OF_PACKETS.size() > 0) {
            double searchTime = LIST_OF_PACKETS.get(LIST_OF_PACKETS.size()-1).TIME - pastLimit;
            ii = LIST_OF_PACKETS.size() -1;
            while (ii > 0 && LIST_OF_PACKETS.get(ii).TIME > searchTime) {
                ii--;
            }
        }
        if (ii < -1) {
            ii = -1;
        }
        ii++;
        for (int i = ii; i < LIST_OF_PACKETS.size(); i++) {
            p = LIST_OF_PACKETS.get(i);

            double standout = (p.TIME - LIST_OF_PACKETS.get(ii).TIME) /
                    (LIST_OF_PACKETS.get(LIST_OF_PACKETS.size() - 1).TIME - LIST_OF_PACKETS.get(ii).TIME);

            double entropy = (standout*(Math.random() - 0.5)) / 300;

            ColorPalette.setColor(gl, ColorPalette.getColorShade(
                    fColor, lColor, normPasses.get(colorPasser).normalize(p)), standout);
            gl.glBegin(GL2.GL_LINE_STRIP);
            for (int j = 0; j < normPasses.size(); j++) {
                double normVal = normPasses.get(j).normalize(p) + entropy;
                gl.glVertex2d(((double) j) / (normPasses.size() - 1), normVal);
            }
            gl.glEnd();
        }

        for (int i = 0; i < newPackets.size(); i++) {
            p = newPackets.get(i);
            for (int j = 0; j < normPasses.size(); j++) {
                double normVal = normPasses.get(j).normalize(p);
                trafficMeasure[j][(int) (normVal * 99)] += 0.001;
            }
        }
        for (int i = 0; i < normPasses.size(); i++) {
            for (int j = 0; j < 100; j++) {
                if ((int) (trafficMeasure[i][j] * 2048) != 0) {
                    if (trafficMeasure[i][j] > 0.1f)
                        trafficMeasure[i][j] = 0.1f;
                    gl.glColor4f(0.6f + trafficMeasure[i][j] * 4, 0f, 0, trafficMeasure[i][j] * 4);

                    drawActivityBar(gl, ((float) i) / (normPasses.size() - 1), (float) j*0.01f,
                            trafficMeasure[i][j] / 2);
                    trafficMeasure[i][j] = trafficMeasure[i][j] * 0.99f;
                }
            }
        }

        if (displaySelectionBar) {
            gl.glColor4d(0, 0, 0, 0.5);
            gl.glBegin(GL2.GL_POLYGON);
            float x = ((float) visHighlighted) / (normPasses.size() - 1);

            gl.glVertex2f(x+0.13f, 1f);
            gl.glVertex2f(x+0.13f, 0f);
            gl.glVertex2f(x-0.01f, 0f);
            gl.glVertex2f(x-0.01f, 1f);
            gl.glEnd();
            gl.glColor3d(1, 1, 1);
            for (int i = 0; i < 10; i++) {
                gl.glBegin(GL2.GL_LINE_STRIP);
                gl.glVertex2f(x-0.01f, (float)i/10);
                gl.glVertex2f(x, (float)i/10);
                gl.glEnd();
                gl.glRasterPos2f(x+0.01f, (float)i/10); // set position
                glut.glutBitmapString(
                        GLUT.BITMAP_HELVETICA_12, normPasses.get(visHighlighted).denormalize((double)i/10)
                );
            }
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
        Label normalizerLabel = new Label("Color");
        ComboBox<String> normalizerComboBox = new ComboBox<>();
        normalizerComboBox.getItems().addAll(NormalizeFactory.INSTANCE.getNormalizerNames());
        normalizerComboBox.getSelectionModel().selectFirst();
        normalizerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            colorPasser = normalizerComboBox.getSelectionModel().getSelectedIndex();
        });
        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(normalizerLabel, normalizerComboBox);
        vbox.getChildren().addAll(timeFilter, hbox);
        return Borders.wrap(vbox).lineBorder().thickness(1).radius(5).build().build();
    }

    @Override
    public String getName() {
        return "Dataflow";
    }

    @Override
    public String getDescription() {
        return "Saturation indicates time - grey lines are past lines, coloured lines are more recent. You can " +
                "follow a packet by it's colour. The red bars show traffic volume. They shrink with each iteration " +
                "by some percentage and grow linearly with each packet, up to a limit. Hover over an axis to show " +
                "the scale. Click an axis to switch to the distribution visualisation for that attribute.";
    }

    private void drawActivityBar(GL2 gl, float xc, float yc, float radius) {
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex2f(xc+radius, yc+0.01f);
        gl.glVertex2f(xc+radius, yc);
        gl.glVertex2f(xc-radius, yc);
        gl.glVertex2f(xc-radius, yc+0.01f);
        gl.glEnd();
    }
}
