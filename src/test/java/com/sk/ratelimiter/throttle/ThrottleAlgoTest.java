package com.sk.ratelimiter.throttle;

import com.sk.ratelimiter.config.AppConfig;
import com.sk.ratelimiter.exceptions.MinimumFrequencyBreachException;
import com.sk.ratelimiter.exceptions.SessionTimeOutException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

@Log4j2
class ThrottleAlgoTest {

    @Mock
    AppConfig appConfig;

    @InjectMocks
    ThrottleAlgo throttleAlgo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testReset() {
        throttleAlgo = new ThrottleAlgo(1,1, 6000);
        throttleAlgo.reset();
        Assertions.assertNotNull(throttleAlgo);
    }

    @Test
    void testInit() {
        when(appConfig.getMaxRate()).thenReturn(10);
        when(appConfig.getMinRate()).thenReturn(1);
        when(appConfig.getMinTimeMillis()).thenReturn(100);
        when(appConfig.getTimeOutMillis()).thenReturn(3000);
        Assertions.assertDoesNotThrow(()-> throttleAlgo.init());

    }

    @Test
    void testVerify() {
        Assertions.assertThrows(IllegalArgumentException.class, ()->new ThrottleAlgo(0, 10, 100));
        Assertions.assertThrows(IllegalArgumentException.class, ()->new ThrottleAlgo(10, 0, 100));
        Assertions.assertThrows(IllegalArgumentException.class, ()->new ThrottleAlgo(10, 10, 0));
    }

    @Test
    void testTryConsume() throws Exception {
        int maxRateTest = 40;
        throttleAlgo = new ThrottleAlgo(maxRateTest, 1, 50);
        boolean result = throttleAlgo.tryConsume();
        Assertions.assertTrue(result);

        for(int i=0; i<maxRateTest; i++) {
            result = throttleAlgo.tryConsume();
        }
        Assertions.assertFalse(result);
    }


    @Test
    void testMinimumFrequencyBreachException() {
        throttleAlgo = new ThrottleAlgo(10, 1, 10);
        try {
            TimeUnit.MILLISECONDS.sleep(10);
            Assertions.assertThrows(MinimumFrequencyBreachException.class, ()->throttleAlgo.tryConsume());
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Test
    void testTryConsumeMakeOnlyOneGetProcessed() {
        throttleAlgo = new ThrottleAlgo(1, 1, 6000);
        int slots = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(slots);

        for (int i = 0; i < slots; i++) {
            int finalI = i;
            executorService.submit(
                    () -> {
                        try {
                            if (throttleAlgo.tryConsume()) {
                                log.info("Pass Through Throttle {}" , finalI );
                            } else {
                                log.info("Failed to be sent {} ", finalI);
                            }
                        } catch (MinimumFrequencyBreachException e) {
                            log.error("Minimum Frequency Breach", e);
                        } catch (SessionTimeOutException e) {
                            log.error("Its time-out disconnection", e);
                        }

                    });
        }
        try {
            if(executorService.awaitTermination(1000, TimeUnit.MICROSECONDS))
                executorService.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    void testTryConsumeMakeGetAllProcessed() {
        throttleAlgo = new ThrottleAlgo(10, 1, 6000);
        int slots = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(slots);

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executorService.submit(
                    () -> {
                        try {
                            if (throttleAlgo.tryConsume()) {
                                log.info("Pass Through Throttle {}" , finalI );
                            } else {
                                log.info("Failed to be sent {} ", finalI);
                            }
                        } catch (MinimumFrequencyBreachException e) {
                            log.error("Minimum Frequency Breach", e);
                        } catch (SessionTimeOutException e) {
                            log.error("Its time-out disconnection", e);
                        }
                    });
        }
        try {
            if(executorService.awaitTermination(1000, TimeUnit.MICROSECONDS))
                executorService.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

