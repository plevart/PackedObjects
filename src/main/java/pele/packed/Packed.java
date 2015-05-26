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
    final byte[] target;
    final int offset, size;

    final long unsafeOffset() {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET + offset;
    }

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

    /**
     * Factory for views of Packed instances.
     */
    static <P extends Packed> P newView(PackedClass<P> packedClass, byte[] target, int offset, int size) {
        try {
            @SuppressWarnings("unchecked")
            P instance = (P) U.allocateInstance(packedClass.asClass());
            U.putOrderedObject(instance, TARGET, target);
            U.putOrderedInt(instance, OFFSET, offset);
            U.putOrderedInt(instance, SIZE, size);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Factory for copies of Packed instances.
     */
    static <P extends Packed> P newCopy(PackedClass<P> packedClass, byte[] target, int offset, int size) {
        try {
            @SuppressWarnings("unchecked")
            P instance = (P) U.allocateInstance(packedClass.asClass());
            U.putOrderedObject(instance, TARGET,
                Arrays.copyOfRange(target, offset, offset + size));
            // offset is by default 0
            // U.putOrderedInt(instance, OFFSET, 0);
            U.putOrderedInt(instance, SIZE, size);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
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
