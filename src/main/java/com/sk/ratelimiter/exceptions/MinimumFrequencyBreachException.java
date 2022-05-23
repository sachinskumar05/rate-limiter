package com.sk.ratelimiter.exceptions;

public class MinimumFrequencyBreachException extends Exception {
    public MinimumFrequencyBreachException(String message) {
        super(message);
    }
}
