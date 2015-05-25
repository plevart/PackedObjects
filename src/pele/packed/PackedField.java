/*
 * Written by Peter.Levart@gmail.com and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */
package pele.packed;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A PackedField allows to access an individual field and to lay out the
 * field in a packed object class. PackedField subclasses (each field type
 * has one subclass: {@link pfBoolean}, {@link pfByte}, {@link pfShort}, ...)
 * are instantiated and assigned to {@code static final} fields of a
 * {@link PackedObject} subclass. The code in the packed object class then
 * uses them to retrieve and store values.
 */
public abstract class PackedField<T, H extends PackedObject> {

    final PackedClass<T, ?> type;
    final Class<H> homeClass;
    final int offset, size, alignment;
    // the name injected by PackedClass constructor which blesses the field at the same time
    private String name;
    // the modifiers injected by PackedClass constructor
    private int modifiers;

    private PackedField(PackedClass<T, ?> type, Class<H> homeClass, int size, int alignment) {
        this.type = type;
        this.homeClass = homeClass;
        // PackedField(s) are assigned to static fields of PackedObject subclasses.
        // The order of class initialization and static field assignment is defined
        // by Java language. We exploit this fact to lay out the packed fields
        // one by one as they get instantiated and assigned to static fields...
        PackedField lastAssigned = findLastAssignedField(homeClass);
        if (lastAssigned != null) {
            offset = align(lastAssigned.offset + lastAssigned.size, alignment);
        } else {
            // 1st field
            offset = 0;
        }
        this.size = size;
        this.alignment = alignment;
    }

    /**
     * Constructor for primitive and packed object type fields
     */
    PackedField(PackedClass<T, ?> primitiveOrObjectType, Class<H> homeClass) {
        this(primitiveOrObjectType, homeClass,
            primitiveOrObjectType.getSize(), primitiveOrObjectType.getAlignment());
    }

    /**
     * Constructor for packed array type fields
     */
    PackedField(PackedClass<T, ?> arrayType, int length, Class<H> homeClass) {
        this(arrayType, homeClass, arrayType.arraySize(length), arrayType.getAlignment());
    }

    public String getName() {
        checkBlessed();
        return name;
    }

    public int getModifiers() {
        checkBlessed();
        return modifiers;
    }

    public PackedClass<T, ?> getType() {
        checkBlessed();
        return type;
    }

    public int getOffset() {
        checkBlessed();
        return offset;
    }

    public int getSize() {
        checkBlessed();
        return size;
    }

    public int getAlignment() {
        checkBlessed();
        return alignment;
    }

    public Class<H> getHomeClass() {
        checkBlessed();
        return homeClass;
    }

    public abstract T get(H object);

    public abstract void set(H object, T value);

    /**
     * PackedField is blessed with name and modifiers given in homeClass by PackedClass constructor
     */
    void bless(String name, int modifiers) {
        this.name = name;
        this.modifiers = modifiers;
    }

    /**
     * Every public method must call this first. Only blessed fields are safe to use when using Unsafe!
     */
    void checkBlessed() {
        if (name == null) {
            throw new IllegalStateException("PackedField is not blessed.");
        }
    }

    static int align(int offset, int alignment) {
        return (offset + alignment - 1) & ~(alignment - 1);
    }

    static PackedField<?, ?> findLastAssignedField(Class<? extends PackedObject> homeClass) {
        PackedField<?, ?> lastAssignedField = null;
        for (Class<?> c = homeClass; lastAssignedField == null && c != PackedObject.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && PackedField.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    try {
                        PackedField<?, ?> pf = (PackedField<?, ?>) f.get(null);
                        if (pf != null) {
                            if (lastAssignedField == null || pf.offset > lastAssignedField.offset) {
                                lastAssignedField = pf;
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new Error(e);
                    }
                }
            }
        }
        return lastAssignedField;
    }

    public static final class pfBoolean<H extends PackedObject> extends PackedField<Boolean, H> {
        public pfBoolean(Class<H> homeClass) {
            super(PackedClass.forClass(boolean.class), homeClass);
        }

        public boolean getBoolean(H object) {
            checkBlessed();
            return U.getBoolean(object.target, object.unsafeOffset() + this.offset);
        }

