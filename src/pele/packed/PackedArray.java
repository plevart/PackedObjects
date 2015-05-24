package pele.packed;

/**
 * PackedArray is an array of primitives or PackedObject(s).
 *
 * @author peter.levart@gmail.com
 */
public abstract class PackedArray<CT> extends Packed {

    private final int length;

    /**
     * Constructor for "zero" PackedArray(s)
     */
    PackedArray(PackedClass<? extends PackedArray, CT> arrayType, int length) {
        super(arrayType.arraySize(checkLength(length)));
        this.length = length;
    }

    /**
     * Factory for PackedArray views.
     */
    static <T, PA extends PackedArray<T>> PA newArrayView(PackedClass<PA, T> arrayType, byte[] target, int offset, int length) {
        PA array = newView(arrayType, target, offset, arrayType.arraySize(length));
        U.putOrderedInt(array, LENGTH, length);
        return array;
    }

    /**
     * Factory for PackedArray copies.
     */
    static <T, PA extends PackedArray<T>> PA newArrayCopy(PackedClass<PA, T> arrayType, byte[] target, int offset, int length) {
        PA array = newCopy(arrayType, target, offset, arrayType.arraySize(length));
        U.putOrderedInt(array, LENGTH, length);
        return array;
    }

    /**
     * @return the length of the packed array.
     */
    public final int length() {
        return length;
    }

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

