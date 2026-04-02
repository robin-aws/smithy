package software.amazon.smithy.model.regex.ast;

/**
 * Represents the dot (.) metacharacter in a regex pattern.
 *
 * The dot matches any character except newline (in default mode).
 */
public final class DotNode extends RegexNode {

    /** Singleton instance since all dot nodes are equivalent. */
    public static final DotNode INSTANCE = new DotNode();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private DotNode() {}

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitDot(this, () -> {});
    }

    @Override
    public String toString() {
        return "Dot{.}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DotNode;
    }

    @Override
    public int hashCode() {
        return DotNode.class.hashCode();
    }
}
