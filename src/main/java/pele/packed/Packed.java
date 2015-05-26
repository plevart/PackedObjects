/*
 * Written by Peter.Levart@gmail.com and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */
package pele.packed;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Abstract base for PackedObject(s) and PackedArray(s).
 */
abstract class Packed {
    private final byte[] target;
    private final int offset;
    private final int size;

    /**
     * Constructor for "zero" PackedObject(s).
     */
    Packed() {
        this.size = PackedClass.forClass((Class) getClass()).getSize();
        this.target = (size == 0) ? null : new byte[size];
        this.offset = 0;
    }

    /**
     * Constructor for "zero" PackedArray(s).
     */
    Packed(int size) {
        this.size = size;
        this.target = (size == 0) ? null : new byte[size];
        this.offset = 0;
    }

    /**
     * @return the type of this Packed instance.
     */
    public abstract PackedClass<? extends Packed> type();

    /**
     * Returns {@code true} if and only if given {@code object} is of the same
     * runtime {@link #getClass() class} as this object and the value of this packed
     * object or array is bit-by-bit-equal to the value of given {@code object}.
     *
     * @param object the object with which to compare
     * @return {@code true} if this and given object are equal-by-type-and-value.
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        Packed that = (Packed) object;
        if (this.size != that.size) return false;
        if (this.target == that.target &&
            this.offset == that.offset) return true;
        for (int i = 0; i < this.size; i++) {
            if (this.target[this.offset + i] != that.target[that.offset + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash code value for this packed object or array. The hash code
     * is calculated from all the bits of the packed object's or array's value.
     *
     * @return a hash code value for this packed object or array.
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        int h = getClass().getName().hashCode();
        for (int i = 0; i < size; i++) {
            h += (31 * h) + target[offset + i];
        }
        return h;
    }

    // low-level operations - unsafe and unchecked

    /**
     * Factory for views of Packed instances.
     */
    final <P extends Packed> P getViewU(PackedClass<P> type, long offset, long size) {
        return getViewU(type.asClass(), offset, size);
    }

    final <P extends Packed> P getViewU(Class<P> clazz, long offset, long size) {
        try {
            @SuppressWarnings("unchecked")
            P instance = (P) U.allocateInstance(clazz);
            U.putOrderedObject(instance, TARGET, this.target);
            U.putOrderedInt(instance, OFFSET, this.offset + (int) offset);
            U.putOrderedInt(instance, SIZE, (int) size);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Factory for copies of Packed instances.
     */
    final <P extends Packed> P getCopyU(PackedClass<P> type, long offset, long size) {
        return getCopyU(type.asClass(), offset, size);
    }

    final <P extends Packed> P getCopyU(Class<P> clazz, long offset, long size) {
        try {
            @SuppressWarnings("unchecked")
            P instance = (P) U.allocateInstance(clazz);
            U.putOrderedObject(instance, TARGET,
                Arrays.copyOfRange(target, this.offset + (int) offset, this.offset + (int) (offset + size)));
            // offset is by default 0
            // U.putOrderedInt(instance, OFFSET, 0);
            U.putOrderedInt(instance, SIZE, (int) size);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Factory for PackedArray views.
     */
    final <PA extends PackedArray<?>> PA getArrayViewU(PackedClass<PA> arrayType, long offset, int length) {
        return getArrayViewU(arrayType, offset, arrayType.arraySize(length), length);
    }

    final <PA extends PackedArray<?>> PA getArrayViewU(PackedClass<PA> arrayType, long offset, long size, int length) {
        PA array = getViewU(arrayType, offset, size);
        array.initLengthAndType(length, arrayType);
        return array;
    }

    /**
     * Factory for PackedArray copies.
     */
    final <PA extends PackedArray<?>> PA getArrayCopyU(PackedClass<PA> arrayType, long offset, int length) {
        return getArrayCopyU(arrayType, offset, arrayType.arraySize(length), length);
    }

    final <PA extends PackedArray<?>> PA getArrayCopyU(PackedClass<PA> arrayType, long offset, long size, int length) {
        PA array = getCopyU(arrayType, offset, size);
        array.initLengthAndType(length, arrayType);
        return array;
    }

    // low-level operations - unsafe and unchecked

    private long unsafeOffset(long offset) {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET + this.offset + offset;
    }

    private long unsafeOffset() {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET + this.offset;
    }

    final boolean getBooleanU(long offset) {
        return U.getBoolean(target, unsafeOffset(offset));
    }

    final void putBooleanU(long offset, boolean b) {
        U.putBoolean(target, unsafeOffset(offset), b);
    }

    final byte getByteU(long offset) {
        return U.getByte(target, unsafeOffset(offset));
    }

    final void putByteU(long offset, byte b) {
        U.putByte(target, unsafeOffset(offset), b);
    }

    final char getCharU(long offset) {
        return U.getChar(target, unsafeOffset(offset));
    }

    final void putCharU(long offset, char c) {
        U.putChar(target, unsafeOffset(offset), c);
    }

    final short getShortU(long offset) {
        return U.getShort(target, unsafeOffset(offset));
    }

    final void putShortU(long offset, short i) {
        U.putShort(target, unsafeOffset(offset), i);
    }

    final int getIntU(long offset) {
        return U.getInt(target, unsafeOffset(offset));
    }

    final void putIntU(long offset, int i) {
        U.putInt(target, unsafeOffset(offset), i);
    }

    final long getLongU(long offset) {
        return U.getLong(target, unsafeOffset(offset));
    }

    final void putLongU(long offset, long l) {
        U.putLong(target, unsafeOffset(offset), l);
    }

    final float getFloatU(long offset) {
        return U.getFloat(target, unsafeOffset(offset));
    }

    final void putFloatU(long offset, float v) {
        U.putFloat(target, unsafeOffset(offset), v);
    }

    final double getDoubleU(long offset) {
        return U.getDouble(target, unsafeOffset(offset));
    }

    final void putDoubleU(long offset, double v) {
        U.putDouble(target, unsafeOffset(offset), v);
    }

    // copyFrom support

    final void copyFromU(Packed source, long targetOffset, long targetSize) {
        U.copyMemory(source.target, source.unsafeOffset(),
            this.target, this.unsafeOffset(targetOffset), targetSize);
    }

    // Unsafe machinery

    static final Unsafe U;
    private static final long TARGET, OFFSET, SIZE;

    static {
        try {
            Field uf = Unsafe.class.getDeclaredField("theUnsafe");
            uf.setAccessible(true);
            U = (Unsafe) uf.get(null);
            TARGET = U.objectFieldOffset(
                Packed.class.getDeclaredField("target"));
            OFFSET = U.objectFieldOffset(
                Packed.class.getDeclaredField("offset"));
            SIZE = U.objectFieldOffset(
                Packed.class.getDeclaredField("size"));
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }
}
