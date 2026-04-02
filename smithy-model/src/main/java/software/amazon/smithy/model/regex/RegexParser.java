package software.amazon.smithy.model.regex;

import software.amazon.smithy.model.regex.ast.AlternationNode;
import software.amazon.smithy.model.regex.ast.AnchorNode;
import software.amazon.smithy.model.regex.ast.BackreferenceNode;
import software.amazon.smithy.model.regex.ast.CharacterClassNode;
import software.amazon.smithy.model.regex.ast.DotNode;
import software.amazon.smithy.model.regex.ast.EmptyNode;
import software.amazon.smithy.model.regex.ast.GroupNode;
import software.amazon.smithy.model.regex.ast.LiteralNode;
import software.amazon.smithy.model.regex.ast.LookaheadNode;
import software.amazon.smithy.model.regex.ast.LookbehindNode;
import software.amazon.smithy.model.regex.ast.QuantifierNode;
import software.amazon.smithy.model.regex.ast.RegexNode;
import software.amazon.smithy.model.regex.ast.SequenceNode;
import software.amazon.smithy.model.regex.ast.UnicodePropertyNode;
import software.amazon.smithy.model.regex.ast.UnknownGroupNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Comprehensive regex parser that handles advanced regex constructs.
 *
 * This parser supports all the regex features needed for accurate bounds analysis,
 * including lookaround assertions, Unicode properties, and complex quantifiers.
 * Regex parser that produces a public AST for static analysis of regular expressions.
 *
 * <p>This parser exists because the AST types used internally by {@link java.util.regex.Pattern}
 * are not public, making it impossible to inspect or analyze a compiled pattern's structure.
 * By providing our own parser and AST, consumers can perform static analysis of
 * regular expressions.
 *
 * <p>The Smithy spec declares that the Regex syntax aligns with the ECMA 262 standard,
 * which, although similiar, is technically different from Java's {@link java.util.regex.Pattern}.
 *
 * <p>Supports lookaround assertions, Unicode properties, and complex quantifiers.
 */
public class RegexParser {

    private String pattern;
    private int position;
    private int length;

    /**
     * Parses a regex pattern into an AST.
     * @param pattern the regex pattern to parse
     * @return the root AST node
     * @throws InvalidRegexException if the pattern is invalid
     */
    public RegexNode parse(String pattern) throws InvalidRegexException {
        if (pattern == null) {
            throw new InvalidRegexException("Pattern cannot be null", "null");
        }

        this.pattern = pattern;
        this.position = 0;
        this.length = pattern.length();

        if (length == 0) {
            // Empty pattern matches empty string - wrap in group for consistency
            return GroupNode.nonCapturing(EmptyNode.INSTANCE);
        }

        try {
            RegexNode result = parseAlternation();

            if (position < length) {
                throw new InvalidRegexException(
                    "Unexpected character: '" + pattern.charAt(position) + "'", pattern, position);
            }

            // Handle empty pattern
            if (result == null) {
                // Empty pattern matches empty string - wrap in group for consistency
                return GroupNode.nonCapturing(EmptyNode.INSTANCE);
            }
            // Wrap the result in a non-capturing group for consistency with test expectations
            return GroupNode.nonCapturing(result);
        } catch (Exception e) {
            if (e instanceof InvalidRegexException) {
                throw e;
            }
            throw new InvalidRegexException("Failed to parse regex: " + e.getMessage(), pattern, e);
        }
    }

    /**
     * Parses alternation (|) expressions.
     */
    private RegexNode parseAlternation() throws InvalidRegexException {
        List<RegexNode> alternatives = new ArrayList<>();
        alternatives.add(parseSequence());

        while (position < length && peek() == '|') {
            consume('|');
            alternatives.add(parseSequence());
        }

        if (alternatives.size() == 1) {
            return alternatives.get(0);
        }

        return new AlternationNode(alternatives);
    }

