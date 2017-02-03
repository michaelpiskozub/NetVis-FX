package netvis.view.visualizations.jogl;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import netvis.model.DataController;
import netvis.model.Packet;
import netvis.view.VisualizationJOGL;
import netvis.view.util.jogl.comets.Candidate;
import netvis.view.util.jogl.gameengine.FramebufferPool;
import netvis.view.util.jogl.gameengine.TextRendererPool;
import netvis.view.util.jogl.gameengine.TexturePool;
import netvis.view.util.jogl.gameengine.VertexBufferPool;
import netvis.view.util.jogl.maps.MapActivity;
import netvis.view.util.jogl.maps.MapController;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GroupsOfActivityVisualization extends VisualizationJOGL {
    private int width;
    private int height;
    private Timer animator;
    private Timer cleaner;
    private MapActivity map;
    private final Map<String, Candidate> candidates;

    public GroupsOfActivityVisualization(DataController dataController) {
        super(dataController, 30);

        width = 800;
        height = 500;

        candidates = Collections.synchronizedMap(new HashMap<>());
        map = new MapActivity(width, height);
        new MapController(map, visualizationPanel);

        ActionListener animatum = new ActionListener() {
            long lasttime = (new Date()).getTime();

            public void actionPerformed(ActionEvent event) {
                // Animate the entities!
                long newtime = (new Date()).getTime();

                try {
                    map.stepAnimation(newtime - lasttime);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                lasttime = newtime;
            }
        };

        animator = new Timer(25, animatum);
        animator.start();

        ActionListener clearing = event -> {
            candidates.clear();
        };

        cleaner = new Timer(2000, clearing);
        cleaner.start();
        controls = initializeControls();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glClearDepth(0.0);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        double viewfield = map.getViewfield();
        double mx = map.getMX();
        double my = map.getMY();

//        // Count the FPS
//        if (oldTime == null) {
//            frameNum = 0;
//            oldTime = new Date();
//        } else {
//            Date now = new Date();
//            long diff = now.getTime() - oldTime.getTime();
//            if (diff > 2000) {
//                double fpsnum = Math.round(10000.0 * frameNum / (diff)) / 10.0;
//                fpsLabel.setText(fpsnum + " FPS");
//
//                oldTime = null;
//            }
//        }
//        frameNum++;

        // if (true)
        // return;

        GL2 gl = drawable.getGL().getGL2();

        // Set the width and height to the actuall width and height in pixels,
        // (0, 0) is in the middle
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glOrtho(mx - this.width * viewfield / 2, mx + this.width
                        * viewfield / 2, my - this.height * viewfield / 2,
                my + this.height * viewfield / 2, -1000, 2000);

        // Clear the board
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glClearDepth(3000.0);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glShadeModel(GL2.GL_FLAT);

        // Depth things - probably unnecessary
        // gl.glEnable(GL.GL_DEPTH_TEST);
        // gl.glDepthFunc(GL2.GL_GEQUAL);

        // Use the typical blending options
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND);

        // glut.glutInitDisplayMode(GLUT. GLUT_DOUBLE | GLUT.GLUT_RGBA |
        // GLUT.GLUT_DEPTH | GLUT_MULTISAMPLE);

        // Try making rendering as nice as possible
        gl.glEnable(GL2.GL_MULTISAMPLE);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glEnable(GL2.GL_POLYGON_SMOOTH);
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
        gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);

        gl.glPushMatrix();
        //Painter.StressTestList (currentMap.base, gl);
        map.drawEverything(gl);
        gl.glPopMatrix();

        // Probably unnecessary
        visualizationPanel.swapBuffers();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

//        if (width != this.width || height != this.height) {
//            this.width = width;
//            this.height = height;
//            visualizationPanel.setSize(width, height);
//            visualizationPanel.setPreferredSize(new Dimension(width, height));
//        }

        map.setSize(width, height, gl);

        // Mark all the graphic card side object as broken
        TexturePool.discardTextures();
        TextRendererPool.recreate();
        FramebufferPool.discardAll();
        VertexBufferPool.discardAll();
    }

    @Override
    protected Node initializeControls() {
        return new Region();
    }

    @Override
    public String getName() {
        return "Groups of Activity";
    }

    @Override
    public String getDescription() {
        return "A hexagonal grid displaying clients active in the network. They are being grouped around the " +
                "machine they send packets to. Nodes 'heat up' when amount of data goes over specified threshold " +
                "and 'heat down' in time.";
    }

    @Override
    public void newPacketsArrived(List<Packet> newPackets) {
        // Dispatch the data to the specific points in the Map
        synchronized (candidates) {
            for (Packet i : newPackets) {
                Candidate dri = candidates.get(i.SOURCE_IP);
                if (dri == null) {
                    // Create the candidate to be displayed
                    dri = new Candidate(0, i.LENGTH, i.SOURCE_IP, i.DESTINATION_IP);
                    candidates.put(i.SOURCE_IP, dri);
                }
                dri.RegisterPacket(i);
            }

            // Decide on which candidates should be displayed
            for (String ip : candidates.keySet()) {
                Candidate can = candidates.get(ip);

                // System.out.println("I'm considering IP: " + ip +
                // " which dataflow: " + can.datasize);
                if (can.datasize >= 2000) {
                    // System.out.println("IP: " + ip + " which dataflow: " +
                    // can.datasize + " added to the simulation");
                    map.suggestNode(can.sip, can.dip, can.getWaitingPackets());
                    can.resetWaitingPackets();
                }
            }
        }
    }

    @Override
    public void activate() {
        TexturePool.discardTextures();
        super.activate();
    }

    @Override
    public void interrupt() {
        animator.stop();
        cleaner.stop();
        super.interrupt();
    }
}
