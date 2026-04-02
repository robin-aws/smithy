package software.amazon.smithy.model.regex.ast;

/**
 * Represents Unicode property escapes (\p{...} and \P{...}) in a regex pattern.
 *
 * Unicode property escapes match characters based on their Unicode properties
 * such as \p{L} for letters or \p{Nd} for decimal numbers.
 */
public class UnicodePropertyNode extends RegexNode {

    private final String propertyName;
    private final boolean negated;

    /**
     * Creates a new Unicode property node.
     * @param propertyName the Unicode property name (e.g., "L", "Nd", "Script=Latin")
     * @param negated true for \P{...} (negated), false for \p{...} (positive)
     */
    public UnicodePropertyNode(String propertyName, boolean negated) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Unicode property name cannot be null or empty");
        }
        this.propertyName = propertyName;
        this.negated = negated;
    }

    /**
     * Gets the Unicode property name.
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Checks if this is a negated Unicode property.
     * @return true for \P{...}, false for \p{...}
     */
    public boolean isNegated() {
        return negated;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitUnicodeProperty(this, () -> {});
    }

    @Override
    public String toString() {
        return new StringBuilder("UnicodeProperty{")
            .append(negated ? "\\P{" : "\\p{")
            .append(propertyName)
            .append("}}")
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
        UnicodePropertyNode that = (UnicodePropertyNode) obj;
        return negated == that.negated && propertyName.equals(that.propertyName);
    }

    @Override
    public int hashCode() {
        return propertyName.hashCode() * 31 + (negated ? 1 : 0);
    }
}