    /**
     * Parses sequence (concatenation) expressions.
     */
    private RegexNode parseSequence() throws InvalidRegexException {
        List<RegexNode> elements = new ArrayList<>();

        while (position < length && !isAlternationEnd()) {
            RegexNode element = parseQuantified();
            if (element != null) {
                elements.add(element);
            } else {
                break;
            }
        }

        if (elements.isEmpty()) {
            return EmptyNode.INSTANCE;
        } else if (elements.size() == 1) {
            return elements.get(0);
        } else {
            return new SequenceNode(elements);
        }
    }

    /**
     * Parses quantified expressions (*, +, ?, {n,m}).
     */
    private RegexNode parseQuantified() throws InvalidRegexException {
        RegexNode base = parseAtom();
        if (base == null) {
            return null;
        }

        if (position >= length) {
            return base;
        }

        char ch = peek();
        switch (ch) {
            case '*':
                position++;
                if (position < length && peek() == '+') {
                    throw new InvalidRegexException("Possessive quantifiers (*+) are not supported", pattern, position);
                }
                return QuantifierNode.createZeroOrMore(base, !parseLazy());
            case '+':
                position++;
                if (position < length && peek() == '+') {
                    throw new InvalidRegexException("Possessive quantifiers (++) are not supported", pattern, position);
                }
                return QuantifierNode.createOneOrMore(base, !parseLazy());
            case '?':
                position++;
                if (position < length && peek() == '+') {
                    throw new InvalidRegexException("Possessive quantifiers (?+) are not supported", pattern, position);
                }
                return QuantifierNode.createZeroOrOne(base, !parseLazy());
            case '{':
                return parseRepetition(base);
            default:
                return base;
        }
    }

    /**
     * Parses repetition quantifiers {n,m}.
     */
    private RegexNode parseRepetition(RegexNode base) throws InvalidRegexException {
        consume('{');

        // Parse minimum
        int min = parseNumber();

        int max;
        if (position < length && peek() == ',') {
            consume(',');
            if (position < length && peek() == '}') {
                // {n,} - unbounded
                max = Integer.MAX_VALUE;
            } else {
                // {n,m}
                max = parseNumber();
                if (max < min) {
                    throw new InvalidRegexException(
                        "Invalid quantifier range: maximum (" + max + ") is less than minimum (" + min + ")",
                        pattern,
                        position);
                }
            }
        } else {
            // {n} - exact count
            max = min;
        }

        consume('}');

        // Check for possessive quantifier
        if (position < length && peek() == '+') {
            throw new InvalidRegexException("Possessive quantifiers ({n,m}+) are not supported", pattern, position);
        }

        return new QuantifierNode(base, min, max, !parseLazy());
    }

    /**
     * Parses atomic expressions (literals, groups, character classes, etc.).
     */
    private RegexNode parseAtom() throws InvalidRegexException {
        if (position >= length) {
            return null;
        }

        char ch = peek();

        switch (ch) {
            case '(':
                return parseGroup();
            case '[':
                return parseCharacterClass();
            case '.':
                position++;
                return DotNode.INSTANCE;
            case '^':
                position++;
                return new AnchorNode(AnchorNode.Type.START_OF_STRING);
            case '$':
                position++;
                return new AnchorNode(AnchorNode.Type.END_OF_STRING);
            case '\\':
                return parseEscape();
            case '|':
            case ')':
            case '*':
            case '+':
            case '?':
            case '{':
                // These are special characters that end the current atom
                return null;
            case '}':
                // Unmatched } is treated as a literal character in regex
                position++;
                return new LiteralNode(String.valueOf(ch));
            default:
                // Regular literal character
                position++;
                return new LiteralNode(String.valueOf(ch));
        }
    }

    /**
     * Parses group expressions (...).
     */
    private RegexNode parseGroup() throws InvalidRegexException {
        consume('(');

        // Check for special group types
        if (position < length && peek() == '?') {
            position++;
            return parseSpecialGroup();
        }

        // Regular capturing group
        RegexNode content = parseAlternation();
        consume(')');

        return new GroupNode(content, GroupNode.Type.CAPTURING, 1); // capturing group
    }

