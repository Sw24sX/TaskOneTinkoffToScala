package com.example.taskonetincofftoscala;

import com.example.taskonetincofftoscala.handler.Handler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Log4j2
public class Start implements CommandLineRunner {

    Handler handler;

    @Override
    public void run(String... args) {
        var result = handler.performOperation(UUID.randomUUID().toString());
        log.info(result);
    }
}
