package software.amazon.smithy.model.regex.ast;

/**
 * Represents a group in a regex pattern (parentheses).
 * Groups can be capturing or non-capturing and may contain any regex expression.
 */
public class GroupNode extends RegexNode {

    /**
     * Types of groups supported.
     */
    public enum Type {
        /** Capturing group (...). */
        CAPTURING,
        /** Non-capturing group (?:...). */
        NON_CAPTURING
    }

    private final RegexNode child;
    private final Type type;
    private final int groupNumber;

    /**
     * Creates a group node.
     * @param child the content of the group
     * @param type the type of group
     * @param groupNumber the capture group number (0 for non-capturing groups)
     */
    public GroupNode(RegexNode child, Type type, int groupNumber) {
        if (child == null) {
            throw new IllegalArgumentException("Group child cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Group type cannot be null");
        }
        if (groupNumber < 0) {
            throw new IllegalArgumentException("Group number cannot be negative");
        }

        this.child = child;
        this.type = type;
        this.groupNumber = groupNumber;
    }

    /**
     * Creates a capturing group.
     * @param child the content of the group
     * @param groupNumber the capture group number
     * @return a new capturing group node
     */
    public static GroupNode capturing(RegexNode child, int groupNumber) {
        return new GroupNode(child, Type.CAPTURING, groupNumber);
    }

    /**
     * Creates a non-capturing group.
     * @param child the content of the group
     * @return a new non-capturing group node
     */
    public static GroupNode nonCapturing(RegexNode child) {
        return new GroupNode(child, Type.NON_CAPTURING, 0);
    }

    /**
     * Returns the child node within this group.
     * @return the child node within this group
     */
    public RegexNode getChild() {
        return child;
    }

    /**
     * Returns the group type.
     * @return the group type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the capture group number.
     * @return the capture group number
     */
    public int getGroupNumber() {
        return groupNumber;
    }

    /**
     * Checks if this is a capturing group.
     * @return true if this group captures matches
     */
    public boolean isCapturing() {
        return type == Type.CAPTURING;
    }

    /**
     * Checks if this is a lookaround assertion.
     * @return false - lookaround assertions are represented by {@link LookaheadNode} and {@link LookbehindNode}
     */
    public boolean isLookaround() {
        return false;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitGroup(this, () -> child.accept(visitor));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case CAPTURING:
                sb.append("Group").append(groupNumber).append('{');
                break;
            case NON_CAPTURING:
                sb.append("NonCapturingGroup{");
                break;
            default:
                throw new IllegalStateException("Unknown group type: " + type);
        }
        sb.append(child).append('}');
        return sb.toString();
    }
}
