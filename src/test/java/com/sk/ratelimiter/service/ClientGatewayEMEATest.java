package com.sk.ratelimiter.service;

import com.sk.ratelimiter.exceptions.MinimumFrequencyBreachException;
import com.sk.ratelimiter.exceptions.SessionTimeOutException;
import com.sk.ratelimiter.model.Status;
import com.sk.ratelimiter.throttle.ThrottleAlgo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

class ClientGatewayEMEATest {

    RequestProcessor requestProcessor = new RequestProcessorEMEA();

    @Mock
    ThrottleAlgo throttleAlgo = mock(ThrottleAlgo.class);

    ClientGatewayEMEA clientGatewayEMEA = new ClientGatewayEMEA(requestProcessor, throttleAlgo);

    @BeforeEach
    void setUp() {

    }

    @Test
    void testConnect() {
        String testClient = "TestClient";
        clientGatewayEMEA.connect(testClient);
        Assertions.assertTrue(clientGatewayEMEA.isClientConnected(testClient));
    }

    @Test
    void testSubmitWhenNotConnected() throws MinimumFrequencyBreachException, SessionTimeOutException {
        //DISCONNECT Throttle Algo Test Cases are covered in separate test class
        when(throttleAlgo.tryConsume()).thenReturn(false);
        String testClient = "TestClient";
        clientGatewayEMEA.disconnect(testClient);
        Status result = clientGatewayEMEA.submit(testClient, "request");
        Assertions.assertEquals(Status.ERROR, result);
    }

    @Test
    void testSubmitWhenConnected() throws MinimumFrequencyBreachException, SessionTimeOutException {
        //CONNECT Throttle Algo Test Cases are covered in separate test class
        when(throttleAlgo.tryConsume()).thenReturn(true);
        String testClient = "TestClient";
        clientGatewayEMEA.connect(testClient);
        Status result = clientGatewayEMEA.submit(testClient, "request");
        Assertions.assertEquals(Status.OK, result);
        clientGatewayEMEA.disconnect(testClient);
    }

    @Test
    void testSubmitToBeThrottled() throws MinimumFrequencyBreachException, SessionTimeOutException {
        //Throttle Algo Test Cases are covered in separate test class
        when(throttleAlgo.tryConsume()).thenReturn(false);
        String testClient = "TestClient";
        clientGatewayEMEA.connect(testClient);
        Status result = clientGatewayEMEA.submit(testClient, "request");
        Assertions.assertEquals(Status.THROTTLED, result);
    }

    @Test
    void testSubmitToBeMinFreqBreach() throws MinimumFrequencyBreachException, SessionTimeOutException {
        //MIN FREQ Throttle Algo Test Cases are covered in separate test class
        when(throttleAlgo.tryConsume()).thenThrow(MinimumFrequencyBreachException.class);
        String testClient = "TestClient";
        clientGatewayEMEA.connect(testClient);
        Status result = clientGatewayEMEA.submit(testClient, "request");
        Assertions.assertEquals(Status.MIN_FREQ_BREACH, result);
    }

    @Test
    void testSubmitToBeTimedOut() throws MinimumFrequencyBreachException, SessionTimeOutException {
        //TIME OUT Throttle Algo Test Cases are covered in separate test class
        when(throttleAlgo.tryConsume()).thenThrow(SessionTimeOutException.class);
        String testClient = "TestClient";
        clientGatewayEMEA.connect(testClient);
        Status result = clientGatewayEMEA.submit(testClient, "request");
        Assertions.assertEquals(Status.TIMEOUT, result);
    }

    @Test
    void testDisconnect() {
        String testClient = "TestClient";
        clientGatewayEMEA.disconnect(testClient);
        Assertions.assertFalse(clientGatewayEMEA.isClientConnected(testClient));
    }

    @Test
    void testConnectDisconnect() {
        String testClient = "TestClient";
        clientGatewayEMEA.connect(testClient);
        Assertions.assertTrue(clientGatewayEMEA.isClientConnected(testClient));
        clientGatewayEMEA.disconnect(testClient);
        Assertions.assertFalse(clientGatewayEMEA.isClientConnected(testClient));
    }

}

