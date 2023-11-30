package net.englab.contextsearcher.exceptions;

/**
 * This exception is thrown when we encounter
 * an unexpected error during text highlighting.
 */
public class HighlightingException extends RuntimeException {
    public HighlightingException(String message) {
        super(message);
    }
}
