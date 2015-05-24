# PackedObjects
PackedObjects API

PackedObject API implements the idea of
<a href="http://www.oracle.com/technetwork/java/jvmls2013sciam-2013525.pdf">Packed Objects</a>
developed by IBM in experimental J9 JVM, but doesn't need VM changes - just Unsafe.

Here's how a simple packed object can be implemented:

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
    }

Packed objects can embed other packed objects:

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
    }

Or extend them:

    public class Point3D extends Point {
        private static final pfInt<Point3D> z = new pfInt<>(Point3D.class);

        public Point3D(int _x, int _y, int _z) {
            super(_x, _y);
            z.set(this, _z);
        }

        public int getZ() {
            return z.get(this);
        }
    }

Besides packed objects, there are also packed arrays of packed objects or primitives:

    PackedArray.OfObject<Line> lines = new PackedArray.OfObject<>(Line.class, 10);
    for (int i = 0; i < lines.length(); i++) {
        lines.set(i, new Line(new Point(i, i), new Point(i * 2, i * 2)));
    }
    System.out.println(lines);