    static int arraySize(PackedClass<?, ?> componentType, int length) {
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

    static abstract class OfPrimitive<CT> extends PackedArray<CT> {
        OfPrimitive(PackedClass<? extends OfPrimitive, CT> arrayType, int length) {
            super(arrayType, length);
        }
    }

    public static final class OfBoolean extends OfPrimitive<Boolean> {
        public static final PackedClass<OfBoolean, Boolean> TYPE
            = PackedClass.forClass(OfBoolean.class).withComponent(boolean.class);

        public OfBoolean(int length) {
            super(TYPE, length);
        }

        public boolean getBoolean(int index) {
            return U.getBoolean(target, unsafeOffset() + checkIndex(index));
        }

        public boolean setBoolean(int index, boolean value) {
            U.putBoolean(target, unsafeOffset() + checkIndex(index), value);
            return value;
        }

        @Override
        public PackedClass<OfBoolean, Boolean> type() {
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
    }

    public static final class OfByte extends OfPrimitive<Byte> {
        public static final PackedClass<OfByte, Byte> TYPE
            = PackedClass.forClass(OfByte.class).withComponent(byte.class);

        public OfByte(int length) {
            super(TYPE, length);
        }

        public byte getByte(int index) {
            return U.getByte(target, unsafeOffset() + checkIndex(index));
        }

        public byte setByte(int index, byte value) {
            U.putByte(target, unsafeOffset() + checkIndex(index), value);
            return value;
        }

        @Override
        public PackedClass<OfByte, Byte> type() {
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
    }

    public static final class OfChar extends OfPrimitive<Character> {
        public static final PackedClass<OfChar, Character> TYPE
            = PackedClass.forClass(OfChar.class).withComponent(char.class);

        public OfChar(int length) {
            super(TYPE, length);
        }

        public char getChar(int index) {
            return U.getChar(target, unsafeOffset() + (checkIndex(index) << 1));
        }

        public char setChar(int index, char value) {
            U.putChar(target, unsafeOffset() + (checkIndex(index) << 1), value);
            return value;
        }

        @Override
        public PackedClass<OfChar, Character> type() {
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
    }

    public static final class OfShort extends OfPrimitive<Short> {
        public static final PackedClass<OfShort, Short> TYPE
            = PackedClass.forClass(OfShort.class).withComponent(short.class);

        public OfShort(int length) {
            super(TYPE, length);
        }

        public short getShort(int index) {
            return U.getShort(target, unsafeOffset() + (checkIndex(index) << 1));
        }

        public short setShort(int index, short value) {
            U.putShort(target, unsafeOffset() + (checkIndex(index) << 1), value);
            return value;
        }

        @Override
        public PackedClass<OfShort, Short> type() {
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
    }

    public static final class OfInt extends OfPrimitive<Integer> {
        public static final PackedClass<OfInt, Integer> TYPE
            = PackedClass.forClass(OfInt.class).withComponent(int.class);

        public OfInt(int length) {
            super(TYPE, length);
        }

        public int getInt(int index) {
            return U.getInt(target, unsafeOffset() + (checkIndex(index) << 2));
        }

        public int setInt(int index, int value) {
            U.putInt(target, unsafeOffset() + (checkIndex(index) << 2), value);
            return value;
        }

        @Override
        public PackedClass<OfInt, Integer> type() {
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
    }

    public static final class OfLong extends OfPrimitive<Long> {
        public static final PackedClass<OfLong, Long> TYPE
            = PackedClass.forClass(OfLong.class).withComponent(long.class);

        public OfLong(int length) {
            super(TYPE, length);
        }

        public long getLong(int index) {
            return U.getLong(target, unsafeOffset() + (checkIndex(index) << 3));
        }

        public long setLong(int index, long value) {
            U.putLong(target, unsafeOffset() + (checkIndex(index) << 3), value);
            return value;
        }

        @Override
        public PackedClass<OfLong, Long> type() {
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
    }

    public static final class OfFloat extends OfPrimitive<Float> {
        public static final PackedClass<OfFloat, Float> TYPE
            = PackedClass.forClass(OfFloat.class).withComponent(float.class);

        public OfFloat(int length) {
            super(TYPE, length);
        }

        public float getFloat(int index) {
            return U.getFloat(target, unsafeOffset() + (checkIndex(index) << 2));
        }

        public float setFloat(int index, float value) {
            U.putFloat(target, unsafeOffset() + (checkIndex(index) << 2), value);
            return value;
        }

        @Override
        public PackedClass<OfFloat, Float> type() {
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
    }

    public static final class OfDouble extends OfPrimitive<Double> {
        public static final PackedClass<OfDouble, Double> TYPE
            = PackedClass.forClass(OfDouble.class).withComponent(double.class);

        public OfDouble(int length) {
            super(TYPE, length);
        }

        public double getDouble(int index) {
            return U.getDouble(target, unsafeOffset() + (checkIndex(index) << 3));
        }

        public double setDouble(int index, double value) {
            U.putDouble(target, unsafeOffset() + (checkIndex(index) << 3), value);
            return value;
        }

        @Override
        public PackedClass<OfDouble, Double> type() {
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
    }

    public static final class OfObject<CT extends PackedObject> extends PackedArray<CT> {
        public static final PackedClass<OfObject, ?> TYPE = PackedClass.forClass(OfObject.class);

        private final PackedClass<OfObject, CT> type;

        public OfObject(Class<CT> componentClass, int length) {
            this(TYPE.withComponent(componentClass), length);
        }

        private OfObject(PackedClass<OfObject, CT> type, int length) {
            super(type, length);
            this.type = type;
        }

        public CT getView(int index) {
            PackedClass<CT, ?> componentType = type.getComponentType();
            int elementOffset = offset + checkIndex(index) * componentType.getIndexScale();
            return Packed.newView(componentType, target, elementOffset, componentType.getSize());
        }

        public CT getCopy(int index) {
            PackedClass<CT, ?> componentType = type.getComponentType();
            int elementOffset = offset + checkIndex(index) * componentType.getIndexScale();
            return Packed.newCopy(componentType, target, elementOffset, componentType.getSize());
        }

        public CT copyFrom(int index, CT source) {
            PackedClass<CT, ?> componentType = type.getComponentType();
            int elementOffset = offset + checkIndex(index) * componentType.getIndexScale();
            System.arraycopy(source.target, source.offset, target, elementOffset, componentType.getSize());
            return source;
        }

        @Override
        public PackedClass<OfObject, CT> type() {
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
    }


    // Unsafe machinery

    private static final long LENGTH;

    static {
        try {
            LENGTH = U.objectFieldOffset(
                PackedArray.class.getDeclaredField("length"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}