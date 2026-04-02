package software.amazon.smithy.model.regex.ast;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for AST node classes to improve coverage.
 */
@DisplayName("Regex AST Node Tests")
public class RegexAstNodeTest {

    @Nested
    @DisplayName("BackreferenceNode Tests")
    class BackreferenceNodeTests {

        @Test
        @DisplayName("Should create backreference with valid group number")
        void testValidGroupNumber() {
            BackreferenceNode node = new BackreferenceNode(1);
            assertEquals(1, node.getGroupNumber());

            BackreferenceNode node9 = new BackreferenceNode(9);
            assertEquals(9, node9.getGroupNumber());
        }

        @Test
        @DisplayName("Should reject invalid group numbers")
        void testInvalidGroupNumber() {
            assertThrows(IllegalArgumentException.class, () -> new BackreferenceNode(0));
            assertThrows(IllegalArgumentException.class, () -> new BackreferenceNode(10));
        }

        @Test
        @DisplayName("Should implement equals correctly")
        void testEquals() {
            BackreferenceNode node1 = new BackreferenceNode(1);
            BackreferenceNode node2 = new BackreferenceNode(1);
            BackreferenceNode node3 = new BackreferenceNode(2);

            assertEquals(node1, node2);
            assertNotEquals(node1, node3);
            assertEquals(node1, node1);
            assertNotEquals(node1, null);
        }

        @Test
        @DisplayName("Should implement hashCode correctly")
        void testHashCode() {
            BackreferenceNode node1 = new BackreferenceNode(5);
            BackreferenceNode node2 = new BackreferenceNode(5);

            assertEquals(node1.hashCode(), node2.hashCode());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            BackreferenceNode node = new BackreferenceNode(1);
            boolean[] visited = {false};

            node.accept(new RegexNodeVisitor() {
                @Override
                public void visitBackreference(BackreferenceNode n, Runnable recurse) {
                    visited[0] = true;
                }
            });

            assertTrue(visited[0]);
        }
    }

    @Nested
    @DisplayName("LookaheadNode Tests")
    class LookaheadNodeTests {

        @Test
        @DisplayName("Should create positive lookahead")
        void testPositiveLookahead() {
            LiteralNode content = new LiteralNode("test");
            LookaheadNode node = new LookaheadNode(content, true);

            assertTrue(node.isPositive());
            assertEquals(content, node.getContent());
        }

        @Test
        @DisplayName("Should create negative lookahead")
        void testNegativeLookahead() {
            LiteralNode content = new LiteralNode("test");
            LookaheadNode node = new LookaheadNode(content, false);

            assertFalse(node.isPositive());
            assertEquals(content, node.getContent());
        }

        @Test
        @DisplayName("Should reject null content")
        void testNullContent() {
            assertThrows(IllegalArgumentException.class, () -> new LookaheadNode(null, true));
        }

        @Test
        @DisplayName("Should implement equals correctly")
        void testEquals() {
            LiteralNode content1 = new LiteralNode("test");
            LiteralNode content2 = new LiteralNode("test");
            LiteralNode content3 = new LiteralNode("other");

            LookaheadNode node1 = new LookaheadNode(content1, true);
            LookaheadNode node2 = new LookaheadNode(content2, true);
            LookaheadNode node3 = new LookaheadNode(content1, false);
            LookaheadNode node4 = new LookaheadNode(content3, true);

            assertEquals(node1, node2);
            assertNotEquals(node1, node3);
            assertNotEquals(node1, node4);
            assertEquals(node1, node1);
            assertNotEquals(node1, null);
            assertNotEquals(node1, "string");
        }

        @Test
        @DisplayName("Should implement hashCode correctly")
        void testHashCode() {
            LiteralNode content = new LiteralNode("test");
            LookaheadNode node1 = new LookaheadNode(content, true);
            LookaheadNode node2 = new LookaheadNode(content, true);

            assertEquals(node1.hashCode(), node2.hashCode());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            LiteralNode content = new LiteralNode("test");
            LookaheadNode node = new LookaheadNode(content, true);
            TestVisitor visitor = new TestVisitor();

            node.accept(visitor);
            assertTrue(visitor.isVisitedLookahead());
        }
    }

