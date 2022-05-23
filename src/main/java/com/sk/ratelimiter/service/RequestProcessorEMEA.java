package com.sk.ratelimiter.service;

import com.sk.ratelimiter.model.Status;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RequestProcessorEMEA implements RequestProcessor{

    /**
     * To be called on receipt of a request
     *
     * @param client buy-side client
     * @param request buy-side actual request
     * @param status current status
     */
    @Override
    public void onRequest(String client, Object request, Status status) {
        log.info("Messge Processed for Client {}, Status {}, Message {}", client, status, request);
    }

}
