package software.amazon.smithy.model.regex.ast;

/**
 * Represents a quantifier in a regex pattern (+, *, ?, {n,m}).
 * Applies repetition constraints to the child node.
 */
public class QuantifierNode extends RegexNode {

    private final RegexNode child;
    private final int min;
    private final int max;
    private final boolean greedy;

    /**
     * Creates a quantifier node with specific min/max bounds.
     * @param child the node to quantify
     * @param min minimum repetitions
     * @param max maximum repetitions (Integer.MAX_VALUE for unbounded)
     * @param greedy whether this is a greedy quantifier
     */
    public QuantifierNode(RegexNode child, int min, int max, boolean greedy) {
        if (child == null) {
            throw new IllegalArgumentException("Child node cannot be null");
        }
        if (min < 0) {
            throw new IllegalArgumentException("Minimum repetitions cannot be negative");
        }
        if (max < min && max != Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum repetitions cannot be less than minimum");
        }

        this.child = child;
        this.min = min;
        this.max = max;
        this.greedy = greedy;
    }

    /**
     * Creates a zero-or-more quantifier (*).
     * @param child the node to quantify
     * @param greedy whether this is a greedy quantifier
     * @return a new quantifier node
     */
    public static QuantifierNode createZeroOrMore(RegexNode child, boolean greedy) {
        return new QuantifierNode(child, 0, Integer.MAX_VALUE, greedy);
    }

    /**
     * Creates a one-or-more quantifier (+).
     * @param child the node to quantify
     * @param greedy whether this is a greedy quantifier
     * @return a new quantifier node
     */
    public static QuantifierNode createOneOrMore(RegexNode child, boolean greedy) {
        return new QuantifierNode(child, 1, Integer.MAX_VALUE, greedy);
    }

    /**
     * Creates a zero-or-one quantifier (?).
     * @param child the node to quantify
     * @param greedy whether this is a greedy quantifier
     * @return a new quantifier node
     */
    public static QuantifierNode createZeroOrOne(RegexNode child, boolean greedy) {
        return new QuantifierNode(child, 0, 1, greedy);
    }

    /**
     * Creates an exact quantifier {n}.
     * @param child the node to quantify
     * @param count exact number of repetitions
     * @param greedy whether this is a greedy quantifier
     * @return a new exact quantifier node
     */
    public static QuantifierNode exact(RegexNode child, int count, boolean greedy) {
        return new QuantifierNode(child, count, count, greedy);
    }

    /**
     * Creates a range quantifier {n,m}.
     * @param child the node to quantify
     * @param min minimum repetitions
     * @param max maximum repetitions
     * @param greedy whether this is a greedy quantifier
     * @return a new range quantifier node
     */
    public static QuantifierNode range(RegexNode child, int min, int max, boolean greedy) {
        return new QuantifierNode(child, min, max, greedy);
    }

    /**
     * Returns the child node being quantified.
     * @return the child node being quantified
     */
    public RegexNode getChild() {
        return child;
    }

    /**
     * Returns the minimum repetitions.
     * @return the minimum repetitions
     */
    public int getMin() {
        return min;
    }

    /**
     * Returns the maximum repetitions.
     * @return the maximum repetitions
     */
    public int getMax() {
        return max;
    }

    /**
     * Returns whether this is a greedy quantifier.
     * @return true if this is a greedy quantifier
     */
    public boolean isGreedy() {
        return greedy;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitQuantifier(this, () -> child.accept(visitor));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Quantifier{");
        sb.append(child);
        if (min == 0 && max == Integer.MAX_VALUE) {
            sb.append('*');
        } else if (min == 1 && max == Integer.MAX_VALUE) {
            sb.append('+');
        } else if (min == 0 && max == 1) {
            sb.append('?');
        } else if (min == max) {
            sb.append('{').append(min).append('}');
        } else {
            sb.append('{').append(min).append(',');
            if (max != Integer.MAX_VALUE) {
                sb.append(max);
            }
            sb.append('}');
        }
        if (!greedy) {
            sb.append('?');
        }
        sb.append('}');
        return sb.toString();
    }
}