    @Nested
    @DisplayName("LookbehindNode Tests")
    class LookbehindNodeTests {

        @Test
        @DisplayName("Should create positive lookbehind")
        void testPositiveLookbehind() {
            LiteralNode content = new LiteralNode("test");
            LookbehindNode node = new LookbehindNode(content, true);

            assertTrue(node.isPositive());
            assertEquals(content, node.getContent());
        }

        @Test
        @DisplayName("Should create negative lookbehind")
        void testNegativeLookbehind() {
            LiteralNode content = new LiteralNode("test");
            LookbehindNode node = new LookbehindNode(content, false);

            assertFalse(node.isPositive());
        }

        @Test
        @DisplayName("Should reject null content")
        void testNullContent() {
            assertThrows(IllegalArgumentException.class, () -> new LookbehindNode(null, true));
        }

        @Test
        @DisplayName("Should implement equals correctly")
        void testEquals() {
            LiteralNode content1 = new LiteralNode("test");
            LiteralNode content2 = new LiteralNode("test");

            LookbehindNode node1 = new LookbehindNode(content1, true);
            LookbehindNode node2 = new LookbehindNode(content2, true);
            LookbehindNode node3 = new LookbehindNode(content1, false);

            assertEquals(node1, node2);
            assertNotEquals(node1, node3);
            assertEquals(node1, node1);
            assertNotEquals(node1, null);
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            LiteralNode content = new LiteralNode("test");
            LookbehindNode node = new LookbehindNode(content, true);
            TestVisitor visitor = new TestVisitor();

            node.accept(visitor);
            assertTrue(visitor.isVisitedLookbehind());
        }
    }

    @Nested
    @DisplayName("DotNode Tests")
    class DotNodeTests {

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            DotNode node = DotNode.INSTANCE;
            TestVisitor visitor = new TestVisitor();

            node.accept(visitor);
            assertTrue(visitor.isVisitedDot());
        }

