package netvis.view.visualizations.jogl;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import netvis.model.DataController;
import netvis.model.Packet;
import netvis.ui.controlsfx.Borders;
import netvis.view.VisualizationJOGL;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;
import netvis.view.util.jogl.ColorPalette;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Semaphore;

public class TrafficVolumeVisualization extends VisualizationJOGL {
    // GLUT object for drawing
    private final GLUT glut;

    // Fields holding packet data
    private final Map<Integer, Map<String, Integer>> protocolCountMaps;
    private final Map<String, Integer> globalProtocolCount;
    private final Map<String, Color> protocolColors;

    private final Semaphore available;

    // The color palette
    private ColorPalette colorPalette;

    // Fields governing the appearance of the graph
    private int maxTime;
    private int maxX;
    private double maxY;

    // Positions for the key labels in the protocol colour key (x100).
    private Point[] keyPositions = { new Point(-93, -85), new Point(-93, -93),
            new Point(-50, -77), new Point(-50, -85), new Point(-50, -93), new Point(-10, -77),
            new Point(-10, -85), new Point(-10, -93), new Point(30, -77), new Point(30, -85),
            new Point(30, -93), new Point(70, -77), new Point(70, -85), new Point(70, -93) };

    public TrafficVolumeVisualization(DataController dataController) {
        super(dataController, 10);

        glut = new GLUT();
        protocolCountMaps = Collections.synchronizedMap(new HashMap<>());
        globalProtocolCount = new HashMap<>();
        protocolColors = new HashMap<>();
        available = new Semaphore(1, true);
        colorPalette = new ColorPalette(ColorPalette.SCHEME_QUALITATIVE);
        maxTime = 0;
        maxX = 128;
        maxY = 0;
        controls = initializeControls();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor3d(0, 0, 0);
        gl.glRectd(-1, -1, 1, 1);
        gl.glColor3f(1, 1, 1);

        gl.glRasterPos2d(-0.9, 0.93); // Set title position
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "Traffic Volume by Protocol");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // go through all packets if we need to
        if (haveAllDataChanged) {
            haveAllDataChanged = false;
            maxTime = 0;
            protocolCountMaps.clear();
            globalProtocolCount.clear();
            processNewPackets(LIST_OF_PACKETS);
        }

        // find maximum heights of bars for scale
        maxY = 0;
        for (int i = maxTime - maxX; i < maxTime; i++) {
            Map<String, Integer> currentPackets = protocolCountMaps.get(i);
            if (currentPackets != null) {
                int sum = 0;
                for (int count : currentPackets.values()) {
                    sum += count;
                }
                maxY = Math.max(maxY, sum);
            }
        }

        // Draw the white background
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3d(0.95, 0.95, 0.95);
        gl.glVertex2d(-1, -0.7);
        gl.glVertex2d(-1, 0.9);
        gl.glVertex2d(1, 0.9);
        gl.glVertex2d(1, -0.7);
        gl.glEnd();

        double intervalWidth = 2.0 / maxX;
        double xPos = -1.0;

        // For each vertical strip
        for (int i = maxTime - maxX; i < maxTime; i++) {
            if (protocolCountMaps.containsKey(i)) {
                Map<String, Integer> currentPackets = protocolCountMaps.get(i);
                double yPos = -0.7;

                // For each protocol to be drawn in the strip
                synchronized (protocolCountMaps) {
                    Iterator<String> currentPacketsKeySetIterator = currentPackets.keySet().iterator();
                    while (currentPacketsKeySetIterator.hasNext()) {
                        String protocol = currentPacketsKeySetIterator.next();
                        Color c = protocolColors.get(protocol);
                        double height = currentPackets.get(protocol) * 1.6 / maxY;
                        gl.glBegin(GL2.GL_QUADS);
                        ColorPalette.setColor(gl, c);
                        gl.glVertex2d(xPos, yPos);
                        gl.glVertex2d(xPos, yPos + height);
                        gl.glVertex2d(xPos + intervalWidth, yPos + height);
                        gl.glVertex2d(xPos + intervalWidth, yPos);
                        gl.glEnd();
                        yPos += height;
                    }
                }
            }
            xPos += intervalWidth;
        }

        // Clear the title area
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3d(0, 0, 0);
        gl.glVertex2d(-1, 1);
        gl.glVertex2d(1, 1);
        gl.glVertex2d(1, 0.9);
        gl.glVertex2d(-1, 0.9);
        gl.glEnd();

