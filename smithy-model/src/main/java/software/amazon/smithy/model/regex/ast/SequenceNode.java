package software.amazon.smithy.model.regex.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a sequence (concatenation) of regex nodes.
 * This node matches all of its children in order.
 */
public class SequenceNode extends RegexNode {

    private final List<RegexNode> children;

    /**
     * Creates a sequence node with the given children.
     * @param children the child nodes in sequence
     */
    public SequenceNode(List<RegexNode> children) {
        if (children == null || children.isEmpty()) {
            throw new IllegalArgumentException("Sequence must have at least one child");
        }
        if (children.contains(null)) {
            throw new IllegalArgumentException("Children cannot contain null nodes");
        }

        this.children = new ArrayList<>(children);
    }

    /**
     * Creates a sequence node with multiple children.
     * @param children the child nodes in sequence
     * @return a new sequence node
     */
    public static SequenceNode of(RegexNode... children) {
        return new SequenceNode(Arrays.asList(children));
    }

    /**
     * Creates a sequence node with two children.
     * @param first the first child
     * @param second the second child
     * @return a new sequence node
     */
    public static SequenceNode of(RegexNode first, RegexNode second) {
        List<RegexNode> children = new ArrayList<>();
        children.add(first);
        children.add(second);
        return new SequenceNode(children);
    }

    /**
     * Gets the child nodes in this sequence.
     * @return unmodifiable list of child nodes
     */
    public List<RegexNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitSequence(this, () -> {
            for (RegexNode child : children) {
                child.accept(visitor);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sequence{");

        for (int i = 0; i < children.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(children.get(i));
        }

        sb.append("}");
        return sb.toString();
    }
}
