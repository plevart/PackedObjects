import pele.packed.PackedArray;

import static java.lang.Math.*;

/**
 * Created by peter on 5/25/15.
 */
public class Test {

    public static void main(String[] args) {

        boolean ok = true;

        PackedArray.OfInt ints = new PackedArray.OfInt(10);
        for (int i = 0; i < ints.length(); i++) {
            ints.setInt(i, i);
        }
        ok &= testEquals(1, ints.toString(),
            "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]");

        PackedArray.OfObject<Line> lines = new PackedArray.OfObject<>(Line.class, 3);
        for (int i = 0; i < lines.length(); i++) {
            lines.set(i, new Line(new Point(i, i), new Point(i * 2, i * 2)));
        }
        ok &= testEquals(2, lines.toString(),
            "[Line{p1=Point{x=0, y=0}, p2=Point{x=0, y=0}}, Line{p1=Point{x=1, y=1}, p2=Point{x=2, y=2}}, Line{p1=Point{x=2, y=2}, p2=Point{x=4, y=4}}]");

        // modify P2 points of all lines in array constructed above
        for (int i = 0; i < lines.length(); i++) {
            Point p2View = lines.getView(i).getP2View();
            p2View.setX(i * 3);
            p2View.setY(i * 4);
        }
        ok &= testEquals(3, lines.toString(),
            "[Line{p1=Point{x=0, y=0}, p2=Point{x=0, y=0}}, Line{p1=Point{x=1, y=1}, p2=Point{x=3, y=4}}, Line{p1=Point{x=2, y=2}, p2=Point{x=6, y=8}}]");

        // another way to modify points (P1 this time)
        for (int i = 0; i < lines.length(); i++) {
            lines.getView(i).copyP1From(new Point(-i * 3, -i * 4));
        }
        ok &= testEquals(4, lines.toString(),
            "[Line{p1=Point{x=0, y=0}, p2=Point{x=0, y=0}}, Line{p1=Point{x=-3, y=-4}, p2=Point{x=3, y=4}}, Line{p1=Point{x=-6, y=-8}, p2=Point{x=6, y=8}}]");

        Point3D p3d = new Point3D(3, 5, 7);
        Point pView = p3d.viewAs(Point.class);
        Point pCopy = p3d.copyAs(Point.class);
        ok &= testEquals(5, String.format("{pView=%s, pCopy=%s, p3d=%s}", pView, pCopy, p3d),
            "{pView=Point{x=3, y=5}, pCopy=Point{x=3, y=5}, p3d=Point3D{x=3, y=5, z=7}}");

        pView.setX(4);
        pCopy.setY(6);
        ok &= testEquals(6, String.format("{pView=%s, pCopy=%s, p3d=%s}", pView, pCopy, p3d),
            "{pView=Point{x=4, y=5}, pCopy=Point{x=3, y=6}, p3d=Point3D{x=4, y=5, z=7}}");

        Curve c = new Curve();
        double r = 100d;
        for (int i = 0; i < 8; i++) {
            c.addPoint(new Point(
                (int) (r * cos(2d * PI * i / 8)),
                (int) (r * sin(2d * PI * i / 8))
            ));
        }
        ok &= testEquals(7, c.toString(),
            "Curve{size=8, points=[Point{x=100, y=0}, Point{x=70, y=70}, Point{x=0, y=100}, Point{x=-70, y=70}, Point{x=-100, y=0}, Point{x=-70, y=-70}, Point{x=0, y=-100}, Point{x=70, y=-70}, Point{x=0, y=0}, Point{x=0, y=0}]}");

        ok &= testEquals(8, c.getPointsView().toString(),
            "[Point{x=100, y=0}, Point{x=70, y=70}, Point{x=0, y=100}, Point{x=-70, y=70}, Point{x=-100, y=0}, Point{x=-70, y=-70}, Point{x=0, y=-100}, Point{x=70, y=-70}]");

        if (ok) {
            System.out.println("All tests OK.");
        } else {
            throw new AssertionError("Some tests failed!");
        }
    }

    static boolean testEquals(int testIndex, String result, String expected) {
        if (result.equals(expected)) {
            System.out.println("test#" + testIndex + " OK");
            return true;
        } else {
            System.out.println("test#" + testIndex +
                "\n  EXPECTED: " + expected +
                "\n       GOT: " + result);
            return false;
        }
    }
}
