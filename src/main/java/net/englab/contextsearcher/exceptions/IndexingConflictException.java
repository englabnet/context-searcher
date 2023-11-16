package net.englab.contextsearcher.exceptions;

/**
 * The exception is thrown when the operation cannot proceed due to a running indexing job.
 */
public class IndexingConflictException extends RuntimeException {
    public IndexingConflictException(String message) {
        super(message);
    }
}
