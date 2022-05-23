package com.sk.ratelimiter.service;

import com.sk.ratelimiter.exceptions.MinimumFrequencyBreachException;
import com.sk.ratelimiter.exceptions.SessionTimeOutException;
import com.sk.ratelimiter.model.Status;
import com.sk.ratelimiter.throttle.ThrottleAlgo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Scope("prototype")
@Log4j2
public class ClientGatewayEMEA implements Gateway {

    private static final Map<String, ClientGatewayEMEA> clientGatewayMap = new ConcurrentHashMap<>();

    public boolean isClientConnected(String client) {
        ClientGatewayEMEA clientGateway = clientGatewayMap.get(client);
        if(null != clientGateway) {
            return clientGateway.isConnected.get();
        }
        return false;
    }

    private AtomicBoolean isConnected = new AtomicBoolean(false);

    @Autowired
    private RequestProcessor requestProcessor;
    @Autowired
    private ThrottleAlgo throttleAlgo;

    public ClientGatewayEMEA() {}
    public ClientGatewayEMEA(RequestProcessor requestProcessor, ThrottleAlgo throttleAlgo) {
        this.requestProcessor = requestProcessor;
        this.throttleAlgo = throttleAlgo;
    }

    /**
     * To be called before attempting to submit requests
     *
     * @param client buy-side client
     */
    @Override
    public void connect(String client) {
        ClientGatewayEMEA clientGateway = clientGatewayMap.getOrDefault(client, new ClientGatewayEMEA());
        clientGatewayMap.computeIfAbsent(client, clientK-> clientGateway);
        boolean check = clientGateway.isConnected.get();
        if(!check) {
            if(clientGateway.isConnected.compareAndSet(check, true)) {
                log.info("Client {} Connected Now", client);
            } else {
                log.info("Client {} Already Connected by another thread", client);
            }
        } else {
            log.info("Client {} Already Connected",client);
        }
    }

    /**
     * Submit a request.
     *
     * @param client buy-side client
     * @param request buy-side actual request
     * @return a Status of OK, if the request was accepted
     */
    @Override
    public Status submit(String client, Object request) {
        ClientGatewayEMEA clientGateway = clientGatewayMap.get(client);
        if(null == clientGateway || !clientGateway.isConnected.get()) {
            return Status.ERROR;
        }
        try {
            if (!throttleAlgo.tryConsume()) {
                return Status.THROTTLED;
            }
        } catch (MinimumFrequencyBreachException e) {
            log.error("Client {} Minimum Frequency Breach", client, e);
            return Status.MIN_FREQ_BREACH;
        } catch (SessionTimeOutException e) {
            log.error("Client {} Its Time-Out Disconnection",client, e);
            disconnect(client);
            return Status.TIMEOUT;
        }
        requestProcessor.onRequest(client, request, Status.OK);
        log.info("Client {} Sent {} Request {} ", client, Status.OK, request);
        return Status.OK;
    }

    /**
     * To be called when the client disconnects
     *
     * @param client buy-side client
     */
    @Override
    public void disconnect(String client) {
        ClientGatewayEMEA clientGateway = clientGatewayMap.get(client);
        if(null == clientGateway || !clientGateway.isConnected.get()) {
            log.info("Client {} is not connected", client);
            return;
        }
        boolean check = clientGateway.isConnected.get();
        if(check) {
            if(clientGateway.isConnected.compareAndSet(check, false)){
                log.info("Client {} Disconnected Now", client);
            } else {
                log.info("Client {} Already Disconnected by another thread", client);
            }
        } else {
            log.info("Client {} Already Disconnected ", client);
        }
        clientGatewayMap.remove(client);
    }

}
