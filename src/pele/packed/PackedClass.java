/*
 * Written by Peter.Levart@gmail.com and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */
package pele.packed;


import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * PackedClass holds additional meta information about the type of packed object,
 * primitive or packed array, and in case of arrays, can also hold meta information
 * about the array's component type.
 */
public final class PackedClass<T, CT> {

    /**
     * Returns a PackedClass object for given {@code clazz} which must
     * represent either:
     * <ul>
     * <li>a non-void primitive type, or</li>
     * <li>a {@link Packed} class or subclass (either abstract or concrete)</li>
     * </ul>
     * <p>
     * If given {@code clazz} represents an array type ({@link PackedArray} or
     * subclass) the returned PackedClass does not specify the full array
     * type yet as it's lacking a {@link #getComponentType() component type}. To
     * obtain PackedClass(es) representing instantiatable array type(s), use either:
     * <ul>
     * <li>{@link PackedArray.OfBoolean#TYPE}, {@link PackedArray.OfByte#TYPE}, ...
     * constants, or</li>
     * <li>{@link PackedArray.OfObject#typeWithComponent(Class)} static method.</li>
     * </ul>
     *
     * @param clazz the Class for which to obtain a PackedClass
     * @param <T>   the type represented by given {@code clazz}
     * @return a PackedClass object for given {@code clazz}
     * @throws IllegalArgumentException if given {@code clazz} does not represent
     *                                  a non-void primitive type or {@link Packed}
     *                                  class or subclass
     */
    @SuppressWarnings("unchecked")
    public static <T> PackedClass<T, ?> forClass(Class<T> clazz) {
        return (PackedClass) FOR_CLASS.get(clazz);
    }

    /**
     * Factory for 2nd level of PackedClasses (representing instantiatable packed
     * array types with a component type). The invariant is that only PackedClasses
     * for instantiatable types can be constructed or {@link IllegalArgumentException}
     * is thrown:
     * <ul>
     * <li>the {@code arrayType} must represent a non-abstract {@link PackedArray} subclass</li>
     * <li>the {@code componentClass} must represent either a non-void primitive class or
     * a {@link PackedObject} or it's non-abstract subclass</li>
     * <li>if the array type is a subclass of {@link PackedArray.OfPrimitive} then
     * the component type must be a primitive and vice versa:</li>
     * <li>if the array type is a subclass of {@link PackedArray.OfObject} then
     * the component type must be a concrete {@link PackedObject} or subclass</li>
     * </ul>
     * This is part of validation that is performed just once for a particular pair
     * of {@code (arrayType, componentClass)} and then cached. Contrary to the
     * constructor of 1st level of PackedClasses (primitive types, PackedObject(s)
     * and PackedArray(s) with yet unknown component type) where no such restriction
     * is placed.
     *
     * @return a PackedClass representing the same basic array type as this
     * PackedClass with a component type represented by given {@code componentClass}
     * @throws IllegalArgumentException if this ComponentClass does not represent an
     *                                  instantiatable basic array type or given
     *                                  {@code componentClass} goes not represent
     *                                  an instantiatable and compatible component
     *                                  type.
     */
    @SuppressWarnings("unchecked")
    <T2 extends T, CT2> PackedClass<T2, CT2> withComponent(Class<CT2> componentClass) {
        return (PackedClass) WITH_COMPONENT.get(componentClass);
    }

    private static final ClassValue<PackedClass<?, ?>> FOR_CLASS = new ClassValue<PackedClass<?, ?>>() {
        @Override
        protected PackedClass<?, ?> computeValue(Class<?> clazz) {
            return new PackedClass<>(clazz);
        }
    };

    private final ClassValue<PackedClass<T, ?>> WITH_COMPONENT = new ClassValue<PackedClass<T, ?>>() {
        @Override
        protected PackedClass<T, ?> computeValue(Class<?> componentClass) {
            return new PackedClass<>(PackedClass.this, componentClass);
        }
    };

