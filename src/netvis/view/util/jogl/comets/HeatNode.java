package netvis.view.util.jogl.comets;

import netvis.model.Packet;
import netvis.view.util.jogl.gameengine.Node;
import netvis.view.util.jogl.gameengine.NodePainter;

import javax.media.opengl.GL2;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class HeatNode extends Node {
    private int warning;
    private double[] bgColor;
    private double opacity;
    private String tex;
    private boolean selected;
    private long cumultime;

    // How much data was transferred
    private long data;

    // And with what protocols
    private HashMap<String, Long> protocollengths;
    private long maxVal = 0;
    public String maxProto = "";

    public HeatNode(String tname, String nname) {
        super (nname);

        // Cumulating time
        cumultime = 0;

        // How much data was transferred
        data = 0;

        tex = tname;
        bgColor = new double[3];
        opacity = 1.0;

        // Set the background color
        bgColor[0] = 0.5;
        bgColor[1] = 1.0;
        bgColor[2] = 0.7;
        warning = 0;

        selected = false;
        protocollengths = new HashMap<>();
    }

    public int getWarning() {
        return warning;
    }

    public double[] getBGColor() {
        return bgColor;
    }

    public double getOpacity () {
        return opacity;
    }

    public void setBGColor(double r, double g, double b) {
        bgColor[0] = r;
        bgColor[1] = g;
        bgColor[2] = b;
    }

    public String getTexture() {
        return tex;
    }

    public boolean getSelected() {
        return selected;
    }

    public void increaseWarning() {
        if (warning == 0) {
            // Make it colorful again
            bgColor[0] = 0.5;
            bgColor[1] = 1.0;
            bgColor[2] = 0.7;

            opacity = 1.0;
        }

        warning += 1;
        bgColor[0] *= 1.3;
        bgColor[1] *= 0.9;
        bgColor[2] *= 0.9;
    }

    public void decreaseWarning() {
        warning -= 1;
        bgColor[0] /= 1.3;
        bgColor[1] /= 0.9;
        bgColor[2] /= 0.9;

        if (warning == 0) {
            // Make it grey
            bgColor[0] = 0.7;
            bgColor[1] = 0.7;
            bgColor[2] = 0.7;

            opacity = 0.5;
        }
    }

    @Override
    public void draw(int base, NodePainter painter, GL2 gl) {
        painter.drawNode(base, this, gl);
    }

    @Override
    public void updateWithData(Packet pp) {
        data += pp.LENGTH;
        while (warning < data / 1024) {
            increaseWarning();
        }

        Long val = protocollengths.get(pp.PROTOCOL);
        if (val == null) {
            val = new Long(0);
            protocollengths.put(pp.PROTOCOL, val);
        }

        val += pp.LENGTH;
        protocollengths.put(pp.PROTOCOL, val);

        // Compare it with the current most common protocol
        if (val > maxVal) {
            maxVal = val;
            maxProto = pp.PROTOCOL;
        }
    }

    @Override
    public void updateAnimation(long time) {
        cumultime += time;
        if (cumultime >= 5000) {
            // Every second decrease the warning of the node
            if (warning > 0) {
                decreaseWarning();
            } else if (warning == 0) {
                // Delete the node by calling the parent method
                //this.GetParent().DetachNode (this);
            }

            // Reset the counter
            cumultime = 0;
        }
    }

    @Override
    public int priority() {
        return warning;
    }

    @Override
    public void mouseClick(MouseEvent e) {
        if (e.getClickCount() == 1) {
            selected = !selected;
        }
    }
}
