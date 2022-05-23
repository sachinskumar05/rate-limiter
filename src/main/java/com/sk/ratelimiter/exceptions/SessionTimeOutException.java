package com.sk.ratelimiter.exceptions;

public class SessionTimeOutException extends Exception {
    public SessionTimeOutException(String message) {
        super(message);
    }
}
