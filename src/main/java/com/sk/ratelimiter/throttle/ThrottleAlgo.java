package com.sk.ratelimiter.throttle;

import com.sk.ratelimiter.config.AppConfig;
import com.sk.ratelimiter.exceptions.MinimumFrequencyBreachException;
import com.sk.ratelimiter.exceptions.SessionTimeOutException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope(value="prototype", proxyMode= ScopedProxyMode.TARGET_CLASS)
@Log4j2
public class ThrottleAlgo {

  private int maxRate;
  private int minRate;
  private int minTime;
  private int timeOut;
  private int counter = 0;
  private Lock lock = new ReentrantLock();

  // holds time of last action (past or future!)
  private long lastScheduledAction = System.currentTimeMillis();

  @Autowired
  private AppConfig appConfig;

  public ThrottleAlgo(){}

  @PostConstruct
  protected void init(){
    ThrottleAlgo.verify(appConfig.getMaxRate(), appConfig.getMinRate(), appConfig.getMinTimeMillis());
    this.maxRate = appConfig.getMaxRate();
    this.minRate = appConfig.getMinRate();
    this.minTime = appConfig.getMinTimeMillis();
    this.timeOut = appConfig.getTimeOutMillis();
    this.counter = this.maxRate;
  }

  public ThrottleAlgo(int maxRate, int minRate, int minTimeMillis) {
    this(maxRate, minRate, minTimeMillis, 3000);
  }

  private ThrottleAlgo(int maxRate, int minRate, int minTimeMillis, int timeOut) {
    ThrottleAlgo.verify(maxRate, minRate, minTimeMillis);
    this.maxRate = maxRate;
    this.minRate = minRate;
    this.minTime = minTimeMillis;
    this.timeOut = timeOut;
    this.counter = this.maxRate;
  }

  protected static void verify(int maxRate, int minRate, int minTimeMillis) {
    if (maxRate <= 0 || minTimeMillis <= 0 || minRate <= 0 ) {
      throw new IllegalArgumentException(
          String.format("Invalid maxRate, minRate or time %s, %s, %s", maxRate, minRate, minTimeMillis));
    }
    log.info("initializing with valid maxRate {}, minRate {}  per millis {}", maxRate, minRate, minTimeMillis);
  }

  public void reset() {
    lastScheduledAction = System.currentTimeMillis();
  }

  public boolean tryConsume() throws MinimumFrequencyBreachException, SessionTimeOutException {
    try {
      lock.lock();
      long curTime = System.currentTimeMillis();
      long timeLeft = lastScheduledAction + minTime - curTime;

      int count = maxRate - counter;

      if(timeLeft >= timeOut && count <= 0) {
        throw new SessionTimeOutException(
                String.format("Session timed-out due to No Request received within the %s milliseconds, timeleft %s, request count %s",
                        timeOut, timeLeft, count));
      }

      if(timeLeft <= 0 && count <= minRate) {
        throw new MinimumFrequencyBreachException(
                String.format("No Request received within the %s second interval, timeLeft %s, request count %s",
                        minRate, timeLeft, count ));
      }

      log.info( "Time Left {}, Permitted Count {}", timeLeft, counter);
      if (timeLeft <= 0 && counter <= 0) {
        reset();
        counter = this.maxRate;
      }

      if (timeLeft > 0 && counter > 0) {
        counter--;
        return true;
      } else {
        log.info( "Either Time window EXPIRED or no more requests are permitted. Time Left {}, Counter {} ", timeLeft, counter);
        return false;
      }
    } finally {
      lock.unlock();
    }
  }

}
