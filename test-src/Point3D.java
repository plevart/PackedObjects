import pele.packed.PackedField.pfInt;

/**
 * Example of a packed object extending another packed object
 */
public class Point3D extends Point {
    private static final pfInt<Point3D> z = new pfInt<>(Point3D.class);

    public Point3D(int _x, int _y, int _z) {
        super(_x, _y);
        z.set(this, _z);
    }

    public int getZ() {
        return z.get(this);
    }

    // test
    public static void main(String[] args) {
        Point3D p = new Point3D(3, 5, 7);
        System.out.println(p);
        System.out.println(p.type());
    }
}
