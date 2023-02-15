package com.example.client.filter.apiFilter;

import com.example.client.filter.exception.NotPermissionException;
import com.example.client.filter.exception.RegistApiSetCacheException;
import com.example.client.filter.dao.port.FilterCacheAccessPort;
import com.example.client.filter.dao.port.FilterDataAccessPort;
import com.example.client.filter.token.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAKey;
import java.util.Base64;
import java.util.Set;

@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class AuthorizationFilter implements WebFilter {
    private final FilterDataAccessPort dataAccessPort;
    private final FilterCacheAccessPort cacheAccessPort;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        /* 토큰 획득 및 검증 */
        Mono<Token> token = getToken(exchange);

        /* Token 속의 Role이 가진 api 목록 호출 */
        Mono<Set<String>> apiSet = getPermissions(token);

        /* 타겟 경로 (api) */
        String targetPath = exchange.getRequest().getPath().value();

        /* 양쪽 비교, 정상적이면 다음 체인 호출, 아니면 예외 발생 */
        return apiSet.flatMap(policy -> policy.contains(targetPath) ?
                                        chain.filter(exchange) : Mono.error(NotPermissionException::new))
                    .onErrorResume(e -> { // 예외처리
                            ServerHttpResponse response = exchange.getResponse();
                            if (e instanceof NotPermissionException){  // 권한 불일치시
                                response.setStatusCode(HttpStatus.FORBIDDEN);
                            } else if (e instanceof JsonProcessingException) {  // 토큰이 바르지 않을 시
                                response.setStatusCode(HttpStatus.BAD_REQUEST);
                            } else if (e instanceof RegistApiSetCacheException) {
                                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                            } else {  //알 수 없는 에러일 때, 현재는 토큰이 바르지 않아도 이걸로 뜬다. (JsonIgnore 걸려있어서)
                                log.error("발생 예외명 : " + e.getClass().getSimpleName());
                                log.error("예외 메세지 : " + e.getMessage());
                                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            /* 예외메세지는 아직 정의하지않은 상태 */
                            return response.writeWith(Mono.fromCallable(() -> response.bufferFactory().wrap(e.getMessage().getBytes())));
//                            return response.writeWith(s -> response.bufferFactory().wrap(e.getMessage().getBytes()));
                    });
    }

    private Mono<Set<String>> getPermissions(Mono<Token> jwToken) {
        /**
         *  To Do 1 ->  DB에서 permissions 조회
         *  To Do 2 ->  Token의 UserId가 Redis에 있는지 확인 후 없으면 조회
         *
         *  로직 구조
         *      - 먼저 캐시를 조회한다.
         *          - 캐시가 있을 경우, DB를 조회하지 않는다.
         *          - 캐시가 없을 경우, DB를 조회한다.
         *      - DB 구조는 나중에 요구사항이 변경될 것을 고려하여 헥사고날 구조의 일부를 차용함.
         */
        return jwToken.flatMap(token -> {
                            /* 레디스나 캐시가 있을 경우, 레디스에서 값 조회함. */
                            Mono<Set<String>> apiSet = cacheAccessPort.findApiByCache(token.getUserName());
                            /* 캐싱데이터가 있으면 바로 통과, 없으면 DB에서 긁어오고, 캐싱데이터 등록 */
                            if(!apiSet.equals(Mono.empty())){ // 있을 경우, 캐시에서 조회.
                                return Mono.fromCallable(() -> token.getAllRoles())
                                        .flatMap(roles -> dataAccessPort.findApiByRoles(roles));
                            } else {  //없을 경우, DB 조회
                                return Mono.fromCallable(() -> token.getAllRoles())
                                        .flatMap(roles -> dataAccessPort.findApiByRoles(roles))
                                        .map(apis -> {
                                            /* 캐시에 조회된 api list 추가 */
                                            cacheAccessPort.registApiSet(apis, token.getUserName())
                                                    .onErrorResume(e -> {
                                                        /* 메인 파이프라인이 아니므로, 메인 파이프라인에서 예외를 처리할 수 있도록 던져준다. */
                                                        throw new RegistApiSetCacheException(e.getMessage());
                                                    });
                                            return apis;
                                        });
                            }
                        });
    }

    /**
     * 아직 검증 로직이 없다.
     * @param exchange
     */
    private Mono<Token> getToken(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> exchange.getRequest().getHeaders().get("Authorization").get(0).substring(7))
                    .map(jwt -> {

                        // 여기서 jwt 검증을 실시함.
                        
                        return jwt.split("\\.")[1];
                    })
                    .map(s -> new String(Base64.getDecoder().decode(s)))
                    .flatMap(s -> Mono.fromCallable(() -> new ObjectMapper().readValue(s, Token.class))); //에러발생시 resume하기위해 flatmap
    }

}
