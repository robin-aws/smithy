package software.amazon.smithy.model.regex;

/**
 * Exception thrown when a regex pattern is malformed or invalid according to ECMA 262 standard.
 */
public class InvalidRegexException extends Exception {

    /** The invalid regex pattern. */
    private final String pattern;
    /** The position in the pattern where the error occurred. */
    private final int position;

    /**
     * Creates a new InvalidRegexException.
     * @param message the error message
     * @param pattern the invalid regex pattern
     */
    public InvalidRegexException(String message, String pattern) {
        this(message, pattern, -1);
    }

    /**
     * Creates a new InvalidRegexException with position information.
     * @param message the error message
     * @param pattern the invalid regex pattern
     * @param position the position in the pattern where the error occurred (-1 if unknown)
     */
    public InvalidRegexException(String message, String pattern, int position) {
        super(formatMessage(message, pattern, position));
        this.pattern = pattern;
        this.position = position;
    }

    /**
     * Creates a new InvalidRegexException with a cause.
     * @param message the error message
     * @param pattern the invalid regex pattern
     * @param cause the underlying cause
     */
    public InvalidRegexException(String message, String pattern, Throwable cause) {
        this(message, pattern, -1, cause);
    }

    /**
     * Creates a new InvalidRegexException with position and cause.
     * @param message the error message
     * @param pattern the invalid regex pattern
     * @param position the position in the pattern where the error occurred (-1 if unknown)
     * @param cause the underlying cause
     */
    public InvalidRegexException(String message, String pattern, int position, Throwable cause) {
        super(formatMessage(message, pattern, position), cause);
        this.pattern = pattern;
        this.position = position;
    }

    /**
     * Gets the invalid regex pattern.
     * @return the regex pattern that caused the error
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Gets the position where the error occurred.
     * @return the position in the pattern, or -1 if unknown
     */
    public int getPosition() {
        return position;
    }

    private static String formatMessage(String message, String pattern, int position) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        if (pattern != null) {
            sb.append(" in pattern: ").append(pattern);
        }
        if (position >= 0) {
            sb.append(" at position ").append(position);
        }
        return sb.toString();
    }
}
