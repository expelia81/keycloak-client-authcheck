package com.example.client.filter.dao.adapter;

import com.example.client.filter.dao.port.FilterCacheAccessPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class FilterCacheAccessAdapter implements FilterCacheAccessPort {
    @Override
    public Mono<Set<String>> findApiByCache(String userName) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> registApiSet(Set<String> apis, String userName) {
        return Mono.empty();
    }
}
