package netvis.view.util.jogl.maps;

import netvis.view.util.jogl.gameengine.Node;
import netvis.view.util.jogl.gameengine.ValueAnimator;

public abstract class Map {
    public ValueAnimator viewfieldanim;
    public ValueAnimator middlex;
    public ValueAnimator middley;

    // Basic size of the node
    public int base = 400;
    public int width;
    public int height;

    public Map() {
        viewfieldanim = new ValueAnimator(5.0);
        middlex = new ValueAnimator(0.0);
        middley = new ValueAnimator(0.0);
    }

    public abstract Node findClickedNode(int x, int y);

    public double getViewfield() {
        return viewfieldanim.toDouble();
    }

    public double getMX () {
        return middlex.toDouble();
    }

    public double getMY () {
        return middley.toDouble();
    }

    public void zoomIn() {
        double viewfield = viewfieldanim.getGoal();
        viewfieldanim.moveTo(viewfield * 0.9, 100);
    }

    public void zoomOut() {
        double viewfield = viewfieldanim.getGoal();
        viewfieldanim.moveTo(viewfield * 1.1, 100);
    }

    public double zoomOn() {
        double screenratio = (1.0 * width) / height;
        if (screenratio < Math.sqrt(3.0)) {
            return (2*base * Math.sqrt(3.0)) / width;
        }
        return (2.0 * base) / height;
    }
}
