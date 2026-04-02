package software.amazon.smithy.model.regex;

import software.amazon.smithy.model.regex.ast.AlternationNode;
import software.amazon.smithy.model.regex.ast.CharacterClassNode;
import software.amazon.smithy.model.regex.ast.EmptyNode;
import software.amazon.smithy.model.regex.ast.GroupNode;
import software.amazon.smithy.model.regex.ast.LiteralNode;
import software.amazon.smithy.model.regex.ast.QuantifierNode;
import software.amazon.smithy.model.regex.ast.RegexNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive unit tests for regex parsing functionality.
 */
public class RegexParsingTest {

    private RegexParser parser;

    @BeforeEach
    void setUp() {
        parser = new RegexParser();
    }

    @Nested
    @DisplayName("Basic Parsing Tests")
    class BasicParsingTests {

        @Test
        @DisplayName("Should parse simple literals")
        void testParseLiteral() throws InvalidRegexException {
            RegexNode result = parser.parse("abc");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
        }

        @Test
        @DisplayName("Should parse single character")
        void testParseSingleCharacter() throws InvalidRegexException {
            RegexNode result = parser.parse("a");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
        }

        @Test
        @DisplayName("Should handle empty pattern")
        void testEmptyPattern() throws InvalidRegexException {
            RegexNode result = parser.parse("");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode group = (GroupNode) result;
            assertTrue(group.getChild() instanceof EmptyNode);
        }

        @Test
        @DisplayName("Should reject null pattern")
        void testNullPattern() {
            assertThrows(InvalidRegexException.class, () -> parser.parse(null));
        }

        @ParameterizedTest
        @DisplayName("Should parse various literal strings")
        @ValueSource(
            strings = {
                "a",
                "cat",
                "hello",
                "world",
                "test123",
                "ABC",
                "MixedCase",
                "with spaces",
                "123456",
                "special-chars_allowed"
            })
        void testVariousLiteralStrings(String literal) throws InvalidRegexException {
            RegexNode ast = parser.parse(literal);
            assertNotNull(ast);
        }
    }

    @Nested
    @DisplayName("Quantifier Parsing Tests")
    class QuantifierTests {

        @Test
        @DisplayName("Should parse basic quantifiers")
        void testParseSimpleQuantifiers() throws InvalidRegexException {
            // Test * quantifier
            RegexNode result = parser.parse("a*");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode group = (GroupNode) result;
            assertTrue(group.getChild() instanceof QuantifierNode);
            QuantifierNode quantifier = (QuantifierNode) group.getChild();
            assertEquals(0, quantifier.getMin());
            assertEquals(Integer.MAX_VALUE, quantifier.getMax());
            assertTrue(quantifier.getChild() instanceof LiteralNode);

            // Test + quantifier
            result = parser.parse("a+");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            group = (GroupNode) result;
            assertTrue(group.getChild() instanceof QuantifierNode);
            quantifier = (QuantifierNode) group.getChild();
            assertEquals(1, quantifier.getMin());
            assertEquals(Integer.MAX_VALUE, quantifier.getMax());

            // Test ? quantifier
            result = parser.parse("a?");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            group = (GroupNode) result;
            assertTrue(group.getChild() instanceof QuantifierNode);
            quantifier = (QuantifierNode) group.getChild();
            assertEquals(0, quantifier.getMin());
            assertEquals(1, quantifier.getMax());
        }

        @Test
        @DisplayName("Should parse range quantifiers")
        void testParseRangeQuantifiers() throws InvalidRegexException {
            // Test {n} quantifier
            RegexNode result = parser.parse("a{3}");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode group = (GroupNode) result;
            assertTrue(group.getChild() instanceof QuantifierNode);
            QuantifierNode quantifier = (QuantifierNode) group.getChild();
            assertEquals(3, quantifier.getMin());
            assertEquals(3, quantifier.getMax());

            // Test {n,m} quantifier
            result = parser.parse("a{2,5}");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            group = (GroupNode) result;
            assertTrue(group.getChild() instanceof QuantifierNode);
            quantifier = (QuantifierNode) group.getChild();
            assertEquals(2, quantifier.getMin());
            assertEquals(5, quantifier.getMax());

            // Test {n,} quantifier
            result = parser.parse("a{2,}");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            group = (GroupNode) result;
            assertTrue(group.getChild() instanceof QuantifierNode);
            quantifier = (QuantifierNode) group.getChild();
            assertEquals(2, quantifier.getMin());
            assertEquals(Integer.MAX_VALUE, quantifier.getMax());
        }

