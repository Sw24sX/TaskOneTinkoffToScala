package com.example.taskonetincofftoscala.handler;

import com.example.taskonetincofftoscala.client.Response;
import lombok.NonNull;

import java.time.Duration;
import java.util.concurrent.Callable;

public record HandlerDto(@NonNull Callable<Response> task, @NonNull Response response, @NonNull Duration lastRequestTime) {

}
