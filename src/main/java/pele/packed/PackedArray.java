/*
 * Written by Peter.Levart@gmail.com and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */
package pele.packed;

/**
 * PackedArray is an array of primitives or PackedObject(s).
 */
public abstract class PackedArray<CT> extends Packed {

    private final int length;

    /**
     * Constructor for "zero" PackedArray(s)
     */
    PackedArray(PackedClass<? extends PackedArray<CT>> arrayType, int length) {
        super(arrayType.arraySize(checkLength(length)));
        this.length = length;
    }

    /**
     * @return the length of the packed array.
     */
    public final int length() {
        return length;
    }

    /**
     * @return the type of the packed array.
     */
    @Override
    public abstract PackedClass<? extends PackedArray<CT>> type();

    /**
     * Grabs and returns the element at given {@code index}.
     * <p>
     * For packed arrays of primitives, this method returns a boxed object.
     * To get the primitive value without boxing, use specific methods
     * of subclasses:
     * {@link OfBoolean#getBoolean(int)},
     * {@link OfByte#getByte(int)}, ...
     * <p>
     * For packed arrays of PackedObject(s), this method returns a view over
     * the element at given {@code index}. To explicitly request a view
     * or a copy of the element, use subclass methods:
     * {@link OfObject#getView(int)},
     * {@link OfObject#getCopy(int)}.
     *
     * @param index the index of the element to return
     * @return an element at given {@code index}
     * @throws ArrayIndexOutOfBoundsException if given {@code index} is not:
     *                                        {@code 0 <= index < }{@link #length()}
     */
    public abstract CT get(int index);

    /**
     * Sets the element at given {@code index} to given {@code value}.
     * <p>
     * For packed arrays of primitives, this method takes a boxed value and
     * un-boxes it (throwing {@code NullPointerException} if it is {@code null}).
     * To pass primitive values directly, use specific methods of subclasses:
     * {@link OfBoolean#setBoolean(int, boolean)},
     * {@link OfByte#setByte(int, byte)}, ...
     * <p>
     * For packed arrays of PackedObject(s), this method copies the given
     * {@code value} to the element at given {@code index}. To request this
     * explicitly, use equivalent subclass method:
     * {@link OfObject#copyFrom(int, PackedObject)}.
     *
     * @param index the index of the element to set
     * @param value the value to set the element to
     * @throws ArrayIndexOutOfBoundsException if given {@code index} is not:
     *                                        {@code 0 <= index < }{@link #length()}
     * @throws NullPointerException           if given {@code value} is null
     */
    public abstract void set(int index, CT value);

    /**
     * Returns a view of this packed array's range.
     * The initial index of the range - {@code from} - must lie between zero
     * and {@link #length()}, inclusive. The final index of the range -
     * {@code to} - must lie between {@code from} and {@link #length()}, inclusive.
     * The length of the returned array will be {@code to - from}.
     *
     * @param from the initial index of the range, inclusive
     * @param to   the final index of the range, exclusive.
     * @return a packed array representing the view of the specified range
     * of this packed array
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *                                        or {@code from > }{@link #length()}
     *                                        or {@code to < 0}
     *                                        or {@code to > }{@link #length()}
     * @throws IllegalArgumentException       if {@code from > to}
     */
    public PackedArray<CT> viewOfRange(int from, int to) {
        checkRangeIndexes(from, to);
        int indexScale = type().getComponentType().getIndexScale();
        return getArrayViewU(type(), indexScale * from, to - from);
    }

    /**
     * Returns a copy of this packed array's range.
     * The initial index of the range - {@code from} - must lie between zero
     * and {@link #length()}, inclusive. The final index of the range -
     * {@code to} - must lie between {@code from} and {@link #length()}, inclusive.
     * The length of the returned array will be {@code to - from}.
     *
     * @param from the initial index of the range to be copied, inclusive
     * @param to   the final index of the range to be copied, exclusive.
     * @return a packed array containing the copy of the specified range
     * from this packed array
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *                                        or {@code from > }{@link #length()}
     *                                        or {@code to < 0}
     *                                        or {@code to > }{@link #length()}
     * @throws IllegalArgumentException       if {@code from > to}
     */
    public PackedArray<CT> copyOfRange(int from, int to) {
        checkRangeIndexes(from, to);
        int indexScale = type().getComponentType().getIndexScale();
        return getArrayCopyU(type(), indexScale * from, to - from);
    }

