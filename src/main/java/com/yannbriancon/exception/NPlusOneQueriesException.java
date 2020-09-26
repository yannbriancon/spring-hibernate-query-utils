package com.yannbriancon.exception;

/**
 * Exception triggered when detecting N+1 queries
 */
public class NPlusOneQueriesException extends RuntimeException {
    public NPlusOneQueriesException(String message) {
        super(message);
    }
}
