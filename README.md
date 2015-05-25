# PackedObjects
PackedObjects API

PackedObject API implements the idea of
<a href="http://www.oracle.com/technetwork/java/jvmls2013sciam-2013525.pdf">Packed Objects</a>
developed by IBM in experimental J9 JVM, but doesn't need VM changes - just Unsafe.

Here's how a simple packed object can be implemented:

```Java
    public class Point extends PackedObject {
        private static final pfInt<Point> x = new pfInt<>(Point.class);
        private static final pfInt<Point> y = new pfInt<>(Point.class);

        public Point(int _x, int _y) {
            x.setInt(this, _x);
            y.setInt(this, _y);
        }

        public int getX() {
            return x.getInt(this);
        }

        public void setX(int _x) {
            x.setInt(this, _x);
        }

        public int getY() {
            return y.get(this);
        }

        public void setY(int _y) {
            y.setInt(this, _y);
        }
    }
```

Packed objects can embed other packed objects:

```Java
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
```

Or extend them:

```Java
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
```

Besides packed objects, there are also packed arrays of primitives or packed objects:

```Java
    PackedArray.OfInt ints = new PackedArray.OfInt(10);
    for (int i = 0; i < ints.length(); i++) {
        ints.setInt(i, i);
    }
    System.out.println(ints);
```

    >>> [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

```Java
    PackedArray.OfObject<Line> lines = new PackedArray.OfObject<>(Line.class, 3);
    for (int i = 0; i < lines.length(); i++) {
        lines.set(i, new Line(new Point(i, i), new Point(i * 2, i * 2)));
    }
    System.out.println(lines);
```

    >>> [Line{p1=Point{x=0, y=0}, p2=Point{x=0, y=0}},
         Line{p1=Point{x=1, y=1}, p2=Point{x=2, y=2}},
         Line{p1=Point{x=2, y=2}, p2=Point{x=4, y=4}}]

...showing that PackedArray and PackedObject define a meaningful default
toString() method.

PackedObject and PackedArray API allows exposing object views of a particular
field/component of the containing object/array which can be mutated:

```Java
    // modify P2 points of all lines in array constructed above
    for (int i = 0; i < lines.length(); i++) {
        Point p2View = lines.getView(i).getP2View();
        p2View.setX(i * 3);
        p2View.setY(i * 4);
    }
    System.out.println(lines);
```

    >>> [Line{p1=Point{x=0, y=0}, p2=Point{x=0, y=0}},
         Line{p1=Point{x=1, y=1}, p2=Point{x=3, y=4}},
         Line{p1=Point{x=2, y=2}, p2=Point{x=6, y=8}}]

```Java
    // another way to modify points (P1 this time)
    for (int i = 0; i < lines.length(); i++) {
        lines.getView(i).copyP1From(new Point(-i * 3, -i * 4));
    }
    System.out.println(lines);
```

    >>> [Line{p1=Point{x=0, y=0}, p2=Point{x=0, y=0}},
         Line{p1=Point{x=-3, y=-4}, p2=Point{x=3, y=4}},
         Line{p1=Point{x=-6, y=-8}, p2=Point{x=6, y=8}}]

PackedObject(s) themselves can be viewed as their superclasses:

```Java
    Point3D p3d = new Point3D(3, 5, 7);
    Point pView = p3d.viewAs(Point.class);
    Point pCopy = p3d.copyAs(Point.class);
    System.out.printf("pView: %s, pCopy: %s, p3d: %s\n", pView, pCopy, p3d);
```

    >>> pView: Point{x=3, y=5},
        pCopy: Point{x=3, y=5},
        p3d: Point3D{x=3, y=5, z=7}

```Java
    pView.setX(4);
    pCopy.setY(6);
    System.out.printf("pView: %s, pCopy: %s, p3d: %s\n", pView, pCopy, p3d);
```

    >>> pView: Point{x=4, y=5},
        pCopy: Point{x=3, y=6},
        p3d: Point3D{x=4, y=5, z=7}
