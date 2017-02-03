package netvis.view.util.jogl.gameengine;

import netvis.model.Packet;

import javax.media.opengl.GL2;
import java.awt.event.MouseEvent;

public abstract class Node {
    private String name = null;
    private Node parent;

    public Node(String nam) {
        name = nam;
    }

    public abstract void updateWithData(Packet pp);
    public abstract void updateAnimation(long time);
    public abstract int priority();
    public abstract void mouseClick(MouseEvent e);

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public void draw(int base, NodePainter painter, GL2 gl) {
        painter.drawNode(base, this, gl);
    }

    public Node getParent() {
        return parent;
    }
    public void setParent(Node par) {
        parent = par;
    }

    // Defaultly a node is a singular hexagon
    public int getDimension() {
        return 1;
    }

    public int getCapacity() {
        return 1;
    }

    public Node getNode(String name) {
        return null;
    }

    public void detachNode(Node n) {}

    public Node getClickedNode(int base, Position deltacoord) {
        if (deltacoord.x == 0 && deltacoord.y == 0) {
            return this;
        } else {
            return null;
        }
    }

    public boolean addNode(String name, Node n) {
        return false;
    }
}