        @Test
        @DisplayName("Should be singleton")
        void testSingleton() {
            DotNode node1 = DotNode.INSTANCE;
            DotNode node2 = DotNode.INSTANCE;

            assertSame(node1, node2);
        }
    }

    @Nested
    @DisplayName("EmptyNode Tests")
    class EmptyNodeTests {

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            EmptyNode node = EmptyNode.INSTANCE;
            TestVisitor visitor = new TestVisitor();

            node.accept(visitor);
            assertTrue(visitor.isVisitedEmpty());
        }

        @Test
        @DisplayName("Should be singleton")
        void testSingleton() {
            EmptyNode node1 = EmptyNode.INSTANCE;
            EmptyNode node2 = EmptyNode.INSTANCE;

            assertSame(node1, node2);
        }
    }

    @Nested
    @DisplayName("UnicodePropertyNode Tests")
    class UnicodePropertyNodeTests {

        @Test
        @DisplayName("Should create with property name")
        void testCreation() {
            UnicodePropertyNode node = new UnicodePropertyNode("Letter", false);
            assertEquals("Letter", node.getPropertyName());
            assertFalse(node.isNegated());
        }

        @Test
        @DisplayName("Should create negated property")
        void testNegated() {
            UnicodePropertyNode node = new UnicodePropertyNode("Digit", true);
            assertTrue(node.isNegated());
        }

        @Test
        @DisplayName("Should implement equals")
        void testEquals() {
            UnicodePropertyNode node1 = new UnicodePropertyNode("Letter", false);
            UnicodePropertyNode node2 = new UnicodePropertyNode("Letter", false);
            UnicodePropertyNode node3 = new UnicodePropertyNode("Digit", false);
            UnicodePropertyNode node4 = new UnicodePropertyNode("Letter", true);

            assertEquals(node1, node2);
            assertNotEquals(node1, node3);
            assertNotEquals(node1, node4);
            assertEquals(node1, node1);
            assertNotEquals(node1, null);
        }

        @Test
        @DisplayName("Should implement hashCode")
        void testHashCode() {
            UnicodePropertyNode node1 = new UnicodePropertyNode("Letter", false);
            UnicodePropertyNode node2 = new UnicodePropertyNode("Letter", false);

            assertEquals(node1.hashCode(), node2.hashCode());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            UnicodePropertyNode node = new UnicodePropertyNode("Letter", false);
            TestVisitor visitor = new TestVisitor();

            node.accept(visitor);
            assertTrue(visitor.isVisitedUnicodeProperty());
        }
    }

    @Nested
    @DisplayName("CharacterClassNode Tests")
    class CharacterClassNodeTests {

        @Test
        @DisplayName("Should test character in range")
        void testCharacterRange() {
            CharacterClassNode.CharacterRange range = new CharacterClassNode.CharacterRange('a', 'z');

            assertTrue(range.contains('a'));
            assertTrue(range.contains('m'));
            assertTrue(range.contains('z'));
            assertFalse(range.contains('A'));
            assertFalse(range.contains('0'));
            assertEquals('a', range.getStart());
            assertEquals('z', range.getEnd());
        }

        @Test
        @DisplayName("Should handle single character range")
        void testSingleCharRange() {
            CharacterClassNode.CharacterRange range = new CharacterClassNode.CharacterRange('x', 'x');

            assertTrue(range.contains('x'));
            assertFalse(range.contains('y'));
        }

        @Test
        @DisplayName("Should reject invalid range")
        void testInvalidRange() {
            assertThrows(IllegalArgumentException.class, () -> new CharacterClassNode.CharacterRange('z', 'a'));
        }

        @Test
        @DisplayName("Should test negated character class")
        void testNegatedClass() {
            Set<CharacterClassNode.CharacterRange> ranges = new HashSet<>();
            ranges.add(new CharacterClassNode.CharacterRange('0', '9'));

            CharacterClassNode node = new CharacterClassNode(Collections.emptySet(), ranges, true);

            assertTrue(node.isNegated());
            assertFalse(node.matches('5'));
            assertTrue(node.matches('a'));
        }

        @Test
        @DisplayName("Should test non-negated character class")
        void testNonNegatedClass() {
            Set<CharacterClassNode.CharacterRange> ranges = new HashSet<>();
            ranges.add(new CharacterClassNode.CharacterRange('0', '9'));

            CharacterClassNode node = new CharacterClassNode(Collections.emptySet(), ranges, false);

            assertFalse(node.isNegated());
            assertTrue(node.matches('5'));
            assertFalse(node.matches('a'));
        }

        @Test
        @DisplayName("Should match individual characters")
        void testIndividualCharacters() {
            Set<Character> chars = new HashSet<>();
            chars.add('a');
            chars.add('b');
            chars.add('c');

            CharacterClassNode node = new CharacterClassNode(chars, Collections.emptySet(), false);

            assertTrue(node.matches('a'));
            assertTrue(node.matches('b'));
            assertFalse(node.matches('d'));
        }

        @Test
        @DisplayName("Should get characters and ranges")
        void testGetters() {
            Set<Character> chars = new HashSet<>();
            chars.add('x');
            Set<CharacterClassNode.CharacterRange> ranges = new HashSet<>();
            ranges.add(new CharacterClassNode.CharacterRange('a', 'z'));

            CharacterClassNode node = new CharacterClassNode(chars, ranges, false);

            assertEquals(chars, node.getCharacters());
            assertEquals(ranges, node.getRanges());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            CharacterClassNode node = new CharacterClassNode(Collections.emptySet(), Collections.emptySet(), false);
            TestVisitor visitor = new TestVisitor();

            node.accept(visitor);
            assertTrue(visitor.isVisitedCharacterClass());
        }
    }

    @Nested
    @DisplayName("SequenceNode Tests")
    class SequenceNodeTests {

        @Test
        @DisplayName("Should get children")
        void testGetChildren() {
            LiteralNode lit1 = new LiteralNode("ab");
            LiteralNode lit2 = new LiteralNode("cd");
            List<RegexNode> children = java.util.Arrays.asList(lit1, lit2);
            SequenceNode seq = new SequenceNode(children);

            assertEquals(children, seq.getChildren());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            LiteralNode lit = new LiteralNode("test");
            SequenceNode seq = new SequenceNode(java.util.Arrays.asList(lit));
            TestVisitor visitor = new TestVisitor();

            seq.accept(visitor);
            assertTrue(visitor.isVisitedSequence());
        }
    }

    @Nested
    @DisplayName("AlternationNode Tests")
    class AlternationNodeTests {

        @Test
        @DisplayName("Should get alternatives")
        void testGetAlternatives() {
            LiteralNode lit1 = new LiteralNode("ab");
            LiteralNode lit2 = new LiteralNode("cd");
            List<RegexNode> alternatives = java.util.Arrays.asList(lit1, lit2);
            AlternationNode alt = new AlternationNode(alternatives);

            assertEquals(alternatives, alt.getAlternatives());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            LiteralNode lit = new LiteralNode("test");
            AlternationNode alt = new AlternationNode(java.util.Arrays.asList(lit));
            TestVisitor visitor = new TestVisitor();

            alt.accept(visitor);
            assertTrue(visitor.isVisitedAlternation());
        }
    }

    @Nested
    @DisplayName("QuantifierNode Tests")
    class QuantifierNodeTests {

        @Test
        @DisplayName("Should calculate length for fixed quantifier")
        void testFixedQuantifier() {
            LiteralNode lit = new LiteralNode("ab");
            QuantifierNode quant = new QuantifierNode(lit, 3, 3, true);

            assertTrue(quant.isGreedy());
            assertEquals(3, quant.getMin());
            assertEquals(3, quant.getMax());
        }

        @Test
        @DisplayName("Should calculate length for range quantifier")
        void testRangeQuantifier() {
            LiteralNode lit = new LiteralNode("ab");
            QuantifierNode quant = new QuantifierNode(lit, 2, 5, true);

            assertEquals(2, quant.getMin());
            assertEquals(5, quant.getMax());
        }

        @Test
        @DisplayName("Should handle unbounded quantifier")
        void testUnboundedQuantifier() {
            LiteralNode lit = new LiteralNode("ab");
            QuantifierNode quant = new QuantifierNode(lit, 1, Integer.MAX_VALUE, true);

            assertEquals(1, quant.getMin());
            assertEquals(Integer.MAX_VALUE, quant.getMax());
        }

        @Test
        @DisplayName("Should handle lazy quantifier")
        void testLazyQuantifier() {
            LiteralNode lit = new LiteralNode("ab");
            QuantifierNode quant = new QuantifierNode(lit, 0, Integer.MAX_VALUE, false);

            assertFalse(quant.isGreedy());
        }

        @Test
        @DisplayName("Should create quantifier using factory method")
        void testFactoryMethod() {
            LiteralNode lit = new LiteralNode("ab");

            QuantifierNode zeroOrMore = QuantifierNode.createZeroOrMore(lit, true);
            assertEquals(0, zeroOrMore.getMin());
            assertEquals(Integer.MAX_VALUE, zeroOrMore.getMax());

            QuantifierNode oneOrMore = QuantifierNode.createOneOrMore(lit, true);
            assertEquals(1, oneOrMore.getMin());

            QuantifierNode zeroOrOne = QuantifierNode.createZeroOrOne(lit, true);
            assertEquals(0, zeroOrOne.getMin());
            assertEquals(1, zeroOrOne.getMax());
        }

        @Test
        @DisplayName("Should reject null child")
        void testNullChild() {
            assertThrows(IllegalArgumentException.class, () -> new QuantifierNode(null, 1, 1, true));
        }

        @Test
        @DisplayName("Should reject negative min")
        void testNegativeMin() {
            LiteralNode lit = new LiteralNode("ab");
            assertThrows(IllegalArgumentException.class, () -> new QuantifierNode(lit, -1, 1, true));
        }

        @Test
        @DisplayName("Should reject max less than min")
        void testMaxLessThanMin() {
            LiteralNode lit = new LiteralNode("ab");
            assertThrows(IllegalArgumentException.class, () -> new QuantifierNode(lit, 5, 2, true));
        }

        @Test
        @DisplayName("Should get child node")
        void testGetChild() {
            LiteralNode lit = new LiteralNode("test");
            QuantifierNode quant = new QuantifierNode(lit, 2, 2, true);

            assertEquals(lit, quant.getChild());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            LiteralNode lit = new LiteralNode("ab");
            QuantifierNode quant = new QuantifierNode(lit, 1, 1, true);
            TestVisitor visitor = new TestVisitor();

            quant.accept(visitor);
            assertTrue(visitor.isVisitedQuantifier());
        }
    }

    @Nested
    @DisplayName("AnchorNode Tests")
    class AnchorNodeTests {

        @Test
        @DisplayName("Should create start anchor")
        void testStartAnchor() {
            AnchorNode anchor = new AnchorNode(AnchorNode.Type.START_OF_STRING);
            assertEquals(AnchorNode.Type.START_OF_STRING, anchor.getType());
        }

        @Test
        @DisplayName("Should create end anchor")
        void testEndAnchor() {
            AnchorNode anchor = new AnchorNode(AnchorNode.Type.END_OF_STRING);
            assertEquals(AnchorNode.Type.END_OF_STRING, anchor.getType());
        }

        @Test
        @DisplayName("Should create word boundary anchor")
        void testWordBoundary() {
            AnchorNode anchor = new AnchorNode(AnchorNode.Type.WORD_BOUNDARY);
            assertEquals(AnchorNode.Type.WORD_BOUNDARY, anchor.getType());
        }

        @Test
        @DisplayName("Should create non-word boundary anchor")
        void testNonWordBoundary() {
            AnchorNode anchor = new AnchorNode(AnchorNode.Type.NON_WORD_BOUNDARY);
            assertEquals(AnchorNode.Type.NON_WORD_BOUNDARY, anchor.getType());
        }

        @Test
        @DisplayName("Should use factory methods")
        void testFactoryMethods() {
            AnchorNode start = AnchorNode.startOfString();
            assertEquals(AnchorNode.Type.START_OF_STRING, start.getType());

            AnchorNode end = AnchorNode.endOfString();
            assertEquals(AnchorNode.Type.END_OF_STRING, end.getType());
        }

        @Test
        @DisplayName("Should reject null type")
        void testNullType() {
            assertThrows(IllegalArgumentException.class, () -> new AnchorNode(null));
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            AnchorNode anchor = new AnchorNode(AnchorNode.Type.START_OF_STRING);
            TestVisitor visitor = new TestVisitor();

            anchor.accept(visitor);
            assertTrue(visitor.isVisitedAnchor());
        }
    }

    @Nested
    @DisplayName("GroupNode Tests")
    class GroupNodeTests {

        @Test
        @DisplayName("Should create capturing group")
        void testCapturingGroup() {
            LiteralNode lit = new LiteralNode("test");
            GroupNode group = new GroupNode(lit, GroupNode.Type.CAPTURING, 1);

            assertEquals(GroupNode.Type.CAPTURING, group.getType());
            assertEquals(lit, group.getChild());
            assertEquals(1, group.getGroupNumber());
        }

        @Test
        @DisplayName("Should create non-capturing group")
        void testNonCapturingGroup() {
            LiteralNode lit = new LiteralNode("test");
            GroupNode group = new GroupNode(lit, GroupNode.Type.NON_CAPTURING, 0);

            assertEquals(GroupNode.Type.NON_CAPTURING, group.getType());
            assertEquals(0, group.getGroupNumber());
        }

        @Test
        @DisplayName("Should use factory methods")
        void testFactoryMethods() {
            LiteralNode lit = new LiteralNode("test");

            GroupNode capturing = GroupNode.capturing(lit, 1);
            assertEquals(GroupNode.Type.CAPTURING, capturing.getType());
            assertEquals(1, capturing.getGroupNumber());

            GroupNode nonCapturing = GroupNode.nonCapturing(lit);
            assertEquals(GroupNode.Type.NON_CAPTURING, nonCapturing.getType());
            assertEquals(0, nonCapturing.getGroupNumber());
        }

        @Test
        @DisplayName("Should reject null child")
        void testNullChild() {
            assertThrows(IllegalArgumentException.class, () -> new GroupNode(null, GroupNode.Type.CAPTURING, 1));
        }

        @Test
        @DisplayName("Should reject negative group number")
        void testNegativeGroupNumber() {
            LiteralNode lit = new LiteralNode("test");
            assertThrows(IllegalArgumentException.class, () -> new GroupNode(lit, GroupNode.Type.CAPTURING, -1));
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            LiteralNode lit = new LiteralNode("test");
            GroupNode group = new GroupNode(lit, GroupNode.Type.CAPTURING, 1);
            TestVisitor visitor = new TestVisitor();

            group.accept(visitor);
            assertTrue(visitor.isVisitedGroup());
        }
    }

    @Nested
    @DisplayName("LiteralNode Tests")
    class LiteralNodeTests {

        @Test
        @DisplayName("Should get literal value")
        void testGetLiteral() {
            LiteralNode lit = new LiteralNode("hello");
            assertEquals("hello", lit.getLiteral());
        }

        @Test
        @DisplayName("Should accept visitor")
        void testVisitor() {
            LiteralNode lit = new LiteralNode("test");
            TestVisitor visitor = new TestVisitor();

            lit.accept(visitor);
            assertTrue(visitor.isVisitedLiteral());
        }
    }

    /**
     * Test visitor to verify accept methods are called.
     */
    private static final class TestVisitor implements RegexNodeVisitor {
        private boolean visitedLookahead = false;
        private boolean visitedLookbehind = false;
        private boolean visitedDot = false;
        private boolean visitedEmpty = false;
        private boolean visitedUnicodeProperty = false;
        private boolean visitedCharacterClass = false;
        private boolean visitedSequence = false;
        private boolean visitedAlternation = false;
        private boolean visitedQuantifier = false;
        private boolean visitedAnchor = false;
        private boolean visitedGroup = false;
        private boolean visitedLiteral = false;

        boolean isVisitedLookahead() {
            return visitedLookahead;
        }

        boolean isVisitedLookbehind() {
            return visitedLookbehind;
        }

        boolean isVisitedDot() {
            return visitedDot;
        }

        boolean isVisitedEmpty() {
            return visitedEmpty;
        }

        boolean isVisitedUnicodeProperty() {
            return visitedUnicodeProperty;
        }

        boolean isVisitedCharacterClass() {
            return visitedCharacterClass;
        }

        boolean isVisitedSequence() {
            return visitedSequence;
        }

        boolean isVisitedAlternation() {
            return visitedAlternation;
        }

        boolean isVisitedQuantifier() {
            return visitedQuantifier;
        }

        boolean isVisitedAnchor() {
            return visitedAnchor;
        }

        boolean isVisitedGroup() {
            return visitedGroup;
        }

        boolean isVisitedLiteral() {
            return visitedLiteral;
        }

        @Override
        public void visitLiteral(LiteralNode node, Runnable recurse) {
            visitedLiteral = true;
        }

        @Override
        public void visitCharacterClass(CharacterClassNode node, Runnable recurse) {
            visitedCharacterClass = true;
        }

        @Override
        public void visitDot(DotNode node, Runnable recurse) {
            visitedDot = true;
        }

        @Override
        public void visitQuantifier(QuantifierNode node, Runnable recurse) {
            visitedQuantifier = true;
        }

        @Override
        public void visitSequence(SequenceNode node, Runnable recurse) {
            visitedSequence = true;
        }

        @Override
        public void visitAlternation(AlternationNode node, Runnable recurse) {
            visitedAlternation = true;
        }

        @Override
        public void visitAnchor(AnchorNode node, Runnable recurse) {
            visitedAnchor = true;
        }

        @Override
        public void visitGroup(GroupNode node, Runnable recurse) {
            visitedGroup = true;
        }

        @Override
        public void visitLookahead(LookaheadNode node, Runnable recurse) {
            visitedLookahead = true;
        }

        @Override
        public void visitLookbehind(LookbehindNode node, Runnable recurse) {
            visitedLookbehind = true;
        }

        @Override
        public void visitEmpty(EmptyNode node, Runnable recurse) {
            visitedEmpty = true;
        }

        @Override
        public void visitUnicodeProperty(UnicodePropertyNode node, Runnable recurse) {
            visitedUnicodeProperty = true;
        }
    }
}
