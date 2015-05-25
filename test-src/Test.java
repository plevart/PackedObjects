import pele.packed.PackedArray;

/**
 * Created by peter on 5/25/15.
 */
public class Test {
    public static void main(String[] args) {

        PackedArray.OfInt ints = new PackedArray.OfInt(10);
        for (int i = 0; i < ints.length(); i++) {
            ints.setInt(i, i);
        }
        System.out.println(ints);

        PackedArray.OfObject<Line> lines = new PackedArray.OfObject<>(Line.class, 3);
        for (int i = 0; i < lines.length(); i++) {
            lines.set(i, new Line(new Point(i, i), new Point(i * 2, i * 2)));
        }
        System.out.println(lines);

        // modify P2 points of all lines in array constructed above
        for (int i = 0; i < lines.length(); i++) {
            Point p2View = lines.getView(i).getP2View();
            p2View.setX(i * 3);
            p2View.setY(i * 4);
        }
        System.out.println(lines);

        // another way to modify points (P1 this time)
        for (int i = 0; i < lines.length(); i++) {
            lines.getView(i).copyP1From(new Point(-i * 3, -i * 4));
        }
        System.out.println(lines);

        Point3D p3d = new Point3D(3, 5, 7);
        Point pView = p3d.viewAs(Point.class);
        Point pCopy = p3d.copyAs(Point.class);
        System.out.printf("pView: %s, pCopy: %s, p3d: %s\n", pView, pCopy, p3d);
        pView.setX(4);
        pCopy.setY(6);
        System.out.printf("pView: %s, pCopy: %s, p3d: %s\n", pView, pCopy, p3d);
    }
}
