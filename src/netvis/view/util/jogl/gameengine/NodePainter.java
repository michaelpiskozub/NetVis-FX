package netvis.view.util.jogl.gameengine;

import netvis.view.util.jogl.comets.CometHeatNode;
import netvis.view.util.jogl.comets.FlipNode;
import netvis.view.util.jogl.comets.GraphNode;
import netvis.view.util.jogl.comets.HeatNode;

import javax.media.opengl.GL2;

public interface NodePainter {
    void drawNode(int base, HeatNode node, GL2 gl);
    void drawNode(int base, CometHeatNode node, GL2 gl);
    void drawNode(int base, FlipNode node, GL2 gl);
    void drawNode(int base, GraphNode node, GL2 gl);
    void drawNode(int base, Node node, GL2 gl);
}