    /**
     * Parses special group constructs (?...).
     */
    private RegexNode parseSpecialGroup() throws InvalidRegexException {
        if (position >= length) {
            throw new InvalidRegexException("Incomplete special group", pattern, position);
        }

        char ch = peek();

        switch (ch) {
            case ':':
                // Non-capturing group (?:...)
                position++;
                RegexNode content = parseAlternation();
                consume(')');
                return new GroupNode(content, GroupNode.Type.NON_CAPTURING, 0); // non-capturing

            case '=':
                // Positive lookahead (?=...)
                position++;
                content = parseAlternation();
                consume(')');
                return new LookaheadNode(content, true);

            case '!':
                // Negative lookahead (?!...)
                position++;
                content = parseAlternation();
                consume(')');
                return new LookaheadNode(content, false);

            case '<':
                // Lookbehind
                position++;
                return parseLookbehind();

            default:
                // Unknown special group — preserve as a distinct node type
                RegexNode unknownContent = parseAlternation();
                consume(')');
                return new UnknownGroupNode(unknownContent);
        }
    }

    /**
     * Parses lookbehind constructs (?<...).
     */
    private RegexNode parseLookbehind() throws InvalidRegexException {
        if (position >= length) {
            throw new InvalidRegexException("Incomplete lookbehind", pattern, position);
        }

        char ch = peek();
        boolean positive;

        if (ch == '=') {
            // Positive lookbehind (?<=...)
            positive = true;
            position++;
        } else if (ch == '!') {
            // Negative lookbehind (?<!...)
            positive = false;
            position++;
        } else {
            throw new InvalidRegexException("Invalid lookbehind syntax", pattern, position);
        }

        RegexNode content = parseAlternation();
        consume(')');

        return new LookbehindNode(content, positive);
    }

    /**
     * Parses character class expressions [...].
     */
    private RegexNode parseCharacterClass() throws InvalidRegexException {
        consume('[');

        boolean negated = false;
        if (position < length && peek() == '^') {
            negated = true;
            position++;
        }

        Set<Character> characters = new HashSet<>();
        Set<CharacterClassNode.CharacterRange> ranges = new HashSet<>();

        while (position < length && peek() != ']') {
            char start = parseCharacterClassChar();

            if (position < length && peek() == '-' && position + 1 < length && pattern.charAt(position + 1) != ']') {
                consume('-');
                char end = parseCharacterClassChar();
                if (start > end) {
                    throw new InvalidRegexException(
                        "Invalid character range [" + start + "-" + end + "]", pattern, position);
                }
                ranges.add(new CharacterClassNode.CharacterRange(start, end));
            } else {
                characters.add(start);
            }
        }

        consume(']');

        return new CharacterClassNode(characters, ranges, negated);
    }

    private char parseCharacterClassChar() throws InvalidRegexException {
        if (position >= length) {
            throw new InvalidRegexException("Unexpected end in character class", pattern, position);
        }

        char c = peek();
        if (c == '\\') {
            return parseEscapeChar();
        } else {
            consume();
            return c;
        }
    }

    private char parseEscapeChar() throws InvalidRegexException {
        consume('\\');
        if (position >= length) {
            throw new InvalidRegexException("Incomplete escape sequence", pattern, position);
        }

        char c = peek();
        consume();
        return getEscapedChar(c);
    }

    private void consume() {
        position++;
    }

