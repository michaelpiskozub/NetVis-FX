package netvis.view.util.jogl.gameengine;

public class Position {
    public int x;
    public int y;

    public Position(int posx, int posy) {
        x = posx;
        y = posy;
    }

    public Position(double posx, double posy) {
        x = (int) Math.round(posx);
        y = (int) Math.round(posy);
    }

    @Override
    public int hashCode() {
        return 1024 * 1024 * x + y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        Position that = (Position) obj;
        return (this.x == that.x) && (this.y == that.y);
    }
}
