package net.englab.contextsearcher.exceptions;

/**
 * The exception is thrown if an unexpected error occurs while
 * performing Elasticsearch operations, such as connectivity issues,
 * or if Elasticsearch is down.
 */
public class ElasticOperationException extends RuntimeException {
    public ElasticOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
