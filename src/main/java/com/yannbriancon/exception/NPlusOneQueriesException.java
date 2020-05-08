package com.yannbriancon.exception;

import org.hibernate.CallbackException;

/**
 * Exception triggered when detecting N+1 queries
 */
public class NPlusOneQueriesException extends CallbackException {
    public NPlusOneQueriesException(String message, Exception cause) {
        super(message, cause);
    }

    public NPlusOneQueriesException(String message) {
        super(message);
    }
}
