package com.sk.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("app-cfg")
public class AppConfig {
    private int maxRate;
    private int minRate;
    private int minTimeMillis;
    private int timeOutMillis;
}