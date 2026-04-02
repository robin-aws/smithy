package software.amazon.smithy.model.regex.ast;

/**
 * Represents a literal character or string in a regex pattern.
 * Matches exactly the specified character(s).
 */
public class LiteralNode extends RegexNode {

    private final String literal;

    /**
     * Creates a new literal node.
     * @param literal the literal string to match
     */
    public LiteralNode(String literal) {
        if (literal == null || literal.isEmpty()) {
            throw new IllegalArgumentException("Literal cannot be null or empty");
        }
        this.literal = literal;
    }

    /**
     * Gets the literal string this node matches.
     * @return the literal string
     */
    public String getLiteral() {
        return literal;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitLiteral(this, () -> {});
    }

    @Override
    public String toString() {
        return new StringBuilder("Literal{").append(literal).append('}').toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LiteralNode that = (LiteralNode) obj;
        return literal.equals(that.literal);
    }

    @Override
    public int hashCode() {
        return literal.hashCode();
    }
}
