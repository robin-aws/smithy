package software.amazon.smithy.model.regex.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an alternation (|) in a regex pattern.
 * Matches any one of the alternative branches.
 */
public class AlternationNode extends RegexNode {

    private final List<RegexNode> alternatives;

    /**
     * Creates an alternation node with the given alternatives.
     * @param alternatives the alternative branches
     */
    public AlternationNode(List<RegexNode> alternatives) {
        if (alternatives == null || alternatives.isEmpty()) {
            throw new IllegalArgumentException("Alternation must have at least one alternative");
        }
        if (alternatives.contains(null)) {
            throw new IllegalArgumentException("Alternatives cannot contain null nodes");
        }

        this.alternatives = new ArrayList<>(alternatives);
    }

    /**
     * Creates an alternation node with two alternatives.
     * @param left the first alternative
     * @param right the second alternative
     * @return a new alternation node
     */
    public static AlternationNode of(RegexNode left, RegexNode right) {
        List<RegexNode> alternatives = new ArrayList<>();
        alternatives.add(left);
        alternatives.add(right);
        return new AlternationNode(alternatives);
    }

    /**
     * Creates an alternation node with multiple alternatives.
     * @param alternatives the alternative branches
     * @return a new alternation node
     */
    public static AlternationNode of(RegexNode... alternatives) {
        List<RegexNode> altList = new ArrayList<>();
        for (RegexNode alt : alternatives) {
            altList.add(alt);
        }
        return new AlternationNode(altList);
    }

    /**
     * Returns the list of alternative branches.
     * @return the list of alternative branches
     */
    public List<RegexNode> getAlternatives() {
        return Collections.unmodifiableList(alternatives);
    }

    /**
     * Adds an alternative to this alternation.
     * @param alternative the alternative to add
     */
    public void addAlternative(RegexNode alternative) {
        if (alternative == null) {
            throw new IllegalArgumentException("Alternative cannot be null");
        }
        alternatives.add(alternative);
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitAlternation(this, () -> {
            for (RegexNode child : alternatives) {
                child.accept(visitor);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Alternation{");

        for (int i = 0; i < alternatives.size(); i++) {
            if (i > 0) {
                sb.append("|");
            }
            sb.append(alternatives.get(i));
        }

        sb.append("}");
        return sb.toString();
    }
}
