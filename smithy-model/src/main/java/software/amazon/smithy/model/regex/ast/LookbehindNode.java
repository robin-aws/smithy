package software.amazon.smithy.model.regex.ast;

/**
 * Represents lookbehind assertions {@code (?<=...)} and {@code (?<!...)} in a regex pattern.
 *
 * Lookbehind assertions are zero-width assertions that check if the pattern
 * behind matches (positive) or doesn't match (negative) without consuming characters.
 */
public class LookbehindNode extends RegexNode {

    private final RegexNode content;
    private final boolean positive;

    /**
     * Creates a new lookbehind node.
     * @param content the pattern to look behind for
     * @param positive true for positive lookbehind {@code (?<=...)}, false for negative {@code (?<!...)}
     */
    public LookbehindNode(RegexNode content, boolean positive) {
        if (content == null) {
            throw new IllegalArgumentException("Lookbehind content cannot be null");
        }
        this.content = content;
        this.positive = positive;
    }

    /**
     * Gets the content pattern of the lookbehind.
     * @return the content node
     */
    public RegexNode getContent() {
        return content;
    }

    /**
     * Checks if this is a positive lookbehind.
     * @return true for positive {@code (?<=...)}, false for negative {@code (?<!...)}
     */
    public boolean isPositive() {
        return positive;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitLookbehind(this, () -> content.accept(visitor));
    }

    @Override
    public String toString() {
        return new StringBuilder("Lookbehind{")
            .append(positive ? "?<=" : "?<!")
            .append(content)
            .append('}')
            .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LookbehindNode that = (LookbehindNode) obj;
        return positive == that.positive && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode() * 31 + Boolean.hashCode(positive);
    }
}
