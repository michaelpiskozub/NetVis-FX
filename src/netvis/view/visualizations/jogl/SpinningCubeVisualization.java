package netvis.view.visualizations.jogl;

import com.jogamp.opengl.util.gl2.GLUT;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import netvis.model.DataController;
import netvis.model.NormalizeFactory;
import netvis.model.Normalizer;
import netvis.model.Packet;
import netvis.ui.controlsfx.Borders;
import netvis.view.VisualizationJOGL;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

public class SpinningCubeVisualization extends VisualizationJOGL {
    private GLU glu;
    private GLUT glut;
    private boolean spin;
    private float yrot;
    private Normalizer xNormalizer;
    private Normalizer yNormalizer;
    private Normalizer zNormalizer;

    public SpinningCubeVisualization(DataController dataController) {
        super(dataController, 60);
        xNormalizer = NormalizeFactory.INSTANCE.getNormalizer("Source MAC");
        yNormalizer = NormalizeFactory.INSTANCE.getNormalizer("Source IP");
        zNormalizer = NormalizeFactory.INSTANCE.getNormalizer("Source Port");
        spin = true;
        yrot = 1f;
        controls = initializeControls();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Global settings.
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_FOG);
        gl.glEnable(GL2.GL_DITHER);
        gl.glHint(GL2.GL_FOG_HINT, GL2.GL_NICEST);

        gl.glColor3d(0, 0, 0);
        glut = new GLUT();

        // We want a nice perspective.
        // Create GLU.
        glu = new GLU();
        // Change to projection matrix.
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        // Perspective.
        glu.gluPerspective(45, 1, 1, 100);
        glu.gluLookAt(2.3, 0.5, 1.8, 0.5, 0.5, 0.5, 0, 1, 0);

        // Change back to model view matrix.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glTranslated(0.5, 0, 0.5);
        if (spin) {
            gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);
        }
        gl.glTranslated(-0.5, 0, -0.5);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glColor3d(0, 1, 0);

        gl.glColor3f(0.2f, 0.2f, 0.2f);

        for (float i = 1; i < 5; i++) {
            gl.glBegin(GL.GL_LINES);
            gl.glVertex3d(0, 0, i / 5);
            gl.glVertex3d(1, 0, i / 5);

            gl.glVertex3d(i / 5, 0, 0);
            gl.glVertex3d(i / 5, 0, 1);
            gl.glEnd();
        }

        gl.glColor3f(0.3f, 0.7f, 0.8f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(0, 1, 0);
        gl.glVertex3d(0, 1, 1);
        gl.glVertex3d(0, 0, 1);
        gl.glEnd();

        gl.glColor3f(0.3f, 0.7f, 0.8f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(1, 0, 0);
        gl.glVertex3d(1, 1, 0);
        gl.glVertex3d(1, 1, 1);
        gl.glVertex3d(1, 0, 1);
        gl.glEnd();

        gl.glColor3f(0.6f, 0.3f, 0.5f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(1, 0, 0);
        gl.glVertex3d(1, 0, 1);
        gl.glVertex3d(0, 0, 1);
        gl.glEnd();

        gl.glColor3f(0.6f, 0.3f, 0.5f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(0, 1, 0);
        gl.glVertex3d(1, 1, 0);
        gl.glVertex3d(1, 1, 1);
        gl.glVertex3d(0, 1, 1);
        gl.glEnd();

        // Draw the points
        Packet p;
        gl.glPointSize(5);
        gl.glBegin(GL2.GL_POINTS);

        for (int i = 0; i < LIST_OF_PACKETS.size(); i++) {
            p = LIST_OF_PACKETS.get(i);
            // we add entropy to make it more obvious when there
            // are many requests on a certain port
            double entropy = (Math.random()- 0.5)/100;
            double x = xNormalizer.normalize(p) + entropy;
            double y = yNormalizer.normalize(p) + entropy;
            double z = zNormalizer.normalize(p) + entropy;
            gl.glColor4d(x, y, z, 0.3);
            gl.glVertex3d(x, y, z);
        }
        gl.glEnd();

        gl.glColor3d(1, 1, 1);
        gl.glRasterPos3d(0, 0, 0); // set position
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "Origin");
        gl.glRasterPos3d(1, 0, 0); // set position
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, xNormalizer.getName());
        gl.glRasterPos3d(0, 1, 0); // set position
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, yNormalizer.getName());
        gl.glRasterPos3d(0, 0, 1); // set position
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, zNormalizer.getName());
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    @Override
    protected Node initializeControls() {
        HBox xAxisBox = new HBox(10);
        Label xAxisLabel = new Label("X Axis");
        ComboBox<String> xAxisComboBox = new ComboBox<>();
        xAxisComboBox.getItems().addAll(NormalizeFactory.INSTANCE.getNormalizerNames());
        xAxisComboBox.getSelectionModel().select(xNormalizer.getName());
        xAxisComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            xNormalizer = NormalizeFactory.INSTANCE.getNormalizer(newValue);
        });
        xAxisBox.getChildren().addAll(xAxisLabel, xAxisComboBox);

        HBox yAxisBox = new HBox(10);
        Label yAxisLabel = new Label("Y Axis");
        ComboBox<String> yAxisComboBox = new ComboBox<>();
        yAxisComboBox.getItems().addAll(NormalizeFactory.INSTANCE.getNormalizerNames());
        yAxisComboBox.getSelectionModel().select(yNormalizer.getName());
        yAxisComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            yNormalizer = NormalizeFactory.INSTANCE.getNormalizer(newValue);
        });
        yAxisBox.getChildren().addAll(yAxisLabel, yAxisComboBox);

        HBox zAxisBox = new HBox(10);
        Label zAxisLabel = new Label("Z Axis");
        ComboBox<String> zAxisComboBox = new ComboBox<>();
        zAxisComboBox.getItems().addAll(NormalizeFactory.INSTANCE.getNormalizerNames());
        zAxisComboBox.getSelectionModel().select(zNormalizer.getName());
        zAxisComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            zNormalizer = NormalizeFactory.INSTANCE.getNormalizer(newValue);
        });
        zAxisBox.getChildren().addAll(zAxisLabel, zAxisComboBox);

        Button spinButton = new Button("Toggle Spin");
        spinButton.setOnAction(event -> spin = !spin);
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(xAxisBox, yAxisBox, zAxisBox, spinButton);
        return Borders.wrap(vbox).lineBorder().thickness(1).radius(5).build().build();
    }

    @Override
    public String getName() {
        return "Spinning Cube";
    }

    @Override
    public String getDescription() {
        return "Visualization based on the Spinning Cube of Potential Doom. " +
                "Every dimension can be set to show any attribute. " +
                "Packets also vibrate to make dense clusters more obvious. ";
    }
}
