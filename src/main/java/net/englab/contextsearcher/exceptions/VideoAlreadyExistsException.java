package net.englab.contextsearcher.exceptions;

/**
 * The exception is thrown when the video cannot be added because it already exists.
 */
public class VideoAlreadyExistsException extends RuntimeException {
    public VideoAlreadyExistsException(String message) {
        super(message);
    }
}
