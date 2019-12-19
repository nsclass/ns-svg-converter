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

package com.acrocontext.reactive.config;

import com.acrocontext.common.security.TokenAuthenticationFilterBuilder;
import com.acrocontext.common.utils.CustomWebExchangerMatcherBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    private final TokenAuthenticationFilterBuilder authenticationBuilder;

    public SecurityConfiguration(TokenAuthenticationFilterBuilder authenticationBuilder) {
        this.authenticationBuilder = authenticationBuilder;
    }

    private Mono<AuthorizationDecision> allowSelfOrAdmin(Mono<Authentication> authentication, AuthorizationContext authorizationContext) {
        return authentication
                .map(auth -> {
                    if (auth.getAuthorities()
                            .stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"))) {
                        return true;
                    }

                    return auth.getName().equals(authorizationContext.getVariables().get("username"));
                })
                .map(AuthorizationDecision::new);
    }


    @Bean
    public SecurityWebFilterChain security(ServerHttpSecurity httpSecurity) {

        CustomWebExchangerMatcherBuilder matcherBuilder = new CustomWebExchangerMatcherBuilder();
        ServerWebExchangeMatcher permitAllPatternMatcher = matcherBuilder
                .addPattern(HttpMethod.POST, "/api/v1/login")
                .addPattern(HttpMethod.POST, "/api/v1/users/register")
                .addPattern(HttpMethod.GET, "/api/v1/products")
                .addPattern(HttpMethod.PUT, "/api/v1/svg/conversion")
                .addPattern(HttpMethod.GET, "/*")
                .addPattern(HttpMethod.GET, "/assets/css/*")
                .addPattern(HttpMethod.GET, "/assets/js/*")
                .addPattern(HttpMethod.GET, "/assets/images/*")
                .addPattern(HttpMethod.GET, "/assets/images/samples/*")
                .addPattern(HttpMethod.GET, "/assets/images/about/*")
                .addPattern(HttpMethod.GET, "/static/css/*")
                .addPattern(HttpMethod.GET, "/static/js/*")
                .addPattern(HttpMethod.GET, "/static/media/*")
                .build();

        return authenticationBuilder.configure(httpSecurity, permitAllPatternMatcher)
                .csrf()
                .disable()
                .authorizeExchange()
                    .pathMatchers(HttpMethod.GET, "/api/v1/users/{username}")
                    .access(this::allowSelfOrAdmin)
                    .and()
                .authorizeExchange()
                    .anyExchange()
                    .permitAll()
                    .and()
                .build();
    }
}
