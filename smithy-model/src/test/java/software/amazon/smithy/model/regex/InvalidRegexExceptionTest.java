package software.amazon.smithy.model.regex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InvalidRegexException Tests")
class InvalidRegexExceptionTest {

    @Test
    @DisplayName("Should create exception with message and pattern")
    void testBasicConstructor() {
        InvalidRegexException ex = new InvalidRegexException("Invalid pattern", "[abc");

        assertEquals("[abc", ex.getPattern());
        assertEquals(-1, ex.getPosition());
        assertTrue(ex.getMessage().contains("Invalid pattern"));
        assertTrue(ex.getMessage().contains("[abc"));
    }

    @Test
    @DisplayName("Should create exception with position")
    void testConstructorWithPosition() {
        InvalidRegexException ex = new InvalidRegexException("Unclosed bracket", "[abc", 0);

        assertEquals("[abc", ex.getPattern());
        assertEquals(0, ex.getPosition());
        assertTrue(ex.getMessage().contains("at position 0"));
    }

    @Test
    @DisplayName("Should create exception with cause")
    void testConstructorWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        InvalidRegexException ex = new InvalidRegexException("Parse error", "a{", cause);

        assertEquals("a{", ex.getPattern());
        assertEquals(-1, ex.getPosition());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("Should create exception with position and cause")
    void testConstructorWithPositionAndCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        InvalidRegexException ex = new InvalidRegexException("Parse error", "a{", 1, cause);

        assertEquals("a{", ex.getPattern());
        assertEquals(1, ex.getPosition());
        assertEquals(cause, ex.getCause());
        assertTrue(ex.getMessage().contains("at position 1"));
    }

    @Test
    @DisplayName("Should handle null pattern")
    void testNullPattern() {
        InvalidRegexException ex = new InvalidRegexException("Error", null);

        assertNull(ex.getPattern());
        assertFalse(ex.getMessage().contains("in pattern"));
    }

    @Test
    @DisplayName("Should format message correctly")
    void testMessageFormatting() {
        InvalidRegexException ex = new InvalidRegexException("Syntax error", "a{1,", 3);

        String message = ex.getMessage();
        assertTrue(message.contains("Syntax error"));
        assertTrue(message.contains("a{1,"));
        assertTrue(message.contains("position 3"));
    }
}
