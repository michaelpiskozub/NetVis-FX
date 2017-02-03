package netvis.view.util.jogl.comets;

import netvis.model.Packet;
import netvis.view.util.jogl.gameengine.NodePainter;

import javax.media.opengl.GL2;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;

public class CometHeatNode extends HeatNode {
    private HashMap<String, Comet> entities;
    private boolean changed = true;

    public CometHeatNode(String texturename, String nodename) {
        super (texturename, nodename);

        entities = new HashMap<>();
    }

    public Collection<Comet> getEntities() {
        return entities.values();
    }

    public void addSatelite(String sip, int amp, double tilt) {
        // Look for the entity with the same name
        Comet find = entities.get(sip);

        if (find == null) {
            entities.put(sip, new Comet(amp, tilt));
        }
    }

    public void removeSatelite(String sip) {
        // Look for the entity
        Comet find = entities.get(sip);

        if (find != null) {
            entities.remove(sip);
            decreaseWarning();
        }
    }

    @Override
    public void draw(int base, NodePainter painter, GL2 gl) {
        // Draw one on top of the other
        painter.drawNode(base, (HeatNode) this, gl);
        painter.drawNode(base, (CometHeatNode) this, gl);
    }

    @Override
    public void updateWithData(Packet pp) {
        // Make their tilts nicely shifted
        addSatelite(pp.SOURCE_IP, 100, entities.size() * Math.PI / 10);

        super.updateWithData(pp);
    }

    @Override
    public void updateAnimation(long time) {
        for (Comet i : entities.values()) {
            i.step(time);
        }
    }

    @Override
    public void mouseClick(MouseEvent e) {
        super.mouseClick(e);
    }
}
