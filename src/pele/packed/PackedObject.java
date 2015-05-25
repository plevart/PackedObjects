/*
 * Written by Peter.Levart@gmail.com and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */
package pele.packed;

import java.util.List;

/**
 * PackedObject implements the idea of
 * <a href="http://www.oracle.com/technetwork/java/jvmls2013sciam-2013525.pdf">Packed Objects</a>
 * developed by IBM in experimental J9 JVM, but doesn't need VM changes. Just Unsafe.
 */
public class PackedObject extends Packed {

    /**
     * Default constructor for "zero" packed objects.
     */
    public PackedObject() {
        super();
    }

    /**
     * @return the type of this packed object.
     */
    public final PackedClass<? extends PackedObject, ?> type() {
        return PackedClass.forClass(getClass());
    }

    /**
     * @return a view of this packed object as a packed object of the same
     * class or a superclass.
     * @throws ClassCastException if given {@code clazz} does not represent the
     *                            same class as this object's class or it's superclass
     *                            up to and including {@link PackedObject}.
     */
    public final <T extends PackedObject> T viewAs(Class<T> clazz) {
        if (!clazz.asSubclass(PackedObject.class).isAssignableFrom(getClass())) {
            throw new ClassCastException(
                "Can only view the packed object as a " + getClass() +
                    " or it's superclass");
        }
        PackedClass<T, ?> type = PackedClass.forClass(clazz);
        return Packed.newView(type, target, offset, type.getSize());
    }

    /**
     * @return a copy of this packed object as a packed object of the same
     * class or a superclass.
     * @throws ClassCastException if given {@code clazz} does not represent the
     *                            same class as this object's class or it's superclass
     *                            up to and including {@link PackedObject}.
     */
    public final <T extends PackedObject> T copyAs(Class<T> clazz) {
        if (!clazz.asSubclass(PackedObject.class).isAssignableFrom(getClass())) {
            throw new ClassCastException(
                "Can only copy the packed object as a " + getClass() +
                    " or it's superclass");
        }
        PackedClass<T, ?> type = PackedClass.forClass(clazz);
        return Packed.newView(type, target, offset, type.getSize());
    }

    /**
     * @return a string representation of the packed object in the format:
     * <p>
     * <pre>
     *   SimpleClassName{field1=valueAsString, field2=...}
     * </pre>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        appendToString(sb, type());
        sb.append("}");
        return sb.toString();
    }

    private void appendToString(StringBuilder sb, PackedClass<?, ?> pc) {
        if (pc == null) {
            return;
        }
        appendToString(sb, pc.getSuperclass());
        @SuppressWarnings("unchecked")
        List<PackedField<?, PackedObject>> fields = (List) pc.getFields();
        for (PackedField<?, PackedObject> field : fields) {
            if (sb.charAt(sb.length() - 1) != '{') {
                sb.append(", ");
            }
            sb.append(field.getName()).append("=").append(field.get(this));
        }
    }
}