        // Draw title
        gl.glColor3d(1, 1, 1);
        gl.glRasterPos2d(-0.9, 0.93);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12,
                "Traffic Volume Visualization -- Maximum y-value: " + (int) maxY + " packets");

        // Sort protocols by the most frequently-used first
        Comparator<String> comparator = (o1, o2) -> {
            if (globalProtocolCount.get(o1) >= globalProtocolCount.get(o2)) {
                return -1;
            } else {
                return 1;
            }
        };
        TreeMap<String, Integer> sortedProtocolMap = new TreeMap<>(comparator);
        sortedProtocolMap.putAll(globalProtocolCount);

        // Clear the key
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3d(0, 0, 0);
        gl.glVertex2d(-1, -1);
        gl.glVertex2d(-1, -0.7);
        gl.glVertex2d(1, -0.7);
        gl.glVertex2d(1, -1);
        gl.glEnd();

        // Draw key title
        gl.glColor3d(1, 1, 1);
        gl.glRasterPos2d(-0.95, -0.77);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "Protocol Color Key");

        // Generate key
        int i = 0;
        Iterator<String> protocolIterator = sortedProtocolMap.descendingKeySet().iterator();
        Stack<String> protocols = new Stack<>();
        while (protocolIterator.hasNext()) {
            protocols.push(protocolIterator.next());
        }

        while (i < keyPositions.length && !protocols.isEmpty()) {
            String protocol = protocols.pop();

            // Set label and color
            String label = protocol + " (" + globalProtocolCount.get(protocol) + ")";

            // Set color and position
            Color color = protocolColors.get(protocol);
            ColorPalette.setColor(gl, color);
            Point position = keyPositions[i];
            gl.glRasterPos2d(position.getX() / 100, position.getY() / 100);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, label);
            i++;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    @Override
    protected Node initializeControls() {
        VBox vbox = new VBox(10);
        Label maxXLabel = new Label("Set max X Axis resolution");
        HBox hBox = new HBox(10);
        TextField maxXTextField = new TextField();
        maxXTextField.setText(maxX + "");
        Button maxXbutton = new Button("Apply");
        maxXbutton.setOnAction(event -> {
            try {
                maxX = Integer.valueOf(maxXTextField.getText());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });
        hBox.getChildren().addAll(maxXTextField, maxXbutton);
        vbox.getChildren().addAll(maxXLabel, hBox);
        return Borders.wrap(vbox).lineBorder().thickness(1).radius(5).build().build();
    }

    @Override
    public String getName() {
        return "Traffic Volume";
    }

    @Override
    public String getDescription() {
        return "Each interval of received packets is presented as a column. Individual protocols are displayed in " +
                "a distinct colour to show how much traffic arrives over one protocol compared to others. Once the " +
                "maximum x-axis resolution has been reached, old data is discarded to allow new data to be shown. " +
                "The y-axis scales up automatically.";
    }

    // Change behaviour of packets arriving so we can process them.
    @Override
    public void newPacketsArrived(List<Packet> newPackets) {
        if (isShown) {
            this.newPackets = newPackets;
            List<Packet> synchronizedNewPackets = Collections.synchronizedList(newPackets);
            processNewPackets(synchronizedNewPackets);
        }
    }

    private synchronized void processNewPackets(List<Packet> newPackets) {
        // Get the number of packets using each protocol
        try {
            available.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Iterator<Packet> newPacketsIterator = newPackets.iterator();
        while (newPacketsIterator.hasNext()) {
            Packet p = newPacketsIterator.next();
            int packetTime = (int) Math.round(p.TIME);
            // If needed, make new protocol maps
            if (packetTime >= maxTime) {
                for (int i = maxTime; i <= packetTime + 1; i++) {
                    protocolCountMaps.put(i, new HashMap<>());
                }
                maxTime = packetTime + 1;
            }
            protocolSeen(p.PROTOCOL);
            // Update protocol map
            Map<String, Integer> protocolCount = protocolCountMaps.get(packetTime);
            if (protocolCount != null) {
                if (!protocolCount.containsKey(p.PROTOCOL)) {
                    protocolCount.put(p.PROTOCOL, 1);
                } else {
                    protocolCount.put(p.PROTOCOL, protocolCount.get(p.PROTOCOL) + 1);
                }
            }
        }

        // Forget data collected far in the past
        // (keep twice too many should user change maxX)
        for (int i = 0; i < maxTime - 2 * maxX; i++) {
            protocolCountMaps.remove(i);
        }
        available.release();
    }

    private void protocolSeen(String protocol) {
        if (!protocolColors.containsKey(protocol)) {
            protocolColors.put(protocol, colorPalette.getNextColor());
        }
        if (!globalProtocolCount.containsKey(protocol)) {
            globalProtocolCount.put(protocol, 1);
        } else {
            globalProtocolCount.put(protocol, globalProtocolCount.get(protocol) + 1);
        }
    }
}
