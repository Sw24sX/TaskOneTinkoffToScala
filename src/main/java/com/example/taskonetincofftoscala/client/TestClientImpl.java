package com.example.taskonetincofftoscala.client;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Log4j2
public class TestClientImpl implements Client {

    private Integer countRetryService2 = 0;

    @Override
    public Response getApplicationStatus1(String id) {

        log.debug("SERVICE 1: start");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("SERVICE 1: end");
        return new Response.Success("Ok", id);
    }

    @Override
    public Response getApplicationStatus2(String id) {

        log.debug("SERVICE 2: start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (countRetryService2 < 3) {
            countRetryService2++;
            var now = Instant.now();
            log.debug("SERVICE 2: retry count {}", countRetryService2);
            log.debug("SERVICE 2: end");

            return new Response.RetryAfter(Duration.between(now, now.plus(1, ChronoUnit.SECONDS)));
        }
        log.debug("SERVICE 2: end");

//        return new Response.Success("Ok", id);
        return new Response.Failure(new Exception("Потому что"));
    }
}
