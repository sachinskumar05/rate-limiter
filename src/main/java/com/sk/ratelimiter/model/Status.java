package com.sk.ratelimiter.model;

public enum Status {
    OK,
    ERROR,
    THROTTLED,
    TIMEOUT,
    MIN_FREQ_BREACH
}
