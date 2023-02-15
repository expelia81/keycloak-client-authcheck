package com.example.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
    /**
     * 권한 수행안되는거 찔러보려면 여기로
     * @return
     */
    @GetMapping("/token/test")
    public Mono<String> failTest(){

        return Mono.just("정상적으로 요청이 수행될 리가 없음.");
    }

    /**
     * 권한 수행되는거 찔러보려면 여기로
     * @return
     */

    @GetMapping("/token/success")
    public Mono<String> passTest(){

        return Mono.just("정상적으로 요청이 수행됨");
    }


}
