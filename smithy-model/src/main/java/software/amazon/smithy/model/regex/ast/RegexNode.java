package software.amazon.smithy.model.regex.ast;

/**
 * Abstract base class for all regex AST nodes.
 * Provides common functionality for traversing and analyzing regex patterns.
 */
public abstract class RegexNode {

    /**
     * Accepts a visitor for traversing the AST.
     * @param visitor the visitor to accept
     */
    public abstract void accept(RegexNodeVisitor visitor);

    /**
     * Gets a string representation of this node for debugging.
     * @return string representation
     */
    @Override
    public abstract String toString();
}
