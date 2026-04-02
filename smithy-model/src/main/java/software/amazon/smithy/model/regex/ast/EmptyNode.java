package software.amazon.smithy.model.regex.ast;

/**
 * Represents an empty regex pattern that matches the empty string.
 * This node has zero length and always matches successfully.
 */
public final class EmptyNode extends RegexNode {

    /** Singleton instance for empty patterns. */
    public static final EmptyNode INSTANCE = new EmptyNode();

    private EmptyNode() {
        // Private constructor for singleton
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitEmpty(this, () -> {});
    }

    @Override
    public String toString() {
        return "Empty{}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptyNode;
    }

    @Override
    public int hashCode() {
        return EmptyNode.class.hashCode();
    }
}
