package com.example.client.filter.annotationFilter;

import com.example.client.annotation.RequiredPermission;
import com.example.client.filter.exception.NotPermissionException;
import com.example.client.filter.token.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Set;


/**
 *
 * 어노테이션에 의한 필터링이 필요할 경우에만 사용함.
 *
 */
//@Component
@Slf4j
public class AuthorizationFilter implements WebFilter {
    @Autowired
    private RequestMappingHandlerMapping mapping;

    @Value("${keylcoak.publickey}")
    private String keycloakPublicKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        /* 토큰 획득 및 검증 */
        Mono<Token> token = getToken(exchange);

        /* 요청자의 Role 에게서 policy 추출 */
        Mono<Set> permissions = getPermissions(token);

        /* target의 annotation에서 정책 획득 */
        Mono<String> targetPolicy = getPolicy(exchange);

        /* 양쪽 비교, 정상적이면 다음 체인 호출, 아니면 예외 발생 */
        return permissions.flatMap(policy ->
                                targetPolicy.flatMap(reqPermission -> policy.contains(reqPermission) ?
                                                chain.filter(exchange) : Mono.error(NotPermissionException::new)
                                ))
                        .onErrorResume(e -> {
                            ServerHttpResponse response = exchange.getResponse();
                            if (e instanceof NotPermissionException){  // 권한 불일치시
                                response.setStatusCode(HttpStatus.FORBIDDEN);
                                return response.writeWith(permissions.flatMap(set -> Mono.fromCallable(() -> response.bufferFactory().wrap(new ObjectMapper().writeValueAsString(set).getBytes()))));  // 이러면 바디에 들어갈 데이터 설정 가능.
                            } else if (e instanceof JsonProcessingException) {  // 토큰이 바르지 않을 시
                                response.setStatusCode(HttpStatus.BAD_REQUEST);
                            } else {  //알 수 없는 에러일 때, 현재는 토큰이 바르지 않아도 이걸로 뜬다. (JsonIgnore 걸려있어서)
                                log.error("발생 예외명 : " + e.getClass().getSimpleName());
                                log.error("예외 메세지 : " + e.getMessage());
                                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            return response.writeWith(Mono.empty());
                        });
    }

    private Mono<Set> getPermissions(Mono<Token> roles) {
        /**
         *  To Do 1 ->  DB에서 permissions 조회
         *  To Do 2 ->  Token의 UserId가 Redis에 있는지 확인 후 없으면 조회
         */
        return roles.map(token -> token.getAllRoles());
    }

    /**
     * 아직 검증 로직이 없다.
     * @param exchange
     */
    private Mono<Token> getToken(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> exchange.getRequest().getHeaders().get("Authorization").get(0).substring(7).split("\\.")[1])
                    .map(s -> new String(Base64.getDecoder().decode(s)))
                    .flatMap(s -> Mono.fromCallable(() -> new ObjectMapper().readValue(s, Token.class))); //에러발생시 resume하기위해 flatmap
    }

    /**
     * 타겟 어노테이션 획득
     * @param exchange
     */
    private Mono<String> getPolicy(ServerWebExchange exchange){
        return mapping.getHandler(exchange)
                      .map(o-> (HandlerMethod)o)
                      .map(handlerMethod ->  handlerMethod.getMethodAnnotation(RequiredPermission.class).value());
    }

}
