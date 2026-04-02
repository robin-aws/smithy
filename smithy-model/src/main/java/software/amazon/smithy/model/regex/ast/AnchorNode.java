package software.amazon.smithy.model.regex.ast;

/**
 * Represents an anchor in a regex pattern (^, $, \b, \B).
 * Anchors assert position rather than matching characters.
 */
public class AnchorNode extends RegexNode {

    /**
     * Types of anchors supported.
     */
    public enum Type {
        /** Start of string anchor (^). */
        START_OF_STRING,
        /** End of string anchor ($). */
        END_OF_STRING,
        /** Word boundary (\b). Matches at word/non-word boundaries. */
        WORD_BOUNDARY,
        /** Non-word boundary (\B). Matches where \b does not match. */
        NON_WORD_BOUNDARY
    }

    private final Type type;

    /**
     * Creates an anchor node.
     * @param type the type of anchor
     */
    public AnchorNode(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Anchor type cannot be null");
        }
        this.type = type;
    }

    /**
     * Creates a start-of-string anchor (^).
     * @return a new start-of-string anchor node
     */
    public static AnchorNode startOfString() {
        return new AnchorNode(Type.START_OF_STRING);
    }

    /**
     * Creates an end-of-string anchor ($).
     * @return a new end-of-string anchor node
     */
    public static AnchorNode endOfString() {
        return new AnchorNode(Type.END_OF_STRING);
    }

    /**
     * Creates a word boundary anchor (\b).
     * @return a new word boundary anchor node
     */
    public static AnchorNode wordBoundary() {
        return new AnchorNode(Type.WORD_BOUNDARY);
    }

    /**
     * Creates a non-word boundary anchor (\B).
     * @return a new non-word boundary anchor node
     */
    public static AnchorNode nonWordBoundary() {
        return new AnchorNode(Type.NON_WORD_BOUNDARY);
    }

    /**
     * Returns the anchor type.
     * @return the anchor type
     */
    public Type getType() {
        return type;
    }

    /**
     * Checks if this is a start anchor (^).
     * @return true if this anchor marks the start of string
     */
    public boolean isStartAnchor() {
        return type == Type.START_OF_STRING;
    }

    /**
     * Checks if this is an end anchor ($).
     * @return true if this anchor marks the end of string
     */
    public boolean isEndAnchor() {
        return type == Type.END_OF_STRING;
    }

    /**
     * Checks if this is a word boundary anchor (\b).
     * @return true if this anchor marks a word boundary
     */
    public boolean isWordBoundary() {
        return type == Type.WORD_BOUNDARY;
    }

    /**
     * Checks if this is a non-word boundary anchor (\B).
     * @return true if this anchor marks a non-word boundary
     */
    public boolean isNonWordBoundary() {
        return type == Type.NON_WORD_BOUNDARY;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitAnchor(this, () -> {});
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Anchor{");
        switch (type) {
            case START_OF_STRING:
                sb.append('^');
                break;
            case END_OF_STRING:
                sb.append('$');
                break;
            case WORD_BOUNDARY:
                sb.append("\\b");
                break;
            case NON_WORD_BOUNDARY:
                sb.append("\\B");
                break;
            default:
                sb.append('?');
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AnchorNode that = (AnchorNode) obj;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
