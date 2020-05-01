package com.yannbriancon.exception;

import org.hibernate.CallbackException;

/**
 * Exception triggered when detecting a N+1 query
 */
public class NPlusOneQueryException extends CallbackException {
    public NPlusOneQueryException(String message) {
        super(message);
    }
}
