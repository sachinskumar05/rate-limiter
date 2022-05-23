package com.sk.ratelimiter.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AppConfigTest {
    AppConfig appConfig = new AppConfig();

    @Test
    void testSetMaxRate() {
        int mxR= 10;
        appConfig.setMaxRate(mxR);
        Assertions.assertEquals(mxR, appConfig.getMaxRate());
    }

    @Test
    void testSetMinRate() {
        int minR = 12;
        appConfig.setMinRate(minR);
        Assertions.assertEquals(minR, appConfig.getMinRate());
    }

    @Test
    void testSetMinTimeMillis() {
        int minTimeMill = 1000;
        appConfig.setMinTimeMillis(minTimeMill);
        Assertions.assertEquals(minTimeMill, appConfig.getMinTimeMillis());
    }

    @Test
    void testSetTimeOutMillis() {
        int timeOutMills = 50;
        appConfig.setTimeOutMillis(timeOutMills);
        Assertions.assertEquals(timeOutMills, appConfig.getTimeOutMillis());
    }
}

