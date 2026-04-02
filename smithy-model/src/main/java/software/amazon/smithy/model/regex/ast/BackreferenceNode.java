package software.amazon.smithy.model.regex.ast;

/**
 * Represents a backreference (\1 through \9) in a regex pattern.
 */
public class BackreferenceNode extends RegexNode {

    private final int groupNumber;

    public BackreferenceNode(int groupNumber) {
        if (groupNumber < 1 || groupNumber > 9) {
            throw new IllegalArgumentException("Group number must be 1-9");
        }
        this.groupNumber = groupNumber;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitBackreference(this, () -> {});
    }

    @Override
    public String toString() {
        return new StringBuilder("Backreference{\\")
            .append(groupNumber)
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
        return groupNumber == ((BackreferenceNode) obj).groupNumber;
    }

    @Override
    public int hashCode() {
        return groupNumber;
    }
}
