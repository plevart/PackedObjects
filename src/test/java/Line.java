import pele.packed.PackedField.pfObject;
import pele.packed.PackedObject;

/**
 * Example of a packed object embedding other packed objects
 */
public class Line extends PackedObject {
    private static final pfObject<Point, Line> p1 =
        new pfObject<>(Point.class, Line.class);
    private static final pfObject<Point, Line> p2 =
        new pfObject<>(Point.class, Line.class);

    public Line(Point _p1, Point _p2) {
        p1.copyFrom(this, _p1);
        p2.copyFrom(this, _p2);
    }

    public Point getP1View() {
        return p1.getView(this);
    }

    public Point getP1Copy() {
        return p1.getCopy(this);
    }

    public void copyP1From(Point _p1) {
        p1.copyFrom(this, _p1);
    }

    public Point getP2View() {
        return p2.getView(this);
    }

    public Point getP2Copy() {
        return p2.getCopy(this);
    }

    public void copyP2From(Point _p2) {
        p2.copyFrom(this, _p2);
    }
}
