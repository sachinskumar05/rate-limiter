package com.sk.ratelimiter.service;

import com.sk.ratelimiter.model.Status;

/**
 * A single Gateway instance receives connections and requests from multiple clients.
 * Assume the Gateway calls for any single client, connect / disconnect / submit are received in serial. However the Gateway can be called by different clients concurrently.
 * The lifecycle of a client connection is
 * 1) Connect
 * 2) Submit one or more requests
 * 3) Disconnect
 */
public interface Gateway {

    /**
     * To be called before attempting to submit requests
     *
     * @param client buy-side client
     */
    void connect(String client);

    /**
     * Submit a request.
     *
     * @param client buy-side client
     * @param request buy-side actual request
     * @return a Status of OK, if the request was accepted
     */
    Status submit(String client, Object request);

    /**
     * To be called when the client disconnects
     *
     * @param client buy-side client
     */
    void disconnect(String client);

}
