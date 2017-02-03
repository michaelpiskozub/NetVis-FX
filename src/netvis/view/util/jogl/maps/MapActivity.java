package netvis.view.util.jogl.maps;

import junit.framework.Assert;
import netvis.model.Packet;
import netvis.view.util.jogl.comets.FlipNode;
import netvis.view.util.jogl.comets.GraphNode;
import netvis.view.util.jogl.comets.HeatNode;
import netvis.view.util.jogl.comets.MultiNode;
import netvis.view.util.jogl.gameengine.Node;
import netvis.view.util.jogl.gameengine.Painter;
import netvis.view.util.jogl.gameengine.Position;
import netvis.view.util.jogl.gameengine.Units;

import javax.media.opengl.GL2;
import java.util.*;
import java.util.concurrent.*;

public class MapActivity extends Map {
    private MapPainter painter;

    // Top dimension
    private int dim;

    // Top level nodes
    private final java.util.Map<Position, MultiNode> nodes;

    // Different way of storing them
    private HashMap<String, MultiNode> nodesByName;

    private Random rand;

//    private class NamedThreadFactory implements ThreadFactory {
//        int i = 0;
//
//        public Thread newThread(Runnable r) {
//            return new Thread(r, "Node animating thread #" + (i++));
//        }
//    }
//
//    // Animation of the nodesByName can be parallelised
//    public ExecutorService exe = new ThreadPoolExecutor(4, 8, 5000, TimeUnit.MILLISECONDS,
//            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory());

    public MapActivity(int w, int h) {
        width = w;
        height = h;
        nodes = Collections.synchronizedMap(new HashMap<>());
        nodesByName = new HashMap<>();

        // Start with one node 14x14
        dim = 5;
        Painter.generateGrid("grid", dim);
        rand = new Random();
        painter = new MapPainter();
    }

    public void drawEverything(GL2 gl) {
        synchronized (nodes) {
            Iterator<java.util.Map.Entry<Position, MultiNode>> nodesIter = nodes.entrySet().iterator();
            while (nodesIter.hasNext()) {
                java.util.Map.Entry<Position, MultiNode> en = nodesIter.next();
                Position pos = en.getKey();
                MultiNode mn = en.getValue();

                Position realp = Units.positionByCoordinate(base, pos);

                gl.glPushMatrix();
                // Transpose it to the right spot
                gl.glTranslated(realp.x, realp.y, 0.0);

                // Draw it
                Painter.drawGrid(base, dim, "grid", gl);
                mn.draw(base, painter, gl);
                gl.glPopMatrix();
            }
        }
    }

    public void stepAnimation(final long time) throws InterruptedException, ExecutionException {
        synchronized (nodes) {
            for (final Node i : nodes.values()) {
                i.updateAnimation(time);
            }
        }

//        ArrayList<Future<?>> list = new ArrayList<>();
//        for (final Node i : nodes.values()) {
//            list.add(exe.submit(new Callable() {
//                @Override
//                public Object call() throws Exception {
//                    i.updateAnimation(time);
//                    return null;
//                }
//            }));
//        }
//
//        for (Future<?> i : list) {
//            i.get();
//        }
    }

    public void setSize(int w, int h, GL2 gl) {
        width = w;
        height = h;
    }

    public void suggestNode(String sip, String dip, List<Packet> packets) {
        // Suggests the existence of the node in the network to be displayed
        Node nnn = addNode(dip, sip, "basic");

        // Update the node with data
        for (Packet pp : packets) {
            nnn.updateWithData(pp);
        }
    }

