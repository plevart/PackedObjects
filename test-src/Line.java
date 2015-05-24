import pele.packed.PackedArray;
import pele.packed.PackedField.pfObject;
import pele.packed.PackedObject;

/**
 * Example of a packed object embedding other packed objects
 */
public class Line extends PackedObject {
    private static final pfObject<Point, Line> p1 = new pfObject<>(Point.class, Line.class);
    private static final pfObject<Point, Line> p2 = new pfObject<>(Point.class, Line.class);

    public Line(Point _p1, Point _p2) {
        p1.set(this, _p1);
        p2.set(this, _p2);
    }

    public Point getP1() {
        return p1.get(this);
    }

    public Point getP2() {
        return p2.get(this);
    }

    // test
    public static void main(String[] args) {
        Line l = new Line(new Point(0, 0), new Point3D(1, 2, 3));
        System.out.println(l);

        PackedArray.OfObject<Line> lines = new PackedArray.OfObject<>(Line.class, 10);
        for (int i = 0; i < lines.length(); i++) {
            lines.set(i, new Line(new Point(i, i), new Point(i * 2, i * 2)));
        }
        System.out.println(lines);
    }
}
