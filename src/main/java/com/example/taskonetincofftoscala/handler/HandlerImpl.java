package com.example.taskonetincofftoscala.handler;

import com.example.taskonetincofftoscala.client.Client;
import com.example.taskonetincofftoscala.client.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HandlerImpl implements Handler {

    Client client;
    ExecutorService executorService;

    AtomicInteger retriesCount = new AtomicInteger();

    @Override
    public ApplicationStatusResponse performOperation(String id) {

        try {

            var task = new FutureTask<>(() -> getResponse(id));
            task.run();
            return task.get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {

            log.debug("TIMEOUT");
            return new ApplicationStatusResponse.Failure(null, retriesCount.get());
        }
    }

    private ApplicationStatusResponse getResponse(String id) {

        Callable<Response> getFirstService = () -> client.getApplicationStatus1(id);
        Callable<Response> getSecondService = () -> client.getApplicationStatus2(id);

        var serv = new ExecutorCompletionService<HandlerDto>(executorService);
        serv.submit(() -> {
            log.debug("get FIRST service");
            return getApplicationStatus(getFirstService, 0);
        });

        serv.submit(() -> {
            log.debug("get SECOND service");
            return getApplicationStatus(getSecondService, 0);
        });
        var failure = 0;
        while (failure < 2) {
            try {
                var res = serv.poll(15, TimeUnit.SECONDS).get();
                if (res == null) {

                    return new ApplicationStatusResponse.Failure(null, retriesCount.get());
                }

                if (res.response() instanceof Response.Success) {

                    var response = (Response.Success) res.response();
                    return new ApplicationStatusResponse.Success(id, response.applicationStatus());

                } else if (res.response() instanceof Response.RetryAfter) {

                    var response = (Response.RetryAfter) res.response();
                    retriesCount.incrementAndGet();
                    serv.submit(() -> getApplicationStatus(res.task(), response.delay().toMillis()));

                } else {

                    failure++;

                }
            } catch (Exception exception) {
                return new ApplicationStatusResponse.Failure(null, retriesCount.get());
            }
        }

        return new ApplicationStatusResponse.Failure(null, retriesCount.get());
    }

    private HandlerDto getApplicationStatus(Callable<Response> task, long delay) throws Exception {
        if (delay != 0) {
            Thread.sleep(delay);
        }
        var start = Instant.now();
        var result = task.call();
        var end = Instant.now();
        return new HandlerDto(task, result, Duration.between(start, end));
    }
}
