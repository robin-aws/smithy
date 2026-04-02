package software.amazon.smithy.model.regex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.regex.ast.RegexNode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RegexParser Edge Cases")
class RegexParserEdgeCasesTest {

    private RegexParser parser;

    @BeforeEach
    void setUp() {
        parser = new RegexParser();
    }

    @Test
    @DisplayName("Should parse empty pattern")
    void testEmptyPattern() throws InvalidRegexException {
        RegexNode node = parser.parse("");
        assertNotNull(node);
    }

    @Test
    @DisplayName("Should parse single character")
    void testSingleCharacter() throws InvalidRegexException {
        RegexNode node = parser.parse("a");
        // Single character might be wrapped in a SequenceNode
        assertNotNull(node);
    }

    @Test
    @DisplayName("Should parse escaped characters")
    void testEscapedCharacters() {
        assertDoesNotThrow(() -> parser.parse("\\n"));
        assertDoesNotThrow(() -> parser.parse("\\t"));
        assertDoesNotThrow(() -> parser.parse("\\r"));
        assertDoesNotThrow(() -> parser.parse("\\."));
        assertDoesNotThrow(() -> parser.parse("\\*"));
    }

    @Test
    @DisplayName("Should parse character classes")
    void testCharacterClasses() throws InvalidRegexException {
        assertDoesNotThrow(() -> parser.parse("[abc]"));
        assertDoesNotThrow(() -> parser.parse("[a-z]"));
        assertDoesNotThrow(() -> parser.parse("[^abc]"));
        assertDoesNotThrow(() -> parser.parse("[a-zA-Z0-9]"));
    }

    @Test
    @DisplayName("Should parse quantifiers")
    void testQuantifiers() throws InvalidRegexException {
        assertDoesNotThrow(() -> parser.parse("a*"));
        assertDoesNotThrow(() -> parser.parse("a+"));
        assertDoesNotThrow(() -> parser.parse("a?"));
        assertDoesNotThrow(() -> parser.parse("a{2}"));
        assertDoesNotThrow(() -> parser.parse("a{2,}"));
        assertDoesNotThrow(() -> parser.parse("a{2,5}"));
    }

    @Test
    @DisplayName("Should parse groups")
    void testGroups() throws InvalidRegexException {
        assertDoesNotThrow(() -> parser.parse("(abc)"));
        assertDoesNotThrow(() -> parser.parse("(?:abc)"));
        assertDoesNotThrow(() -> parser.parse("(a|b)"));
    }

    @Test
    @DisplayName("Should parse anchors")
    void testAnchors() throws InvalidRegexException {
        assertDoesNotThrow(() -> parser.parse("^abc"));
        assertDoesNotThrow(() -> parser.parse("abc$"));
        assertDoesNotThrow(() -> parser.parse("^abc$"));
        assertDoesNotThrow(() -> parser.parse("\\b"));
        assertDoesNotThrow(() -> parser.parse("\\B"));
    }

    @Test
    @DisplayName("Should parse lookaheads and lookbehinds")
    void testLookarounds() throws InvalidRegexException {
        assertDoesNotThrow(() -> parser.parse("(?=abc)"));
        assertDoesNotThrow(() -> parser.parse("(?!abc)"));
        assertDoesNotThrow(() -> parser.parse("(?<=abc)"));
        assertDoesNotThrow(() -> parser.parse("(?<!abc)"));
    }

    @Test
    @DisplayName("Should parse alternation")
    void testAlternation() throws InvalidRegexException {
        RegexNode node = parser.parse("a|b");
        // Alternation might be wrapped in a GroupNode
        assertNotNull(node);
    }

    @Test
    @DisplayName("Should parse complex patterns")
    void testComplexPatterns() throws InvalidRegexException {
        assertDoesNotThrow(() -> parser.parse("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"));
        assertDoesNotThrow(() -> parser.parse("\\d{3}-\\d{2}-\\d{4}"));
        assertDoesNotThrow(() -> parser.parse("(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})"));
    }

    @Test
    @DisplayName("Should throw on unclosed bracket")
    void testUnclosedBracket() {
        assertThrows(InvalidRegexException.class, () -> parser.parse("[abc"));
    }

    @Test
    @DisplayName("Should throw on unclosed parenthesis")
    void testUnclosedParenthesis() {
        assertThrows(InvalidRegexException.class, () -> parser.parse("(abc"));
    }

    @Test
    @DisplayName("Should throw on invalid quantifier")
    void testInvalidQuantifier() {
        assertThrows(InvalidRegexException.class, () -> parser.parse("a{"));
        assertThrows(InvalidRegexException.class, () -> parser.parse("a{,}"));
    }

    @Test
    @DisplayName("Should throw on invalid escape")
    void testInvalidEscape() {
        assertThrows(InvalidRegexException.class, () -> parser.parse("\\"));
    }
}