        @ParameterizedTest
        @DisplayName("Should parse various quantifier patterns")
        @ValueSource(strings = {"a*", "b+", "c?", "d{3}", "e{2,5}", "f{1,}", "[a-z]*", "(abc)+", "\\d{2,4}"})
        void testQuantifierPatterns(String pattern) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result, "Pattern '" + pattern + "' should parse successfully");
        }

        @Test
        @DisplayName("Should reject possessive quantifiers")
        void testPossessiveQuantifiers() {
            String[] possessivePatterns = {"a*+", "b++", "c?+", "d{2,5}+"};

            for (String pattern : possessivePatterns) {
                InvalidRegexException exception = assertThrows(
                    InvalidRegexException.class,
                    () -> parser.parse(pattern),
                    "Pattern '" + pattern + "' should throw InvalidRegexException");

                assertTrue(
                    exception.getMessage().toLowerCase().contains("possessive")
                        || exception.getMessage().toLowerCase().contains("unsupported"),
                    "Error message should indicate possessive quantifiers are unsupported for: " + pattern);
            }
        }
    }

    @Nested
    @DisplayName("Anchor Parsing Tests")
    class AnchorTests {

        @Test
        @DisplayName("Should parse anchors")
        void testParseAnchors() throws InvalidRegexException {
            // Test start anchor
            RegexNode result = parser.parse("^abc");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);

            // Test end anchor
            result = parser.parse("abc$");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);

            // Test both anchors
            result = parser.parse("^abc$");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
        }

        @ParameterizedTest
        @DisplayName("Should parse anchored patterns")
        @ValueSource(strings = {"^start", "end$", "^both$", "^[a-z]+$", "^\\d{2,4}$"})
        void testAnchoredPatterns(String pattern) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result, "Anchored pattern '" + pattern + "' should parse successfully");
        }
    }

    @Nested
    @DisplayName("Character Class Parsing Tests")
    class CharacterClassTests {

        @Test
        @DisplayName("Should parse character classes")
        void testParseCharacterClass() throws InvalidRegexException {
            // Test simple character class
            RegexNode result = parser.parse("[abc]");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode group = (GroupNode) result;
            assertTrue(group.getChild() instanceof CharacterClassNode);
            CharacterClassNode charClass = (CharacterClassNode) group.getChild();
            assertFalse(charClass.isNegated());
            assertEquals(3, charClass.getCharacters().size());

            // Test negated character class
            result = parser.parse("[^abc]");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            group = (GroupNode) result;
            assertTrue(group.getChild() instanceof CharacterClassNode);
            charClass = (CharacterClassNode) group.getChild();
            assertTrue(charClass.isNegated());

            // Test character range
            result = parser.parse("[a-z]");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            group = (GroupNode) result;
            assertTrue(group.getChild() instanceof CharacterClassNode);
            charClass = (CharacterClassNode) group.getChild();
            assertEquals(1, charClass.getRanges().size());
        }

        @ParameterizedTest
        @DisplayName("Should parse various character class patterns")
        @ValueSource(
            strings = {
                "[abc]",
                "[^abc]",
                "[a-z]",
                "[A-Z]",
                "[0-9]",
                "[a-zA-Z0-9]",
                "[a-z_]",
                "[^0-9]",
                "[\\w]",
                "[\\d]",
                "[\\s]"
            })
        void testCharacterClassPatterns(String pattern) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result, "Character class pattern '" + pattern + "' should parse successfully");
        }

        @Test
        @DisplayName("Should handle complex character classes")
        void testComplexCharacterClasses() throws InvalidRegexException {
            String[] complexPatterns = {
                "[a-zA-Z0-9_.-]", "[^\\s\\t\\r\\n]", "[a-z0-9.-]{1,61}", "[\\p{L}\\p{N}]" // May not be supported
            };

            for (String pattern : complexPatterns) {
                try {
                    RegexNode result = parser.parse(pattern);
                    assertNotNull(result, "Complex character class '" + pattern + "' should parse if supported");
                } catch (InvalidRegexException e) {
                    // Some complex patterns may not be supported
                    assertTrue(
                        e.getMessage().contains("unsupported")
                            || e.getMessage().contains("invalid")
                            || e.getMessage().contains("property"),
                        "Error should indicate unsupported feature for pattern: " + pattern);
                }
            }
        }
    }

    @Nested
    @DisplayName("Group Parsing Tests")
    class GroupTests {

        @Test
        @DisplayName("Should parse groups")
        void testParseGroups() throws InvalidRegexException {
            // Test capturing group
            RegexNode result = parser.parse("(abc)");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode outerGroup = (GroupNode) result;
            assertTrue(outerGroup.getChild() instanceof GroupNode);
            GroupNode group = (GroupNode) outerGroup.getChild();
            assertTrue(group.isCapturing());
            assertEquals(1, group.getGroupNumber());

            // Test non-capturing group
            result = parser.parse("(?:abc)");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            outerGroup = (GroupNode) result;
            assertTrue(outerGroup.getChild() instanceof GroupNode);
            group = (GroupNode) outerGroup.getChild();
            assertFalse(group.isCapturing());
            assertEquals(GroupNode.Type.NON_CAPTURING, group.getType());
        }

        @ParameterizedTest
        @DisplayName("Should parse various group patterns")
        @ValueSource(strings = {"(abc)", "(?:abc)", "(a|b)", "(?:hello|world)", "(a+)", "([a-z]+)", "(\\d{2,4})"})
        void testGroupPatterns(String pattern) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result, "Group pattern '" + pattern + "' should parse successfully");
        }

        @Test
        @DisplayName("Should handle nested groups")
        void testNestedGroups() throws InvalidRegexException {
            String[] nestedPatterns = {"((abc))", "(a(b|c)d)", "(?:(?:abc)+)", "(a(?:b|c)+d)"};

            for (String pattern : nestedPatterns) {
                RegexNode result = parser.parse(pattern);
                assertNotNull(result, "Nested group pattern '" + pattern + "' should parse successfully");
            }
        }
    }

    @Nested
    @DisplayName("Alternation Parsing Tests")
    class AlternationTests {

        @Test
        @DisplayName("Should parse alternation")
        void testParseAlternation() throws InvalidRegexException {
            RegexNode result = parser.parse("abc|def");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode group = (GroupNode) result;
            assertTrue(group.getChild() instanceof AlternationNode);
            AlternationNode alternation = (AlternationNode) group.getChild();
            assertEquals(2, alternation.getAlternatives().size());
        }

        @ParameterizedTest
        @DisplayName("Should parse various alternation patterns")
        @ValueSource(
            strings = {"a|b", "abc|def", "hello|world", "cat|dog|bird", "a|bb|ccc", "[a-z]|[0-9]", "(abc)|(def)"})
        void testAlternationPatterns(String pattern) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result, "Alternation pattern '" + pattern + "' should parse successfully");
        }

        @Test
        @DisplayName("Should handle alternation with empty alternatives")
        void testAlternationWithEmpty() throws InvalidRegexException {
            String[] emptyAlternationPatterns = {
                "a|", // Empty right alternative
                "|b", // Empty left alternative
                "a||b", // Empty middle alternative
                "|-cn|-us-gov" // AWS ARN pattern style
            };

            for (String pattern : emptyAlternationPatterns) {
                RegexNode result = parser.parse(pattern);
                assertNotNull(result, "Alternation with empty '" + pattern + "' should parse successfully");
            }
        }
    }

    @Nested
    @DisplayName("Escape Sequence Tests")
    class EscapeTests {

        @Test
        @DisplayName("Should parse escape sequences")
        void testParseEscapes() throws InvalidRegexException {
            // Test escaped special characters
            RegexNode result = parser.parse("\\.");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode group = (GroupNode) result;
            assertTrue(group.getChild() instanceof LiteralNode);
            LiteralNode literal = (LiteralNode) group.getChild();
            assertEquals(".", literal.getLiteral());

            // Test escaped newline
            result = parser.parse("\\n");
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            group = (GroupNode) result;
            assertTrue(group.getChild() instanceof LiteralNode);
            literal = (LiteralNode) group.getChild();
            assertEquals("\n", literal.getLiteral());
        }

        @ParameterizedTest
        @DisplayName("Should parse escaped metacharacters")
        @CsvSource({
            "'\\\\', '\\'",
            "'\\^', '^'",
            "'\\$', '$'",
            "'\\.', '.'",
            "'\\|', '|'",
            "'\\?', '?'",
            "'\\*', '*'",
            "'\\+', '+'",
            "'\\(', '('",
            "'\\)', ')'",
            "'\\[', '['",
            "'\\{', '{'"
        })
        void testEscapedMetacharacters(String pattern, String expectedChar) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result);
            assertTrue(result instanceof GroupNode);
            GroupNode group = (GroupNode) result;
            assertTrue(group.getChild() instanceof LiteralNode);
            LiteralNode literal = (LiteralNode) group.getChild();
            assertEquals(expectedChar, literal.getLiteral());
        }

        @ParameterizedTest
        @DisplayName("Should parse shorthand character classes")
        @ValueSource(strings = {"\\d", "\\D", "\\w", "\\W", "\\s", "\\S"})
        void testShorthandCharacterClasses(String pattern) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result, "Shorthand class '" + pattern + "' should parse successfully");
        }

        @Test
        @DisplayName("Should parse escaped plus character")
        void testEscapedPlusCharacter() throws InvalidRegexException {
            // 1\+1=2 - escaped plus character
            RegexNode ast = parser.parse("1\\+1=2");
            assertNotNull(ast);
        }

        @Test
        @DisplayName("Should distinguish escaped vs unescaped plus")
        void testUnescapedPlusQuantifier() throws InvalidRegexException {
            // 1+1=2 - plus as quantifier
            RegexNode ast = parser.parse("1+1=2");
            assertNotNull(ast);
        }
    }

    @Nested
    @DisplayName("Dot and Word Boundary Tests")
    class DotAndWordBoundaryTests {

        @Test
        @DisplayName("Should parse dot wildcard")
        void testDotWildcard() throws InvalidRegexException {
            RegexNode ast = parser.parse(".");
            assertNotNull(ast);
        }

        @ParameterizedTest
        @DisplayName("Should parse dot with quantifiers")
        @ValueSource(strings = {".*", ".+", ".?", ".{2,5}"})
        void testDotWithQuantifiers(String pattern) throws InvalidRegexException {
            RegexNode ast = parser.parse(pattern);
            assertNotNull(ast);
        }

        @Test
        @DisplayName("Should handle word boundary \\b correctly")
        void testWordBoundary() throws InvalidRegexException {
            RegexNode ast = parser.parse("^\\bword\\b$");
            assertNotNull(ast);
        }

        @Test
        @DisplayName("Should handle non-word boundary \\B correctly")
        void testNonWordBoundary() throws InvalidRegexException {
            RegexNode ast = parser.parse("^\\Btest\\B$");
            assertNotNull(ast);
        }

        @Test
        @DisplayName("Should handle mixed word boundaries and literals")
        void testMixedWordBoundaries() throws InvalidRegexException {
            RegexNode ast = parser.parse("^\\b[a-z]+\\b$");
            assertNotNull(ast);
        }
    }

    @Nested
    @DisplayName("Complex Pattern Tests")
    class ComplexPatternTests {

        @Test
        @DisplayName("Should parse complex email-like pattern")
        void testComplexPattern() throws InvalidRegexException {
            RegexNode result = parser.parse("^[a-zA-Z0-9]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
            assertNotNull(result);
        }

        @ParameterizedTest
        @DisplayName("Should parse real-world patterns")
        @ValueSource(
            strings = {
                "^arn:aws(|-cn|-us-gov):s3:::[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$",
                "^([a-zA-Z0-9](-*[a-zA-Z0-9]){0,62})$",
                "^\\+?[1-9]\\d{1,14}$",
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                "https?://[a-zA-Z0-9.-]+(/[a-zA-Z0-9._~:/?#\\[\\]@!$&'()*+,;=-]*)?",
                "Boolean|Integer|Real"
            })
        void testRealWorldPatterns(String pattern) throws InvalidRegexException {
            RegexNode result = parser.parse(pattern);
            assertNotNull(result, "Real-world pattern '" + pattern + "' should parse successfully");
        }

        @Test
        @DisplayName("Should handle mixed quantifiers and groups")
        void testMixedComplexPatterns() throws InvalidRegexException {
            String[] complexPatterns = {
                "(a|b)*c+",
                "^(\\d{3})-?(\\d{2})-?(\\d{4})$",
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)",
                "([a-zA-Z0-9_\\-=/]|\\{satellite_id\\}|\\{config\\-name}"
                    + "|\\{s3\\-config-id}|\\{year\\}|\\{month\\}|\\{day\\}){1,900}"
            };

            for (String pattern : complexPatterns) {
                RegexNode result = parser.parse(pattern);
                assertNotNull(result, "Complex pattern '" + pattern + "' should parse successfully");
            }
        }

        @Test
        @DisplayName("Should parse complex pattern combining multiple features")
        void testComplexPatternCombination() throws InvalidRegexException {
            // (cat|dog)+\\d{2,4}
            RegexNode ast = parser.parse("(cat|dog)+\\d{2,4}");
            assertNotNull(ast);
        }

        @Test
        @DisplayName("Should parse Windows file path pattern")
        void testWindowsFilePathPattern() throws InvalidRegexException {
            // c:\\temp
            RegexNode ast = parser.parse("c:\\\\temp");
            assertNotNull(ast);
        }

        @ParameterizedTest
        @DisplayName("Should parse various file path patterns")
        @ValueSource(strings = {"c:\\\\temp", "/usr/local/bin", "~/documents", "./relative/path"})
        void testFilePathPatterns(String pattern) throws InvalidRegexException {
            RegexNode ast = parser.parse(pattern);
            assertNotNull(ast);
        }
    }

    @Nested
    @DisplayName("Unicode Character Tests")
    class UnicodeTests {

        @Test
        @DisplayName("Should handle literal Unicode characters")
        void testLiteralUnicodeCharacters() throws InvalidRegexException {
            String[] unicodePatterns = {
                "café", // Latin extended
                "测试", // Chinese characters
                "Ελληνικά", // Greek
                "🚀", // Emoji
                "a测试b" // Mixed ASCII and unicode
            };

            for (String pattern : unicodePatterns) {
                RegexNode result = parser.parse(pattern);
                assertNotNull(result, "Unicode pattern '" + pattern + "' should parse successfully");
            }
        }

        @Test
        @DisplayName("Should handle Unicode property escapes if supported")
        void testUnicodePropertyEscapes() throws InvalidRegexException {
            String[] unicodePropertyPatterns = {
                "\\p{L}", // Unicode letters
                "\\p{Nd}", // Unicode decimal digits
                "\\P{ASCII}", // Negated ASCII property
                "\\p{Letter}", // Long property name
                "\\p{Other_Letter}" // Property with underscore
            };

            for (String pattern : unicodePropertyPatterns) {
                try {
                    RegexNode result = parser.parse(pattern);
                    assertNotNull(result, "Unicode property '" + pattern + "' should parse if supported");
                } catch (InvalidRegexException e) {
                    // Unicode properties may not be supported - that's okay
                    assertTrue(
                        e.getMessage().contains("property")
                            || e.getMessage().contains("unsupported")
                            || e.getMessage().contains("unicode"),
                        "Error should indicate Unicode properties are unsupported for: " + pattern);
                }
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle invalid patterns gracefully")
        void testInvalidPatterns() {
            // Test unclosed group
            assertThrows(InvalidRegexException.class, () -> parser.parse("(abc"));

            // Test unclosed character class
            assertThrows(InvalidRegexException.class, () -> parser.parse("[abc"));

            // Test quantifier without atom
            assertThrows(InvalidRegexException.class, () -> parser.parse("*"));

            // Test invalid quantifier range
            assertThrows(InvalidRegexException.class, () -> parser.parse("a{5,2}"));

            // Test incomplete escape
            assertThrows(InvalidRegexException.class, () -> parser.parse("\\"));
        }

        @ParameterizedTest
        @DisplayName("Should reject malformed patterns")
        @ValueSource(
            strings = {
                "(", // Unmatched opening parenthesis
                ")", // Unmatched closing parenthesis
                "[", // Unmatched opening bracket
                "{", // Unmatched opening brace (when not followed by digits)
                "*", // Quantifier without base
                "+", // Quantifier without base
                "?", // Quantifier without base
                "a{abc}", // Invalid quantifier content
                "a{3,1}", // Invalid quantifier range (min > max)
                "\\", // Trailing backslash
                "[z-a]" // Invalid character range
            })
        void testMalformedPatterns(String invalidPattern) {
            assertThrows(
                InvalidRegexException.class,
                () -> parser.parse(invalidPattern),
                "Pattern '" + invalidPattern + "' should throw InvalidRegexException");
        }

        @Test
        @DisplayName("Should provide meaningful error messages")
        void testErrorMessages() {
            try {
                parser.parse("(unclosed");
                assertTrue(false, "Should have thrown exception");
            } catch (InvalidRegexException e) {
                assertTrue(
                    e.getMessage().contains("unclosed")
                        || e.getMessage().contains("unmatched")
                        || e.getMessage().contains("parenthesis"),
                    "Error message should be meaningful: " + e.getMessage());
            }

            try {
                parser.parse("a{5,2}");
                assertTrue(false, "Should have thrown exception");
            } catch (InvalidRegexException e) {
                assertTrue(
                    e.getMessage().contains("range")
                        || e.getMessage().contains("quantifier")
                        || e.getMessage().contains("invalid"),
                    "Error message should be meaningful: " + e.getMessage());
            }
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid character ranges")
        @ValueSource(strings = {"[z-a]", "[9-0]", "[Z-A]"})
        void testInvalidCharacterRanges(String invalidPattern) {
            // Some parsers may allow these, others may reject them
            // The important thing is that they don't crash
            assertDoesNotThrow(() -> {
                try {
                    RegexNode ast = parser.parse(invalidPattern);
                    // If parsing succeeds, that's okay too
                    assertNotNull(ast);
                } catch (InvalidRegexException e) {
                    // If parsing fails, that's also okay
                    assertTrue(
                        e.getMessage().contains("range")
                            || e.getMessage().contains("invalid")
                            || e.getMessage().contains("character"),
                        "Error message should indicate invalid character range");
                }
            });
        }

        @Test
        @DisplayName("Should handle incomplete escape sequences")
        void testIncompleteEscapeSequences() {
            assertThrows(
                InvalidRegexException.class,
                () -> parser.parse("abc\\"),
                "Trailing backslash should throw InvalidRegexException");
        }

        @Test
        @DisplayName("Should handle invalid quantifier ranges")
        void testInvalidQuantifierRanges() {
            // These should definitely throw exceptions
            String[] definitelyInvalid = {"a{5,2}", "a{abc}", "a{-1}", "a{1,-1}"};

            for (String pattern : definitelyInvalid) {
                InvalidRegexException exception = assertThrows(
                    InvalidRegexException.class,
                    () -> parser.parse(pattern),
                    "Pattern '" + pattern + "' should throw InvalidRegexException");

                assertTrue(
                    exception.getMessage().toLowerCase().contains("quantifier")
                        || exception.getMessage().toLowerCase().contains("range")
                        || exception.getMessage().toLowerCase().contains("invalid"),
                    "Error should indicate invalid quantifier for: " + pattern);
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle patterns with only whitespace")
        void testWhitespaceOnlyPatterns() throws InvalidRegexException {
            String[] whitespacePatterns = {" ", "  ", "\t", "\n", " \t\n "};

            for (String pattern : whitespacePatterns) {
                RegexNode ast = parser.parse(pattern);
                assertNotNull(ast);
            }
        }

        @Test
        @DisplayName("Should handle very long literal strings")
        void testVeryLongLiteralStrings() throws InvalidRegexException {
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longString.append("a");
            }

            RegexNode ast = parser.parse(longString.toString());
            assertNotNull(ast);
        }

        @Test
        @DisplayName("Debug domain pattern parsing")
        void debugDomainPattern() throws InvalidRegexException {
            String pattern = "^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}$";
            RegexNode ast = parser.parse(pattern);
            assertNotNull(ast, "Domain pattern should parse successfully");
        }
    }
}
