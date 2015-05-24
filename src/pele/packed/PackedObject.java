package pele.packed;

import java.util.List;

/**
 * PackedObject implements the idea of
 * <a href="http://www.oracle.com/technetwork/java/jvmls2013sciam-2013525.pdf">Packed Objects</a>
 * developed by IBM in experimental J9 JVM, but doesn't need VM changes. Just Unsafe.
 *
 * @author peter.levart@gmail.com
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
     * @return a string representation of the packed object.
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
