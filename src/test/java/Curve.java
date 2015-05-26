import pele.packed.PackedArray;
import pele.packed.PackedArray.OfObject;
import pele.packed.PackedField.pfArray;
import pele.packed.PackedField.pfInt;
import pele.packed.PackedObject;

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
}
