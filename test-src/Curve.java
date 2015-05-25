import pele.packed.PackedArray;
import pele.packed.PackedArray.OfObject;
import pele.packed.PackedField.pfArray;
import pele.packed.PackedField.pfInt;
import pele.packed.PackedObject;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Example of a packed object embedding an int field and a fix-sized array of
 * packed objects.
 */
public class Curve extends PackedObject {
    private static final pfInt<Curve> size =
        new pfInt<>(Curve.class);
    private static final pfArray<OfObject<Point>, Curve> points =
        new pfArray<>(OfObject.typeWithComponent(Point.class), 10, Curve.class);

    public int getSize() {
        return size.getInt(this);
    }

    public int getCapacity() {
        return points.length();
    }

    public void addPoint(Point p) {
        int n = size.getInt(this);
        points.getView(this).copyFrom(n, p);
        size.setInt(this, n + 1);
    }

    public PackedArray.OfObject<Point> getPoints() {
        return points.getView(this).viewOfRange(0, getSize());
    }

    public static void main(String[] args) {
        Curve c = new Curve();
        double r = 100d;
        for (int i = 0; i < 8; i++) {
            c.addPoint(new Point(
                (int) (r * cos(2d * PI * i / 8)),
                (int) (r * sin(2d * PI * i / 8))
            ));
        }
        System.out.println("curve: " + c);
        System.out.println("points: " + c.getPoints());
        System.out.println("curve size in bytes: " + c.type().getSize());
    }
}
