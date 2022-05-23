package com.sk.ratelimiter.service;

import com.sk.ratelimiter.model.Status;

public interface RequestProcessor {
    /**
     * To be called on receipt of a request
     *
     * @param client buy-side client
     * @param request buy-side actual request
     * @param status current status
     */
    void onRequest(String client, Object request, Status status);
}
