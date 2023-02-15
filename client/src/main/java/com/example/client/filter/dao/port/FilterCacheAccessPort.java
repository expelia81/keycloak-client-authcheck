package com.example.client.filter.dao.port;

import reactor.core.publisher.Mono;

import java.util.Set;

public interface FilterCacheAccessPort {
    Mono<Set<String>> findApiByCache(String userName);

    Mono<Void> registApiSet(Set<String> apis, String userName);
}
