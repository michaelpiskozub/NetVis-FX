package netvis.view.util.jogl.comets;

import netvis.model.Packet;
import netvis.view.util.jogl.gameengine.Node;
import netvis.view.util.jogl.gameengine.NodePainter;
import netvis.view.util.jogl.gameengine.Position;
import netvis.view.util.jogl.gameengine.Units;

import javax.media.opengl.GL2;
import java.awt.event.MouseEvent;
import java.util.*;

public class MultiNode extends Node {
    // Dimension of this node
    private int dim;

    // Dimension of the subnode
    private int subdim;

    private Map<Position, Node> subnodes;
    private Map<String, Node> subnodesByName;

    public MultiNode (int dimension, MultiNode par) {
        super(null);

        setParent(par);

        dim = dimension;
        subdim = -1;

        subnodes = Collections.synchronizedMap(new HashMap<>());
        subnodesByName = Collections.synchronizedMap(new HashMap<>());
    }

    public int getSubDimension() {
        return subdim;
    }

    public int getSubSize() {
        return subnodes.size();
    }

    public int getFreeSlots () {
        // If the size of the subnode is not set - the node is empty
        if (subdim == -1) {
            return 1;
        }

        // Calculate how many rings can fit in the node
        int rings = 1;
        while (Units.dimToCap(rings) * Units.dimToCap(subdim) <= Units.dimToCap(dim)) {
            // While the ring still fits
            rings += 1;
        }
        rings -= 1;

        return (Units.dimToCap(rings) - subnodes.size());
    }

    private boolean allocateSubnode(String name, Node mn) {
        // Set the parent to this
        mn.setParent(this);

        // Find the first empty spot
        Position vrs = new Position (0, 0);
        Position p = new Position (0, 0);
        int i=0;
        boolean found = false;
        while (!found) {
            vrs = Units.findSpotAround (i);
            // Redraw this - it might be incorrect for the bigger ones
            p = Units.coordinateByRingAndShift (subdim, vrs.x, vrs.y);

            if (subnodes.get(p) == null) {
                found = true;
            }

            i++;
        }

        Position ars = Units.actualRingAndShift(subdim, vrs);

        // If the ring is too far away we can not allocate the subnode
        if (ars.x > this.dim) {
            return false;
        }

        // Register the node with its name
        synchronized (subnodesByName) {
            if (name != null) {
                subnodesByName.put(name, mn);
            }
        }

//        System.out.println("Node " + name + " placed in ringshift : (" + ars.x + ", " + ars.y +") coords : (" + p.x + ", " + p.y + ")");

        // Add it to the right place
        synchronized (subnodes) {
            subnodes.put(p, mn);
        }
        return true;
    }

    public int findNodeDim() {
        int trydim = dim-1;
        while (7 * Units.dimToCap(trydim) > Units.dimToCap(dim)) {
            trydim--;
        }

        return trydim;
    }

    @Override
    public Node getNode(String name) {
        Node nn = subnodesByName.get(name);

        if (nn == null) {
            // Look in the subnodes
            synchronized (subnodes) {
                for (Node n : subnodes.values()) {
                    nn = n.getNode(name);
                    // If found return it
                    if (nn != null) {
                        return nn;
                    }
                }
            }
        }

        return nn;
    }

    @Override
    public Node getClickedNode(int base, Position deltacoord) {
        Position highlevel = null;
        Node node = null;
        int howfar = 0;
        while (node == null && howfar < Units.dimToCap(dim)) {
            Position deltum = Units.findSpotAround(howfar);
            Position delatcor = Units.coordinateByRingAndShift(1, deltum.x, deltum.y);

            highlevel = new Position (deltacoord.x + delatcor.x, deltacoord.y + delatcor.y);

            node = subnodes.get(highlevel);
            howfar++;
        }

        if (node != null) {
            // Position of the center of the metanode in pixels
            Position deltaclicked = new Position (deltacoord.x - highlevel.x, deltacoord.y - highlevel.y);

            return node.getClickedNode(base, deltaclicked);
        }

        return null;
    }

