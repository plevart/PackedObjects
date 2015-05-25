import pele.packed.PackedField.pfInt;
import pele.packed.PackedObject;

/**
 * Example of a packed object
 */
public class Point extends PackedObject {
    private static final pfInt<Point> x = new pfInt<>(Point.class);
    private static final pfInt<Point> y = new pfInt<>(Point.class);

    public Point(int _x, int _y) {
        x.setInt(this, _x);
        y.setInt(this, _y);
    }

    public int getX() {
        return x.getInt(this);
    }

    public void setX(int _x) {
        x.setInt(this, _x);
    }

    public int getY() {
        return y.get(this);
    }

    public void setY(int _y) {
        y.setInt(this, _y);
    }
}
