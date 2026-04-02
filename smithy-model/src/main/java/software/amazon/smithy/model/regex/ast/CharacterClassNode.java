package software.amazon.smithy.model.regex.ast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a character class in a regex pattern ([abc], [a-z], [^abc]).
 * Matches any single character from the specified set.
 */
public class CharacterClassNode extends RegexNode {

    /**
     * Represents a character range within a character class.
     */
    public static class CharacterRange {
        private final char start;
        private final char end;

        /**
         * Creates a character range from start to end inclusive.
         * @param start the start character of the range
         * @param end the end character of the range
         */
        public CharacterRange(char start, char end) {
            if (start > end) {
                throw new IllegalArgumentException("Range start cannot be greater than end");
            }
            this.start = start;
            this.end = end;
        }

        /**
         * Returns the start character of the range.
         * @return the start character of the range
         */
        public char getStart() {
            return start;
        }

        /**
         * Returns the end character of the range.
         * @return the end character of the range
         */
        public char getEnd() {
            return end;
        }

        /**
         * Checks if a character is within this range.
         * @param c the character to check
         * @return true if the character is within this range
         */
        public boolean contains(char c) {
            return c >= start && c <= end;
        }

        /**
         * Returns the number of characters in this range.
         * @return the number of characters in this range
         */
        public int size() {
            return end - start + 1;
        }

        @Override
        public String toString() {
            if (start == end) {
                return String.valueOf(start);
            } else {
                return new StringBuilder().append(start).append('-').append(end).toString();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CharacterRange that = (CharacterRange) obj;
            return start == that.start && end == that.end;
        }

        @Override
        public int hashCode() {
            return start * 31 + end;
        }
    }

    private final Set<Character> characters;
    private final Set<CharacterRange> ranges;
    private final boolean negated;

    /**
     * Creates a character class node.
     * @param characters individual characters in the class
     * @param ranges character ranges in the class
     * @param negated whether this is a negated character class [^...]
     */
    public CharacterClassNode(Set<Character> characters, Set<CharacterRange> ranges, boolean negated) {
        this.characters = characters != null ? new HashSet<>(characters) : new HashSet<>();
        this.ranges = ranges != null ? new HashSet<>(ranges) : new HashSet<>();
        this.negated = negated;
    }

    /**
     * Creates a simple character class with individual characters.
     * @param characters the characters to include
     * @param negated whether this is a negated character class
     * @return a new character class node
     */
    public static CharacterClassNode ofCharacters(Set<Character> characters, boolean negated) {
        Set<CharacterRange> emptyRanges = Collections.emptySet();
        return new CharacterClassNode(characters, emptyRanges, negated);
    }

    /**
     * Creates a character class with ranges.
     * @param ranges the character ranges to include
     * @param negated whether this is a negated character class
     * @return a new character class node
     */
    public static CharacterClassNode ofRanges(Set<CharacterRange> ranges, boolean negated) {
        Set<Character> emptyChars = Collections.emptySet();
        return new CharacterClassNode(emptyChars, ranges, negated);
    }

    /**
     * Creates a character class for a single character range.
     * @param start start of the range
     * @param end end of the range
     * @param negated whether this is a negated character class
     * @return a new character class node
     */
    public static CharacterClassNode ofRange(char start, char end, boolean negated) {
        Set<CharacterRange> ranges = Collections.singleton(new CharacterRange(start, end));
        return ofRanges(ranges, negated);
    }

    /**
     * Returns the individual characters in this class.
     * @return the individual characters in this class
     */
    public Set<Character> getCharacters() {
        return Collections.unmodifiableSet(characters);
    }

    /**
     * Returns the character ranges in this class.
     * @return the character ranges in this class
     */
    public Set<CharacterRange> getRanges() {
        return Collections.unmodifiableSet(ranges);
    }

    /**
     * Returns whether this is a negated character class.
     * @return true if this is a negated character class
     */
    public boolean isNegated() {
        return negated;
    }

    /**
     * Checks if this character class matches the given character.
     * @param c the character to test
     * @return true if the character matches this class
     */
    public boolean matches(char c) {
        boolean inClass = characters.contains(c) || ranges.stream().anyMatch(range -> range.contains(c));
        if (negated) {
            return !inClass;
        } else {
            return inClass;
        }
    }

    /**
     * Gets the approximate size of this character class.
     * For negated classes, returns a large number to indicate broad matching.
     * @return approximate number of characters this class can match
     */
    public int getApproximateSize() {
        if (negated) {
            // Negated classes match most characters, so treat as large set
            return 65536; // Unicode BMP size
        }

        int size = characters.size();
        for (CharacterRange range : ranges) {
            size += range.size();
        }
        return size;
    }

    @Override
    public void accept(RegexNodeVisitor visitor) {
        visitor.visitCharacterClass(this, () -> {});
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CharacterClass{");
        if (negated) {
            sb.append("^");
        }

        for (Character c : characters) {
            sb.append(c);
        }

        for (CharacterRange range : ranges) {
            sb.append(range);
        }

        sb.append("}");
        return sb.toString();
    }
}