        public boolean setBoolean(H object, boolean value) {
            checkBlessed();
            U.putBoolean(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Boolean get(H object) {
            return getBoolean(object);
        }

        @Override
        public void set(H object, Boolean value) {
            setBoolean(object, value);
        }
    }

    public static final class pfByte<H extends PackedObject> extends PackedField<Byte, H> {
        public pfByte(Class<H> homeClass) {
            super(PackedClass.forClass(byte.class), homeClass);
        }

        public byte getByte(H object) {
            checkBlessed();
            return U.getByte(object.target, object.unsafeOffset() + this.offset);
        }

        public byte setByte(H object, byte value) {
            checkBlessed();
            U.putByte(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Byte get(H object) {
            return getByte(object);
        }

        @Override
        public void set(H object, Byte value) {
            setByte(object, value);
        }
    }

    public static final class pfChar<H extends PackedObject> extends PackedField<Character, H> {
        public pfChar(Class<H> homeClass) {
            super(PackedClass.forClass(char.class), homeClass);
        }

        public char getChar(H object) {
            checkBlessed();
            return U.getChar(object.target, object.unsafeOffset() + this.offset);
        }

        public char setChar(H object, char value) {
            checkBlessed();
            U.putChar(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Character get(H object) {
            return getChar(object);
        }

        @Override
        public void set(H object, Character value) {
            setChar(object, value);
        }
    }

    public static final class pfShort<H extends PackedObject> extends PackedField<Short, H> {
        public pfShort(Class<H> homeClass) {
            super(PackedClass.forClass(short.class), homeClass);
        }

        public short getShort(H object) {
            checkBlessed();
            return U.getShort(object.target, object.unsafeOffset() + this.offset);
        }

        public short setShort(H object, short value) {
            checkBlessed();
            U.putShort(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Short get(H object) {
            return getShort(object);
        }

        @Override
        public void set(H object, Short value) {
            setShort(object, value);
        }
    }

    public static final class pfInt<H extends PackedObject> extends PackedField<Integer, H> {
        public pfInt(Class<H> homeClass) {
            super(PackedClass.forClass(int.class), homeClass);
        }

        public int getInt(H object) {
            checkBlessed();
            return U.getInt(object.target, object.unsafeOffset() + this.offset);
        }

        public int setInt(H object, int value) {
            checkBlessed();
            U.putInt(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Integer get(H object) {
            return getInt(object);
        }

        @Override
        public void set(H object, Integer value) {
            setInt(object, value);
        }
    }

    public static final class pfLong<H extends PackedObject> extends PackedField<Long, H> {
        public pfLong(Class<H> homeClass) {
            super(PackedClass.forClass(long.class), homeClass);
        }

        public long getLong(H object) {
            checkBlessed();
            return U.getLong(object.target, object.unsafeOffset() + this.offset);
        }

        public long setLong(H object, long value) {
            checkBlessed();
            U.putLong(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Long get(H object) {
            return getLong(object);
        }

        @Override
        public void set(H object, Long value) {
            setLong(object, value);
        }
    }

    public static final class pfFloat<H extends PackedObject> extends PackedField<Float, H> {
        public pfFloat(Class<H> homeClass) {
            super(PackedClass.forClass(float.class), homeClass);
        }

        public float getFloat(H object) {
            checkBlessed();
            return U.getFloat(object.target, object.unsafeOffset() + this.offset);
        }

        public float setFloat(H object, float value) {
            checkBlessed();
            U.putFloat(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Float get(H object) {
            return getFloat(object);
        }

        @Override
        public void set(H object, Float value) {
            setFloat(object, value);
        }
    }

    public static final class pfDouble<H extends PackedObject> extends PackedField<Double, H> {
        public pfDouble(Class<H> homeClass) {
            super(PackedClass.forClass(double.class), homeClass);
        }

        public double getDouble(H object) {
            checkBlessed();
            return U.getDouble(object.target, object.unsafeOffset() + this.offset);
        }

        public double setDouble(H object, double value) {
            checkBlessed();
            U.putDouble(object.target, object.unsafeOffset() + this.offset, value);
            return value;
        }

        @Override
        public Double get(H object) {
            return getDouble(object);
        }

        @Override
        public void set(H object, Double value) {
            setDouble(object, value);
        }
    }

    public static final class pfObject<T extends PackedObject, H extends PackedObject> extends PackedField<T, H> {

        public pfObject(Class<T> type, Class<H> homeClass) {
            super(PackedClass.forClass(type), homeClass);
        }

        public T getView(H object) {
            checkBlessed();
            return Packed.newView(type, object.target, object.offset + this.offset, type.getSize());
        }

        public T getCopy(H object) {
            checkBlessed();
            return Packed.newCopy(type, object.target, object.offset + this.offset, type.getSize());
        }

        public T copyFrom(H object, T source) {
            checkBlessed();
            System.arraycopy(source.target, source.offset, object.target, object.offset + this.offset, type.getSize());
            return source;
        }

        @Override
        public T get(H object) {
            return getView(object);
        }

        @Override
        public void set(H object, T value) {
            copyFrom(object, value);
        }
    }

    public static final class pfArray<AT extends PackedArray<?>, H extends PackedObject> extends PackedField<AT, H> {
        final int length;

        public pfArray(PackedClass<AT, ?> arrayType, int length, Class<H> homeClass) {
            super(arrayType, PackedArray.checkLength(length), homeClass);
            this.length = length;
        }

        /**
         * @return the length of the packed array embedded as a field.
         */
        public final int length() {
            return length;
        }


        public AT getView(H object) {
            checkBlessed();
            return PackedArray.newArrayView(type, object.target, object.offset + this.offset, length);
        }

        public AT getCopy(H object) {
            checkBlessed();
            return PackedArray.newArrayCopy(type, object.target, object.offset + this.offset, length);
        }

        public AT copyFrom(H object, AT source) {
            checkBlessed();
            if (this.type != source.type()) {
                throw new ClassCastException(
                    "Can't copy from array of different type - target type: " +
                        type + ", source type: " + source.type());
            }
            if (this.length != source.length()) {
                throw new IllegalArgumentException(
                    "Can't copy from array of different length - target length: " +
                        this.length + ", source length: " + source.length());
            }
            System.arraycopy(source.target, source.offset, object.target, object.offset + this.offset, type.arraySize(this.length));
            return source;
        }

        @Override
        public AT get(H object) {
            return getView(object);
        }

        @Override
        public void set(H object, AT value) {
            copyFrom(object, value);
        }
    }

    // Unsafe
    private static final Unsafe U = Packed.U;
}
