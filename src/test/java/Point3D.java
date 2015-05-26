import pele.packed.PackedField.pfInt;

/**
 * Example of a packed object extending another packed object
 */
public class Point3D extends Point {
    private static final pfInt<Point3D> z = new pfInt<>(Point3D.class);

    public Point3D(int _x, int _y, int _z) {
        super(_x, _y);
        z.setInt(this, _z);
    }

    public int getZ() {
        return z.getInt(this);
    }

    public void setZ(int _z) {
        z.setInt(this, _z);
    }
}
