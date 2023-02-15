package com.example.client.filter.dao.adapter;

import com.example.client.filter.dao.port.FilterDataAccessPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Component
public class FilterDataAccessAdapter implements FilterDataAccessPort {
    @Override
    public Mono<Set<String>> findApiByRoles(Set set) {

        Set<String> returnSet = new HashSet<>();
        returnSet.add("/token/success");


        return Mono.just(returnSet);
    }
}
