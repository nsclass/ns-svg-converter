/*
 * Copyright 2017-present, Nam Seob Seo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

package com.acrocontext.common.security;

import com.acrocontext.common.services.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;


@Component
public class TokenAuthenticationWebFilter implements WebFilter {
    private static final String TOKEN = "Token";

    private final CustomUserDetailService userDetailService;
    private final TokenProvider tokenProvider;

    private DefaultTokenProtectionMatcher tokenProtectionMatcher = new DefaultTokenProtectionMatcher();

    private ServerWebExchangeMatcher permitAllPatternMatcher = ServerWebExchangeMatchers.pathMatchers("/api/v1/users/register", "/api/v1/login");

    private ServerSecurityContextRepository securityContextRepository = new WebSessionServerSecurityContextRepository();

    private ServerAuthenticationSuccessHandler authenticationSuccessHandler = new WebFilterChainServerAuthenticationSuccessHandler();

    @Autowired
    public TokenAuthenticationWebFilter(CustomUserDetailService userDetailService,
                                        TokenProvider tokenProvider) {
        this.userDetailService = userDetailService;
        this.tokenProvider = tokenProvider;
    }

    public void setPermitAllPatternMatcher(ServerWebExchangeMatcher matcher) {
        permitAllPatternMatcher = matcher;
    }

    private Mono<TokenAuthentication> tryTokenAuthentication(String token) {
        return tokenProvider.getUsernameFromToken(token)
                .flatMap(username -> {
                    return userDetailService.findByUsername(username)
                            .map(userDetails -> {

                                return new TokenAuthentication(userDetails, token);
                            })
                            .switchIfEmpty(Mono.error(new AccessDeniedException("Invalid Authentication Token")));
                });
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return this.permitAllPatternMatcher.matches(exchange)
                .flatMap(matchResult -> {
                    if (matchResult.isMatch()) {
                         return continueWebFilterChain(exchange, chain);
                    } else {
                         return authenticate(exchange, chain);
                    }
                });
    }

    private Mono<Void> continueWebFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, chain);
        return webFilterExchange.getChain().filter(exchange);
    }

    private Mono<Void> authenticate(ServerWebExchange exchange, WebFilterChain chain) {
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, chain);

        return this.tokenProtectionMatcher.matches(exchange)
                .filter(matchResult -> matchResult.isMatch() && matchResult.getVariables().containsKey(TOKEN))
                .map(found -> (String)found.getVariables().get(TOKEN))
                .flatMap(token -> tryTokenAuthentication(token))
                .flatMap(authenticate -> onAuthenticationSuccess(authenticate, webFilterExchange));

    }

    private Mono<Void> onAuthenticationSuccess(Authentication authentication, WebFilterExchange webFilterExchange) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        return this.securityContextRepository.save(exchange, securityContext)
                .then(this.authenticationSuccessHandler
                        .onAuthenticationSuccess(webFilterExchange, authentication))
                .subscriberContext(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }

    private static class DefaultTokenProtectionMatcher implements ServerWebExchangeMatcher {

        private static final String BEARER = "Bearer ";

        @Override
        public Mono<MatchResult> matches(ServerWebExchange serverWebExchange) {

            ServerHttpRequest request = serverWebExchange.getRequest();

            String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authorization == null) {
                return Mono.error(new AccessDeniedException("Access denied: invalid token access"));
            }

            Map<String, Object> matchResult = new HashMap<>();

            if (authorization.length() <= BEARER.length()) {
                return Mono.error(new AccessDeniedException("Access denied: invalid bearer token access"));
            } else {
                String token = authorization.substring(BEARER.length(), authorization.length());
                matchResult.put(TOKEN, token);
                return MatchResult.match(matchResult);
            }
        }
    }
}
