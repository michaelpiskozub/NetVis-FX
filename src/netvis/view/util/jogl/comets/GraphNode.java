package netvis.view.util.jogl.comets;

import netvis.model.Packet;
import netvis.view.util.jogl.gameengine.Node;
import netvis.view.util.jogl.gameengine.NodePainter;

import javax.media.opengl.GL2;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class GraphNode extends Node {
    public HashMap<String, Long> protocollengths;
    public long maxVal = 0;

    public GraphNode(String n) {
        super(n);

        protocollengths = new HashMap<>();
    }

    @Override
    public void draw(int base, NodePainter painter, GL2 gl) {
        painter.drawNode(base, this, gl);
    }

    @Override
    public void updateWithData(Packet pp) {
        Long val = protocollengths.get (pp.PROTOCOL);
        if (val == null) {
            val = new Long(0);
            protocollengths.put(pp.PROTOCOL, val);
        }

        val += pp.LENGTH;
        protocollengths.put(pp.PROTOCOL, val);

        // Compare it with the current most common protocol
        if (val > maxVal) {
            maxVal = val;
        }
    }

    @Override
    public void updateAnimation(long time) {}

    @Override
    public int priority() {
        // Those nodes don't want to be in the middle
        return -1;
    }

    @Override
    public void mouseClick(MouseEvent e) {}
}
