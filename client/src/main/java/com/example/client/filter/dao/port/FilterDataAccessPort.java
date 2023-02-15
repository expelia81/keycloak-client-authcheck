package com.example.client.filter.dao.port;

import reactor.core.publisher.Mono;

import java.util.Set;

public interface FilterDataAccessPort {
    Mono<Set<String>> findApiByRoles(Set set);
}