    /**
     * Parses escape sequences \\x.
     */
    private RegexNode parseEscape() throws InvalidRegexException {
        consume('\\');

        if (position >= length) {
            throw new InvalidRegexException("Incomplete escape sequence", pattern, position);
        }

        char ch = peek();
        position++;

        switch (ch) {
            case 'b':
                return AnchorNode.wordBoundary();
            case 'B':
                return AnchorNode.nonWordBoundary();
            case 'd':
                return createDigitClass(false);
            case 'D':
                return createDigitClass(true);
            case 's':
                return createCharacterClassNode(" \t\n\r\f", false);
            case 'S':
                return createCharacterClassNode(" \t\n\r\f", true);
            case 'w':
                return createWordClass(false);
            case 'W':
                return createWordClass(true);
            case 'p':
                return parseUnicodeProperty(false);
            case 'P':
                return parseUnicodeProperty(true);
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return new BackreferenceNode(ch - '0');
            case '{':
            case '}':
                // Escaped braces are literal characters, not quantifier delimiters
                return new LiteralNode(String.valueOf(ch));
            default:
                // Regular escaped character
                return new LiteralNode(String.valueOf(getEscapedChar(ch)));
        }
    }

    /**
     * Parses Unicode property escapes \\p{...} and \\P{...}.
     */
    private RegexNode parseUnicodeProperty(boolean negated) throws InvalidRegexException {
        consume('{');

        StringBuilder property = new StringBuilder();
        while (position < length && peek() != '}') {
            property.append(peek());
            position++;
        }

        consume('}');

        return new UnicodePropertyNode(property.toString(), negated);
    }

    // Helper methods

    private char peek() {
        return position < length ? pattern.charAt(position) : '\0';
    }

    private void consume(char expected) throws InvalidRegexException {
        if (position >= length || pattern.charAt(position) != expected) {
            throw new InvalidRegexException("Expected '" + expected + "'", pattern, position);
        }
        position++;
    }

    private boolean isAlternationEnd() {
        return position >= length || peek() == '|' || peek() == ')';
    }

    private boolean parseLazy() {
        if (position < length && peek() == '?') {
            position++;
            return true;
        }
        return false;
    }

    private int parseNumber() throws InvalidRegexException {
        int start = position;
        while (position < length && Character.isDigit(peek())) {
            position++;
        }

        if (position == start) {
            throw new InvalidRegexException("Invalid number: expected a digit", pattern, position);
        }

        try {
            return Integer.parseInt(pattern.substring(start, position));
        } catch (NumberFormatException e) {
            throw new InvalidRegexException("Invalid number", pattern, position);
        }
    }

    private char getEscapedChar(char ch) {
        switch (ch) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'r':
                return '\r';
            case 'f':
                return '\f';
            case 'a':
                return '\u0007'; // Bell
            case 'e':
                return '\u001B'; // Escape
            case 'v':
                return '\u000B'; // Vertical tab
            default:
                return ch; // Return the character as-is
        }
    }

    /**
     * Helper method to create CharacterClassNode from string.
     */
    private CharacterClassNode createCharacterClassNode(String chars, boolean negated) {
        Set<Character> charSet = new HashSet<>();
        for (char c : chars.toCharArray()) {
            charSet.add(c);
        }
        return new CharacterClassNode(charSet, Collections.emptySet(), negated);
    }

    /**
     * Creates a character class equivalent to \d: [0-9].
     */
    private CharacterClassNode createDigitClass(boolean negated) {
        Set<CharacterClassNode.CharacterRange> ranges = new HashSet<>();
        ranges.add(new CharacterClassNode.CharacterRange('0', '9'));
        return new CharacterClassNode(Collections.emptySet(), ranges, negated);
    }

    /**
     * Creates a character class equivalent to \w: [a-zA-Z0-9_].
     */
    private CharacterClassNode createWordClass(boolean negated) {
        Set<Character> chars = Collections.singleton('_');
        Set<CharacterClassNode.CharacterRange> ranges = new HashSet<>();
        ranges.add(new CharacterClassNode.CharacterRange('a', 'z'));
        ranges.add(new CharacterClassNode.CharacterRange('A', 'Z'));
        ranges.add(new CharacterClassNode.CharacterRange('0', '9'));
        return new CharacterClassNode(chars, ranges, negated);
    }
}