    /**
     * Returns a {@code String} representation of the contents of the array.
     * The format is equivalent to what is returned by
     * {@link java.util.Arrays#toString} methods.
     *
     * @return a {@code String} representation of the array
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    static int checkLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Negative array length.");
        }
        return length;
    }

    static int arraySize(PackedClass<?> componentType, int length) {
        return (length == 0)
            ? 0
            : (length - 1) * componentType.getIndexScale() + componentType.getSize();
    }

    int checkIndex(int index) {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return index;
    }

    void checkRangeIndexes(int from, int to) {
        if (from < 0 || from > length()) {
            throw new ArrayIndexOutOfBoundsException("'from' index out of range: " + from);
        }
        if (to < 0 || to > length()) {
            throw new ArrayIndexOutOfBoundsException("'to' index out of range: " + to);
        }
        if (from > to) {
            throw new IllegalArgumentException("'from' index: " + from + " > 'to' index: " + to);
        }
    }

    static abstract class OfPrimitive<CT> extends PackedArray<CT> {
        OfPrimitive(PackedClass<? extends OfPrimitive<CT>> arrayType, int length) {
            super(arrayType, length);
        }
    }

    public static final class OfBoolean extends OfPrimitive<Boolean> {
        public static final PackedClass<OfBoolean> TYPE
            = PackedClass.forClass(OfBoolean.class).withComponent(boolean.class);

        public OfBoolean(int length) {
            super(TYPE, length);
        }

        public boolean getBoolean(int index) {
            return getBooleanU(checkIndex(index));
        }

        public boolean setBoolean(int index, boolean value) {
            putBooleanU(checkIndex(index), value);
            return value;
        }

        @Override
        public PackedClass<OfBoolean> type() {
            return TYPE;
        }

        @Override
        public Boolean get(int index) {
            return getBoolean(index);
        }

        @Override
        public void set(int index, Boolean value) {
            setBoolean(index, value);
        }

        @Override
        public OfBoolean viewOfRange(int from, int to) {
            return (OfBoolean) super.viewOfRange(from, to);
        }

        @Override
        public OfBoolean copyOfRange(int from, int to) {
            return (OfBoolean) super.copyOfRange(from, to);
        }
    }

    public static final class OfByte extends OfPrimitive<Byte> {
        public static final PackedClass<OfByte> TYPE
            = PackedClass.forClass(OfByte.class).withComponent(byte.class);

        public OfByte(int length) {
            super(TYPE, length);
        }

        public byte getByte(int index) {
            return getByteU(checkIndex(index));
        }

        public byte setByte(int index, byte value) {
            putByteU(checkIndex(index), value);
            return value;
        }

        @Override
        public PackedClass<OfByte> type() {
            return TYPE;
        }

        @Override
        public Byte get(int index) {
            return getByte(index);
        }

        @Override
        public void set(int index, Byte value) {
            setByte(index, value);
        }

        @Override
        public OfByte viewOfRange(int from, int to) {
            return (OfByte) super.viewOfRange(from, to);
        }

        @Override
        public OfByte copyOfRange(int from, int to) {
            return (OfByte) super.copyOfRange(from, to);
        }
    }

    public static final class OfChar extends OfPrimitive<Character> {
        public static final PackedClass<OfChar> TYPE
            = PackedClass.forClass(OfChar.class).withComponent(char.class);

        public OfChar(int length) {
            super(TYPE, length);
        }

        public char getChar(int index) {
            return getCharU(checkIndex(index) << 1);
        }

        public char setChar(int index, char value) {
            putCharU(checkIndex(index) << 1, value);
            return value;
        }

        @Override
        public PackedClass<OfChar> type() {
            return TYPE;
        }

        @Override
        public Character get(int index) {
            return getChar(index);
        }

        @Override
        public void set(int index, Character value) {
            setChar(index, value);
        }

        @Override
        public OfChar viewOfRange(int from, int to) {
            return (OfChar) super.viewOfRange(from, to);
        }

        @Override
        public OfChar copyOfRange(int from, int to) {
            return (OfChar) super.copyOfRange(from, to);
        }
    }

    public static final class OfShort extends OfPrimitive<Short> {
        public static final PackedClass<OfShort> TYPE
            = PackedClass.forClass(OfShort.class).withComponent(short.class);

        public OfShort(int length) {
            super(TYPE, length);
        }

        public short getShort(int index) {
            return getShortU(checkIndex(index) << 1);
        }

        public short setShort(int index, short value) {
            putShortU(checkIndex(index) << 1, value);
            return value;
        }

        @Override
        public PackedClass<OfShort> type() {
            return TYPE;
        }

        @Override
        public Short get(int index) {
            return getShort(index);
        }

        @Override
        public void set(int index, Short value) {
            setShort(index, value);
        }

        @Override
        public OfShort viewOfRange(int from, int to) {
            return (OfShort) super.viewOfRange(from, to);
        }

        @Override
        public OfShort copyOfRange(int from, int to) {
            return (OfShort) super.copyOfRange(from, to);
        }
    }

    public static final class OfInt extends OfPrimitive<Integer> {
        public static final PackedClass<OfInt> TYPE
            = PackedClass.forClass(OfInt.class).withComponent(int.class);

        public OfInt(int length) {
            super(TYPE, length);
        }

        public int getInt(int index) {
            return getIntU(checkIndex(index) << 2);
        }

        public int setInt(int index, int value) {
            putIntU(checkIndex(index) << 2, value);
            return value;
        }

        @Override
        public PackedClass<OfInt> type() {
            return TYPE;
        }

        @Override
        public Integer get(int index) {
            return getInt(index);
        }

        @Override
        public void set(int index, Integer value) {
            setInt(index, value);
        }

        @Override
        public OfInt viewOfRange(int from, int to) {
            return (OfInt) super.viewOfRange(from, to);
        }

        @Override
        public OfInt copyOfRange(int from, int to) {
            return (OfInt) super.copyOfRange(from, to);
        }
    }

    public static final class OfLong extends OfPrimitive<Long> {
        public static final PackedClass<OfLong> TYPE
            = PackedClass.forClass(OfLong.class).withComponent(long.class);

        public OfLong(int length) {
            super(TYPE, length);
        }

        public long getLong(int index) {
            return getLongU(checkIndex(index) << 3);
        }

        public long setLong(int index, long value) {
            putLongU(checkIndex(index) << 3, value);
            return value;
        }

        @Override
        public PackedClass<OfLong> type() {
            return TYPE;
        }

        @Override
        public Long get(int index) {
            return getLong(index);
        }

        @Override
        public void set(int index, Long value) {
            setLong(index, value);
        }

        @Override
        public OfLong viewOfRange(int from, int to) {
            return (OfLong) super.viewOfRange(from, to);
        }

        @Override
        public OfLong copyOfRange(int from, int to) {
            return (OfLong) super.copyOfRange(from, to);
        }
    }

    public static final class OfFloat extends OfPrimitive<Float> {
        public static final PackedClass<OfFloat> TYPE
            = PackedClass.forClass(OfFloat.class).withComponent(float.class);

        public OfFloat(int length) {
            super(TYPE, length);
        }

        public float getFloat(int index) {
            return getFloatU(checkIndex(index) << 2);
        }

        public float setFloat(int index, float value) {
            putFloatU(checkIndex(index) << 2, value);
            return value;
        }

        @Override
        public PackedClass<OfFloat> type() {
            return TYPE;
        }

        @Override
        public Float get(int index) {
            return getFloat(index);
        }

        @Override
        public void set(int index, Float value) {
            setFloat(index, value);
        }

        @Override
        public OfFloat viewOfRange(int from, int to) {
            return (OfFloat) super.viewOfRange(from, to);
        }

        @Override
        public OfFloat copyOfRange(int from, int to) {
            return (OfFloat) super.copyOfRange(from, to);
        }
    }

    public static final class OfDouble extends OfPrimitive<Double> {
        public static final PackedClass<OfDouble> TYPE
            = PackedClass.forClass(OfDouble.class).withComponent(double.class);

        public OfDouble(int length) {
            super(TYPE, length);
        }

        public double getDouble(int index) {
            return getDoubleU(checkIndex(index) << 3);
        }

        public double setDouble(int index, double value) {
            putDoubleU(checkIndex(index) << 3, value);
            return value;
        }

        @Override
        public PackedClass<OfDouble> type() {
            return TYPE;
        }

        @Override
        public Double get(int index) {
            return getDouble(index);
        }

        @Override
        public void set(int index, Double value) {
            setDouble(index, value);
        }

        @Override
        public OfDouble viewOfRange(int from, int to) {
            return (OfDouble) super.viewOfRange(from, to);
        }

        @Override
        public OfDouble copyOfRange(int from, int to) {
            return (OfDouble) super.copyOfRange(from, to);
        }
    }

    public static final class OfObject<CT extends PackedObject> extends PackedArray<CT> {
        private static final PackedClass<OfObject> BASIC_TYPE = PackedClass.forClass(OfObject.class);

        public static <CT2 extends PackedObject>
        PackedClass<OfObject<CT2>> typeWithComponent(Class<CT2> componentClass) {
            return BASIC_TYPE.withComponent(componentClass);
        }

        private final PackedClass<OfObject<CT>> type;

        public OfObject(Class<CT> componentClass, int length) {
            this(typeWithComponent(componentClass), length);
        }

        private OfObject(PackedClass<OfObject<CT>> type, int length) {
            super(type, length);
            this.type = type;
        }

        public CT getView(int index) {
            @SuppressWarnings("unchecked")
            PackedClass<CT> componentType = (PackedClass) type.getComponentType();
            return getViewU(componentType, checkIndex(index) * componentType.getIndexScale(), componentType.getSize());
        }

        public CT getCopy(int index) {
            @SuppressWarnings("unchecked")
            PackedClass<CT> componentType = (PackedClass) type.getComponentType();
            return getCopyU(componentType, checkIndex(index) * componentType.getIndexScale(), componentType.getSize());
        }

        public CT copyFrom(int index, CT source) {
            PackedClass<?> componentType = type.getComponentType();
            copyFromU(source, checkIndex(index) * componentType.getIndexScale(), componentType.getSize());
            return source;
        }

        @Override
        public PackedClass<OfObject<CT>> type() {
            return type;
        }

        @Override
        public CT get(int index) {
            return getView(index);
        }

        @Override
        public void set(int index, CT value) {
            copyFrom(index, value);
        }

        @Override
        public OfObject<CT> viewOfRange(int from, int to) {
            return (OfObject<CT>) super.viewOfRange(from, to);
        }

        @Override
        public OfObject<CT> copyOfRange(int from, int to) {
            return (OfObject<CT>) super.copyOfRange(from, to);
        }

        // Unsafe machinery

        @Override
        void initLengthAndType(int length, PackedClass<?> type) {
            super.initLengthAndType(length, type);
            U.putOrderedObject(this, TYPE, type);
        }

        private static final long TYPE;

        static {
            try {
                TYPE = U.objectFieldOffset(
                    PackedArray.OfObject.class.getDeclaredField("type"));
            } catch (Exception e) {
                throw new InternalError(e);
            }
        }
    }


    // Unsafe machinery

    void initLengthAndType(int length, PackedClass<?> type) {
        U.putOrderedInt(this, LENGTH, length);
    }

    private static final long LENGTH;

    static {
        try {
            LENGTH = U.objectFieldOffset(
                PackedArray.class.getDeclaredField("length"));
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }
}
