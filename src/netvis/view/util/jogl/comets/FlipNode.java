package netvis.view.util.jogl.comets;

import netvis.model.Packet;
import netvis.view.util.jogl.gameengine.*;

import javax.media.opengl.GL2;
import java.awt.event.MouseEvent;

public class FlipNode extends Node {
    private int framebufferid;
    private Node front;
    private Node back;
    private boolean frontSeen = true;
    private ValueAnimator rotation;

    public FlipNode(Node f, Node b) {
        super(f.getName());

        framebufferid = FramebufferPool.generate();
        rotation = new ValueAnimator(0.0);
        front = f;
        back = b;
    }

    public Framebuffer GetFramebuffer() {
        return FramebufferPool.get(framebufferid);
    }

    public Node getFrontNode() {
        return front;
    }

    public Node getBackNode() {
        return back;
    }

    public double getRotation() {
        return rotation.toDouble();
    }

    public boolean isFlat () {
        double rot = rotation.toDouble();
        while (rot > 359.0) {
            rot -= 360.0;
        }
        while (rot < -1.0) {
            rot += 360.0;
        }

        double epsilon = 0.01;
        if (rot < 180.0 + epsilon && rot > 180.0 - epsilon) {
            return true;
        }
        if (rot < 0.0 + epsilon && rot > 0.0 - epsilon) {
            return true;
        }

        return false;
    }

    public Node getSide () {
        double rot = rotation.toDouble();
        while (rot > 180.0)
            rot -= 360.0;

        if (rot > -90.0 && rot < 90.0)
            return front;
        else
            return back;

    }

    public void flip() {
        frontSeen = !frontSeen;
        if (frontSeen) {
            rotation.moveTo(rotation.getGoal() + 180.0, 1000);
        } else {
            rotation.moveTo(rotation.getGoal() - 180.0, 1000);
        }
    }

    @Override
    public void draw(int base, NodePainter painter, GL2 gl) {
        // If one side is visible flat - just redirect rendering to this face
        // rendering procedures
        if (isFlat()) {
            Node visside = getSide();
            visside.draw(base, painter, gl);
        } else {
            // Otherwise draw the properly transformed face
            painter.drawNode(base, this, gl);
        }
    }

    @Override
    public void updateWithData(Packet pp) {
        front.updateWithData(pp);
        back.updateWithData(pp);
    }

    @Override
    public void updateAnimation(long time) {
        front.updateAnimation(time);
        back.updateAnimation(time);
    }

    @Override
    public int priority() {
        // Return the bigger one of the two
        return Math.max(front.priority(), back.priority());
    }

    @Override
    public void mouseClick(MouseEvent e) {
        if (e.getClickCount() == 2) {
            flip();
        } else {
            getSide().mouseClick(e);
        }
    }
}
