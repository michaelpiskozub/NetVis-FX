package netvis.view.visualizations.jogl;

import javafx.scene.layout.Region;
import netvis.model.DataController;
import netvis.model.Packet;
import netvis.view.VisualizationJOGL;
import netvis.view.util.jogl.comets.Candidate;
import netvis.view.util.jogl.gameengine.*;
import netvis.view.util.jogl.gameengine.Node;
import netvis.view.util.jogl.maps.MapHeat;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HeatmapOfActivityVisualization extends VisualizationJOGL {
    private int width;
    private int height;
    private Point oldpos = null;
    private ValueAnimator middlex;
    private ValueAnimator middley;
    private ValueAnimator viewfieldanim;
    private Timer animator;
    private Timer cleaner;
    private MapHeat currentMap;
    private final Map<String, Candidate> candidates;

    public HeatmapOfActivityVisualization(DataController dataController) {
        super(dataController, 30);

        viewfieldanim = new ValueAnimator(5.0);
        middlex = new ValueAnimator(0.0);
        middley = new ValueAnimator(0.0);

        width = 800;
        height = 500;

        candidates = Collections.synchronizedMap(new HashMap<>());

        currentMap = new MapHeat(width, height);

        ActionListener animatum = new ActionListener() {
            long lasttime = (new Date()).getTime();

            public void actionPerformed(ActionEvent event) {
                // Animate the entities!
                long newtime = (new Date()).getTime();

                try {
                    currentMap.stepAnimation(newtime - lasttime);
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

        // Now add the keyboard listener which will be responsible for zooming
        visualizationPanel.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_EQUALS) {
                    zoomIn();
                }
                if (e.getKeyChar() == KeyEvent.VK_MINUS) {
                    zoomOut();
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });

        visualizationPanel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                zoomIn();
            }
            if (e.getWheelRotation() > 0) {
                zoomOut();
            }
        });

        visualizationPanel.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                double viewfield = viewfieldanim.toDouble();
                if (oldpos != null) {
                    middlex.moveTo(middlex.getGoal() - (e.getX() - oldpos.x) * viewfield, 0);
                    middley.moveTo(middley.getGoal() + (e.getY() - oldpos.y) * viewfield, 0);
                    // middlex -= (e.getX()-oldpos.x)*viewfield;
                    // middley += (e.getY()-oldpos.y)*viewfield;
                }
                ;
                oldpos = e.getPoint();
            }
        });

        visualizationPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                double viewfield = viewfieldanim.toDouble();
                int x = (int) Math.round(middlex.toDouble() + (e.getX() - (width / 2)) * viewfield);
                int y = (int) Math.round(middley.toDouble() - (e.getY() - (height / 2)) * viewfield);

                Node nnn = currentMap.findClickedNode(x, y);
                Position ppp = currentMap.findClickedNodePos(x, y);
                if (nnn != null) nnn.mouseClick(e);

                if (e.getClickCount() == 2) {
                    // Sort the map
                    currentMap.sortNodes();

                    // Zoom on the selected node - such that it will fill the
                    // screen
                    double goal = currentMap.zoomOn();
                    viewfieldanim.moveTo(goal, 1000);
                }

                if (nnn != null) {
                    // Move to the selected node
                    middlex.moveTo(ppp.x, 1000);
                    middley.moveTo(ppp.y, 1000);
                }

                e.consume();
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {}

            @Override
            public void mouseExited(MouseEvent arg0) {}

            @Override
            public void mousePressed(MouseEvent arg0) {}

            @Override
            public void mouseReleased(MouseEvent arg0) {
                oldpos = null;
            }

        });

        // Add test nodes
        for (int i = 0; i < 0; i++) {
            Packet pp = new Packet(0, 0, "testk" + i, "", 11, "test" + i, "", 0, "", 0, "");
            ArrayList<Packet> plist = new ArrayList<>();
            plist.add(pp);
            currentMap.suggestNode(pp.SOURCE_IP, pp.DESTINATION_IP, plist);
        }
        controls = initializeControls();
    }

    private void zoomIn() {
        double viewfield = viewfieldanim.getGoal();
        viewfieldanim.moveTo(viewfield * 0.9, 100);
    }

    private void zoomOut() {
        double viewfield = viewfieldanim.getGoal();
        viewfieldanim.moveTo(viewfield * 1.1, 100);
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
        double viewfield = viewfieldanim.toDouble();

//        // Count the FPS
//        if (oldTime == null) {
//            frameNum = 0;
//            oldTime = new Date();
//        } else {
//            Date now = new Date();
//            long diff = now.getTime() - oldTime.getTime();
//            if (diff > 2000) {
//                double fpsnum = Math.round(10000.0 * frameNum / (diff)) / 10.0;
//                fps.setText(fpsnum + " FPS");
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

        gl.glOrtho(middlex.toDouble() - this.width * viewfield / 2, middlex.toDouble() + this.width
                        * viewfield / 2, middley.toDouble() - this.height * viewfield / 2,
                middley.toDouble() + this.height * viewfield / 2, -1000, 2000);

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
        //gl.glTranslated(0.0, 0.0, -1.0);
        // Make the map draw all of the elements
        currentMap.drawEverything(gl);
        gl.glPopMatrix();

        //this.swapBuffers();
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

        currentMap.setSize(width, height, gl);

        // Mark all the graphic card side object as broken
        TexturePool.discardTextures();
        TextRendererPool.recreate();
        FramebufferPool.discardAll();
        VertexBufferPool.discardAll();
    }

    @Override
    protected javafx.scene.Node initializeControls() {
        return new Region();
    }

    @Override
    public String getName() {
        return "Heatmap of Activity";
    }

    @Override
    public String getDescription() {
        return "A hexagonal grid displaying clients active in the network. Color indicated how much data is being " +
                "transferred. Nodes 'heat up' when amount of data goes over specified threshold and 'heat down' in " +
                "time.";
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
                    currentMap.suggestNode(can.sip, can.dip, can.getWaitingPackets());
                    currentMap.suggestNode(can.dip, can.sip, can.getWaitingPackets());

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
