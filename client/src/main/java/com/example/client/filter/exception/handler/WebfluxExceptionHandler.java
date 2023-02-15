package com.example.client.filter.exception.handler;

import com.example.client.filter.exception.NotPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestControllerAdvice("com.example.rsocketclient")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class   WebfluxExceptionHandler {


    @ExceptionHandler(NotPermissionException.class)
    public Mono<ServerResponse> testing(){
        return ServerResponse.status(HttpStatus.FORBIDDEN).build();
//        return ServerResponse.status(HttpStatus.FORBIDDEN).bodyValue(notPermissionExceptionMessage);
    }
}
