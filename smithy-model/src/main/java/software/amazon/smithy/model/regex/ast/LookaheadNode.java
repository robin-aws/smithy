package software.amazon.smithy.model.regex.ast;

/**
 * Represents lookahead assertions (?=...) and (?!...) in a regex pattern.
 *
 * Lookahead assertions are zero-width assertions that check if the pattern
 * ahead matches (positive) or doesn't match (negative) without consuming characters.
 */
public class LookaheadNode extends RegexNode {

    private final RegexNode content;
    private final boolean positive;

    /**
     * Creates a new lookahead node.
     * @param content the pattern to look ahead for
     * @param positive true for positive lookahead (?=...), false for negative (?!...)
     */
    public LookaheadNode(RegexNode content, boolean positive) {
        if (content == null) {
            throw new IllegalArgumentException("Lookahead content cannot be null");
        }
        this.content = content;
        this.positive = positive;
    }

    /**
     * Gets the content pattern of the lookahead.
     * @return the content node
     */
    public RegexNode getContent() {
        return content;
    }

    /**
     * Checks if this is a positive lookahead.
     * @return true for positive (?=...), false for negative (?!...)
     */
    public boolean isPositive() {
        return positive;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitLookahead(this, () -> content.accept(visitor));
    }

    @Override
    public String toString() {
        return new StringBuilder("Lookahead{")
            .append(positive ? "?=" : "?!")
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
        LookaheadNode that = (LookaheadNode) obj;
        return positive == that.positive && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode() * 31 + Boolean.hashCode(positive);
    }
}