    @Override
    public void draw(int base, NodePainter painter, GL2 gl) {
        // Draw each of the subnodes in the right position
        synchronized (subnodes) {
            Iterator<Map.Entry<Position, Node>> subnodesIter = subnodes.entrySet().iterator();
            while (subnodesIter.hasNext()) {
                Map.Entry<Position, Node> en = subnodesIter.next();
                Position coord = en.getKey();
                Node node = en.getValue();

                Position pos = Units.positionByCoordinate(base, coord);

                gl.glPushMatrix();
                gl.glTranslated(pos.x, pos.y, 0.0);

                node.draw(base, painter, gl);

                gl.glPopMatrix();
            }
        }

        // For debug purposes draw a big hexagon around
        gl.glPushMatrix();
        gl.glLineWidth (3.0f);
        gl.glRotated(90.0, 0.0, 0.0, 1.0);
        //Painter.DrawHexagon(GL2.GL_LINE_LOOP, 0.0, 0.0, (int) Math.round(base*Math.sqrt(3.0)*(dim-1)), gl);
        gl.glPopMatrix();
    }

    @Override
    public void detachNode(Node n) {
        // Find the specified node in the lists
        synchronized (subnodes) {
            Iterator<Map.Entry<Position, Node>> subnodesIter = subnodes.entrySet().iterator();
            while (subnodesIter.hasNext()) {
                Map.Entry<Position, Node> en = subnodesIter.next();
                if (en.getValue() == n) {
                    subnodes.remove(en.getKey());
                    break;
                }
            }
        }

        synchronized (subnodesByName) {
            Iterator<Map.Entry<String, Node>> subnodesByNameIter = subnodesByName.entrySet().iterator();
            while (subnodesByNameIter.hasNext()) {
                Map.Entry<String, Node> en = subnodesByNameIter.next();
                if (en.getValue() == n) {
                    subnodesByName.remove(en.getKey());
                    break;
                }
            }
        }
    }

    @Override
    public boolean addNode(String name, Node n) {
        int reqdim = n.getDimension();
        if (subdim == -1) {
            // If the node is empty - check whether we can subdivide the node
            if (reqdim >= findNodeDim()) {
                // We can put this node straight away (but if strict inequality - we won't fit anything else)
                subdim = reqdim;
                return allocateSubnode(name, n);
            } else {
                // We can put this node in the subnode
                subdim = findNodeDim();

                // So allocate the subnode
                Node mn = new MultiNode (subdim, this);
                allocateSubnode(null, mn);

                // And then add the considered node to the subnode
                return mn.addNode(name, n);
            }
        } else {
            // Check that the node size is small enough to be allocated
            if (reqdim > subdim) {
                return false;
            }

            // If the node fits perfectly - allocate it straight away
            if (reqdim == subdim) {
                return allocateSubnode(name, n);
            }

            // Try allocating this node in any of the subnodes
            synchronized (subnodes) {
                for (Node nodum : subnodes.values()) {
                    if (nodum.addNode(name, n)) {
                        return true;
                    }
                }
            }

            // If it failed - create a new subnode and allocate considered node in it
            Node mn = new MultiNode(subdim, this);
            mn.addNode (name, n);
            return allocateSubnode(null, mn);
        }
    }

    @Override
    public int getDimension() {
        return dim;
    }

    @Override
    public int getCapacity() {
        return Units.dimToCap(dim);
    }

    @Override
    public void updateWithData(Packet pp) {}

    @Override
    public void updateAnimation(long time) {
        // Recursively update the animation of the subnodes
        synchronized (subnodes) {
            for (Node n : subnodes.values()) {
                n.updateAnimation(time);
            }
        }
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void mouseClick(MouseEvent e) {}
}
