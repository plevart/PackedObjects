import pele.packed.PackedArray;
import pele.packed.PackedField.pfInt;
import pele.packed.PackedObject;

/**
 * Example of a packed object
 */
public class Point extends PackedObject {
    private static final pfInt<Point> x = new pfInt<>(Point.class);
    private static final pfInt<Point> y = new pfInt<>(Point.class);

    public Point(int _x, int _y) {
        x.set(this, _x);
        y.set(this, _y);
    }

    public int getX() {
        return x.get(this);
    }

    public int getY() {
        return y.get(this);
    }

    // test
    public static void main(String[] args) {
        Point p = new Point(3, 5);
        System.out.println(p);

        PackedArray.OfObject<Point> points = new PackedArray.OfObject<>(Point.class, 5);
        for (int i = 0; i < points.length(); i++) {
            points.set(i, new Point(i * 2, i * 3));
        }
        System.out.println(points);
    }
}
