package software.amazon.smithy.model.regex.ast;

/**
 * Visitor interface for traversing regex AST nodes.
 *
 * <p>Each visit method receives a {@code Runnable} that, when called, continues
 * traversal into the node's children. Visitors control recursion by choosing
 * whether to invoke it. The default implementation calls {@code recurse.run()}
 * so that a bare {@code implements RegexNodeVisitor} performs a full traversal.
 */
public interface RegexNodeVisitor {

    default void visitLiteral(LiteralNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitDot(DotNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitBackreference(BackreferenceNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitEmpty(EmptyNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitCharacterClass(CharacterClassNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitAnchor(AnchorNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitQuantifier(QuantifierNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitGroup(GroupNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitAlternation(AlternationNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitSequence(SequenceNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitUnicodeProperty(UnicodePropertyNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitLookahead(LookaheadNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitLookbehind(LookbehindNode node, Runnable recurse) {
        recurse.run();
    }

    default void visitUnknownGroup(UnknownGroupNode node, Runnable recurse) {
        recurse.run();
    }
}
