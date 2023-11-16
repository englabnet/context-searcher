package net.englab.contextsearcher.exceptions;

/**
 * The exception is thrown when the required video has not been found.
 */
public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(String message) {
        super(message);
    }
}