    private Node addNode(String groupname, String nodename, String textureName) {
        // Look for the wrapping node
        MultiNode groupnode = nodesByName.get(groupname);
        if (groupnode == null) {
            // If there is no grouping node - create it
            groupnode = new MultiNode(2, null);
            groupnode.setName(groupname);
            nodesByName.put(groupname, groupnode);

            // Try putting the newly created group somewhere
            boolean placed = false;
            synchronized (nodes) {
                for (MultiNode n : nodes.values()) {
                    // If the node is already named - it's a group, so don't try adding anything to it
                    if (n.getName() != null) {
                        continue;
                    }

                    if (n.addNode(groupname, groupnode)) {
                        placed = true;
                        break;
                    }
                }
            }

            // If there is nowhere to place the new group - add the top level node
            if (!placed) {
                MultiNode newtop = allocateTopLevelNode();
                newtop.addNode(groupname, groupnode);
            }

            // Put the indicator node in the middle of the group node
            HeatNode midnode = new HeatNode(textureName, groupname);
            midnode.setBGColor(0.5, 0.6, 1.0);

            groupnode.addNode(groupname, midnode);
        }

        // If the node is not already there - add it
        Node found = groupnode.getNode(nodename);
        if (found == null) {
            // Prepare the new node
            HeatNode front = new HeatNode(textureName, nodename);
            GraphNode back = new GraphNode(nodename);

            // Make it into the flip node - the node that has two sides
            FlipNode newnode = new FlipNode(front, back);

            if (tryAdding(newnode, groupnode) == null)
            {
                boolean placed = false;
                MultiNode parent = (MultiNode) groupnode.getParent();
                if (parent == null) {
                    // If this is the top level node - resize everything
                    resizeBaseNode ();

                    // Now we need to try adding this node once again
                    placed = (tryAdding(newnode, groupnode) != null);

                    if (!placed) {
                        MultiNode newtop = allocateTopLevelNode();
                        placed = newtop.addNode(groupname, groupnode);
                    }

                    // It has to fit now
                    Assert.assertEquals(placed, true);
                }

                if (!placed) {
                    // Take the node out
                    parent.detachNode(groupnode);

                    // Wrap it in the bigger node
                    MultiNode trygroupnode = wrapExpand(groupnode);

                    // Try putting the node somewhere else
                    synchronized (nodes) {
                        for (MultiNode n : nodes.values()) {
                            if (tryAdding(trygroupnode, n) != null) {
                                placed = true;
                                groupnode = trygroupnode;
                                break;
                            }
                        }
                    }
                }

                // If that failed try putting the node in the new top level suernode
                if (!placed) {
                    MultiNode newtop = allocateTopLevelNode();
                    placed = newtop.addNode(groupname, groupnode);
                }
                tryAdding(newnode, groupnode);
            }
            found = newnode;
        }

        return found;
    }

    private MultiNode tryAdding(Node added, MultiNode group) {
        String groupname = group.getName();
        String nodename  = added.getName();

        if (group.addNode(nodename, added)) {
            return group;
        }

        MultiNode parent = (MultiNode) group.getParent();
        if ((parent != null) && (parent.getSubSize() == 1)) {
            parent.addNode(nodename, added);
            parent.setName(groupname);
            nodesByName.put(groupname, parent);
            return parent;
        }

        // Impossible to expand - node has to be put somewhere else
        return null;
    }

    private void resizeBaseNode() {
        // First find the next supernode size
        int newdim = dim;
        while (Units.dimToCap(newdim) < 7*Units.dimToCap(dim)) {
            newdim += 1;
        }

        dim = newdim;
        Painter.generateGrid("grid", dim);

        // Now for all the existing nodes put them through the adding procedure again
        nodes.clear();

        Iterator<java.util.Map.Entry<String, MultiNode>> nodesByNameIter = nodesByName.entrySet().iterator();
        while (nodesByNameIter.hasNext()) {
            java.util.Map.Entry<String, MultiNode> en = nodesByNameIter.next();
            // Try putting the node somewhere else
            boolean placed = false;
            synchronized (nodes) {
                for (MultiNode n : nodes.values()) {
                    if (tryAdding(en.getValue(), n) != null) {
                        placed = true;
                        break;
                    }
                }
            }

            if (!placed) {
                // If there is no top level grouping node - create it
                MultiNode newtoplevel = allocateTopLevelNode();
                newtoplevel.addNode (en.getKey(), en.getValue());
            }
        }
    }

    private MultiNode allocateTopLevelNode() {
        Position ringshift = Units.findSpotAround(nodes.size());

        Position coord = Units.coordinateByRingAndShift(dim, ringshift.x, ringshift.y);

        // Create the node - start small
        MultiNode groupnode = new MultiNode(dim, null);

        synchronized (nodes) {
            nodes.put(coord, groupnode);
        }

        return groupnode;
    }

    private MultiNode wrapExpand(MultiNode groupnode) {
        String groupname = groupnode.getName();

        // Find the new dimension
        int dim = groupnode.getDimension();
        int newdim = dim;
        while (Units.dimToCap(newdim) < 7*Units.dimToCap(dim)) {
            newdim += 1;
        }

        // Create a wrapper node
        MultiNode wrapper = new MultiNode (newdim, null);
        wrapper.addNode(groupname, groupnode);

        return wrapper;
    }

    @Override
    public Node findClickedNode(int x, int y) {
        // Center of the clicked node
        Position clickedcoord = Units.coordinateByPosition(base, new Position(x, y));
//        System.out.println("Clicked near the node at position: (" + clickedcoord.x + ", " + clickedcoord.y + ")");

        Position highlevel = null;
        MultiNode node = null;
        int howfar = 0;
        while (node == null && howfar < Units.dimToCap(dim)) {
            Position deltum = Units.findSpotAround(howfar);
            Position delatcor = Units.coordinateByRingAndShift(1, deltum.x, deltum.y);

            highlevel = new Position (clickedcoord.x + delatcor.x, clickedcoord.y + delatcor.y);

            node = nodes.get(highlevel);
            howfar++;
        }

        if (node != null) {
            // Position of the center of the metanode in pixels
            Position deltaclicked = new Position (clickedcoord.x - highlevel.x, clickedcoord.y - highlevel.y);
            return node.getClickedNode(base, deltaclicked);
        }

        return null;
    }
}
