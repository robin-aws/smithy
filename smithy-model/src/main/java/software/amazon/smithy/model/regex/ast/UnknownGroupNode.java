package software.amazon.smithy.model.regex.ast;

/**
 * Represents an unrecognized special group construct in a regex pattern.
 *
 * <p>This node is used for group constructs starting with {@code (?} that the parser
 * does not explicitly handle (e.g. inline flags like {@code (?i:...)}).
 * Using a dedicated node type avoids losing precision by silently treating
 * unknown constructs as non-capturing groups.
 */
public class UnknownGroupNode extends RegexNode {

    private final RegexNode child;

    /**
     * Creates an unknown group node.
     * @param child the parsed content of the group
     */
    public UnknownGroupNode(RegexNode child) {
        if (child == null) {
            throw new IllegalArgumentException("Unknown group child cannot be null");
        }
        this.child = child;
    }

    /**
     * Returns the child node within this group.
     * @return the child node within this group
     */
    public RegexNode getChild() {
        return child;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitUnknownGroup(this, () -> child.accept(visitor));
    }

    @Override
    public String toString() {
        return new StringBuilder("UnknownGroup{").append(child).append('}').toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return child.equals(((UnknownGroupNode) obj).child);
    }

    @Override
    public int hashCode() {
        return child.hashCode() * 31 + 7;
    }
}
