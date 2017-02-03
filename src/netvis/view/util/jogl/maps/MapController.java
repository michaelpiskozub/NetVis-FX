package netvis.view.util.jogl.maps;

import netvis.view.util.jogl.gameengine.Node;
import netvis.view.util.jogl.gameengine.Position;
import netvis.view.util.jogl.gameengine.Units;

import javax.media.opengl.awt.GLJPanel;
import java.awt.*;
import java.awt.event.*;

public class MapController {
    private Point oldpos = null;
    private Map map = null;

    public MapController (Map m, GLJPanel vis) {
        // Set up the connected map
        map = m;

        vis.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    map.zoomIn();
                }
                if (e.getWheelRotation() > 0) {
                    map.zoomOut();
                }
            }
        });

        vis.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {}

            @Override
            public void mouseDragged(MouseEvent e) {
                double viewfield = map.viewfieldanim.toDouble();
                if (oldpos != null) {
                    map.middlex.moveTo(map.middlex.getGoal() - (e.getX() - oldpos.x) * viewfield, 0);
                    map.middley.moveTo(map.middley.getGoal() + (e.getY() - oldpos.y) * viewfield, 0);
                    // map.middlex -= (e.getX()-oldpos.x)*viewfield;
                    // map.middley += (e.getY()-oldpos.y)*viewfield;
                }
                oldpos = e.getPoint();
            }
        });

        vis.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double viewfield = map.viewfieldanim.toDouble();
                int x = (int) Math.round (map.middlex.toDouble() + (e.getX() - (map.width / 2)) * viewfield);
                int y = (int) Math.round (map.middley.toDouble() - (e.getY() - (map.height / 2)) * viewfield);

                Node n = map.findClickedNode(x, y);
                Position c = Units.metaCoordinateByPosition(1, map.base, new Position(x, y));
                Position p = Units.metaPositionByCoordinate (1, map.base, c);
                if (n != null) n.mouseClick(e);

                if (e.getClickCount() == 2) {
                    // Zoom on the selected node - such that it will fill the
                    // screen
                    double goal = map.zoomOn();
                    map.viewfieldanim.moveTo(goal, 1000);
                }

                if (n != null) {
                    // Move to the selected node
                    map.middlex.moveTo(p.x, 1000);
                    map.middley.moveTo(p.y, 1000);
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
    }
}