    private final WeakReference<Class<T>> classRef;
    private final PackedClass<?, ?> componentType;
    private final List<PackedField<?, ?>> fields;
    private final int size, alignment, indexScale;

    /**
     * Constructor for 1st level of PackedClasses (representing primitive types,
     * packed object types or basic packed array types without a component type yet)
     */
    private PackedClass(Class<T> clazz) {
        if ((!clazz.isPrimitive() || void.class == clazz) && !Packed.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                "Invalid packed type: " +
                    clazz + " is neither non-void primitive nor " +
                    Packed.class.getName() + " or subclass");
        }
        classRef = new WeakReference<>(clazz);
        fields = computeAndBlessFields(clazz);
        componentType = null;
        size = computeSize(this);
        alignment = computeAlignment(this);
        indexScale = computeIndexScale(size, alignment);
    }

    /**
     * Constructor for 2nd level of PackedClasses (representing instantiatable
     * packed array types with a component type).
     */
    private PackedClass(PackedClass<T, ?> arrayType, Class<?> componentClass) {
        Class<T> clazz = arrayType.asClass();
        if (!PackedArray.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException(
                "Non-arrays can't have a component type: " +
                    clazz + " is not a non-abstract " +
                    PackedArray.class.getName() + " subclass");
        }
        if ((!componentClass.isPrimitive() || void.class == componentClass) &&
            (!PackedObject.class.isAssignableFrom(componentClass) || Modifier.isAbstract(componentClass.getModifiers()))) {
            throw new IllegalArgumentException(
                "Invalid component type: " +
                    componentClass + " is neither non-void primitive nor a non-abstract " +
                    PackedObject.class.getName() + " or subclass");
        }
        if (PackedArray.OfPrimitive.class.isAssignableFrom(clazz) ^
            componentClass.isPrimitive()) {
            throw new IllegalArgumentException(
                "Primitive arrays must have a primitive component type while " +
                    "object arrays must have an object component type - array type: " +
                    clazz.getName() + ", component type: " + componentClass.getName());
        }
        // inherit clazz, fields and size from parent
        classRef = arrayType.classRef;
        fields = arrayType.fields;
        size = arrayType.size;
        componentType = forClass(componentClass);
        alignment = computeAlignment(this);
        indexScale = computeIndexScale(size, alignment);
    }

    public boolean isPrimitive() {
        return asClass().isPrimitive();
    }

    public boolean isArray() {
        return PackedArray.class.isAssignableFrom(asClass());
    }

    public boolean isObject() {
        return PackedObject.class.isAssignableFrom(asClass());
    }

    public Class<T> asClass() {
        Class<T> clazz = classRef.get();
        if (clazz == null) {
            throw new IllegalStateException("The underlying class has been GCed.");
        }
        return clazz;
    }

    public PackedClass<? super T, ?> getSuperclass() {
        Class<? super T> clazz = asClass();
        if (!Packed.class.isAssignableFrom(clazz)) {
            // primitive
            return null;
        }
        clazz = clazz.getSuperclass();
        if (!Packed.class.isAssignableFrom(clazz)) {
            // beyond the top
            return null;
        }
        return forClass(clazz);
    }

    public List<PackedField<?, ?>> getFields() {
        return fields;
    }

    public PackedClass<?, ?> getComponentType() {
        return componentType;
    }

    public int arraySize(int length) {
        if (componentType == null) {
            throw new IllegalArgumentException("Component type not known for: " + this);
        }
        return (length == 0)
            ? 0
            : (length - 1) * componentType.getIndexScale() + componentType.getSize();
    }

    public int getSize() {
        if (size < 0)
            throw new IllegalArgumentException("Size not known for: " + this);
        return size;
    }

    public int getAlignment() {
        if (alignment < 0)
            throw new IllegalArgumentException("Alignment not known for: " + this);
        return alignment;
    }

    public int getIndexScale() {
        if (indexScale < 0)
            throw new IllegalArgumentException("Index scale not known for: " + this);
        return indexScale;
    }

    @Override
    public String toString() {
        Class<T> clazz = asClass();
        String name = clazz.getName();
        String componentName = (componentType == null)
            ? "?" : componentType.asClass().getName();
        return "PackedClass<" + name + ", " + componentName +
            ">{size=" + (size < 0 ? "unknown" : String.valueOf(size)) +
            ", alignment=" + (alignment < 0 ? "unknown" : String.valueOf(alignment)) +
            ", indexScale=" + (indexScale < 0 ? "unknown" : String.valueOf(indexScale)) +
            "}";
    }

    private static final Map<Class<?>, Integer> PRIMITIVE_SIZES = new HashMap<>();

    static {
        PRIMITIVE_SIZES.put(boolean.class, 1);
        PRIMITIVE_SIZES.put(byte.class, 1);
        PRIMITIVE_SIZES.put(char.class, 2);
        PRIMITIVE_SIZES.put(short.class, 2);
        PRIMITIVE_SIZES.put(int.class, 4);
        PRIMITIVE_SIZES.put(long.class, 8);
        PRIMITIVE_SIZES.put(float.class, 4);
        PRIMITIVE_SIZES.put(double.class, 8);
    }

    private static List<PackedField<?, ?>> computeAndBlessFields(Class<?> clazz) {
        if (PackedObject.class.isAssignableFrom(clazz)) {
            List<PackedField<?, ?>> fields = new ArrayList<>();
            for (Field f : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && PackedField.class.isAssignableFrom(f.getType())) {
                    if (!Modifier.isFinal(f.getModifiers())) {
                        throw new ClassFormatError("Field: " + f + " should be final.");
                    }
                    f.setAccessible(true);
                    try {
                        PackedField<?, ?> pf = (PackedField<?, ?>) f.get(null);
                        // while we are iterating all fields, bless them at the same time...
                        pf.bless(f.getName(), f.getModifiers());
                        fields.add(pf);
                    } catch (IllegalAccessException e) {
                        throw new Error(e);
                    }
                }
            }
            // sort them by offset
            fields.sort(Comparator.comparingInt(PackedField::getOffset));
            return Collections.unmodifiableList(fields);
        } else {
            return Collections.emptyList();
        }
    }

    private static int computeSize(PackedClass<?, ?> type) {
        if (type.isPrimitive()) {
            return PRIMITIVE_SIZES.get(type.asClass());
        } else if (type.isObject()) {
            List<PackedField<?, ?>> fields = type.fields;
            PackedField<?, ?> lastField = fields.isEmpty() ? null : fields.get(fields.size() - 1);
            return (lastField == null) ? 0 : lastField.offset + lastField.size;
        } else {
            assert type.isArray();
            // no size for arrays since they are of different lengths
            return -1;
        }
    }

    private static int computeAlignment(PackedClass<?, ?> type) {
        if (type.isPrimitive()) {
            // primitives have same alignment as size
            return computeSize(type);
        } else if (type.isObject()) {
            // packed object alignment is max(alignments of all fields)
            int alignment = 1;
            for (PackedClass<?, ?> c = type; c != null; c = c.getSuperclass()) {
                for (PackedField<?, ?> pf : c.fields) {
                    if (pf.alignment > alignment) {
                        alignment = pf.alignment;
                    }
                }
            }
            return alignment;
        } else {
            assert type.isArray();
            if (type.getComponentType() != null) {
                // take alignment from component type for arrays
                return type.getComponentType().getAlignment();
            } else {
                // no alignment for arrays if component type is unknown
                return -1;
            }
        }
    }

    private static int computeIndexScale(int componentSize, int alignment) {
        if (componentSize >= 0 && alignment > 0) {
            return PackedField.align(componentSize, alignment);
        } else {
            // no indexScale if size or alignment are unknown
            return -1;
        }
    }
}
