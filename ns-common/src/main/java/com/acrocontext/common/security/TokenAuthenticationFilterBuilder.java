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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.ServerFormLoginAuthenticationConverter;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticationFilterBuilder {

    private final TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler;
    private final TokenAuthenticationWebFilter tokenAuthenticationWebFilter;
    private final TokenAuthenticationManager tokenAuthenticationManager;

    @Autowired
    public TokenAuthenticationFilterBuilder(TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler,
                                            TokenAuthenticationWebFilter tokenAuthenticationWebFilter,
                                            TokenAuthenticationManager tokenAuthenticationManager) {
        this.tokenAuthenticationSuccessHandler = tokenAuthenticationSuccessHandler;
        this.tokenAuthenticationWebFilter = tokenAuthenticationWebFilter;
        this.tokenAuthenticationManager = tokenAuthenticationManager;
    }

    private ServerHttpSecurity configureLogin(ServerHttpSecurity serverHttpSecurity) {

        AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(
                this.tokenAuthenticationManager);
        authenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/api/v1/login"));
        authenticationFilter.setAuthenticationFailureHandler(new TokenAuthenticationFailureHandler());
        authenticationFilter.setAuthenticationConverter(new ServerFormLoginAuthenticationConverter());
        authenticationFilter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler);

        serverHttpSecurity.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.FORM_LOGIN);
        return serverHttpSecurity;
    }

    public ServerHttpSecurity configure(ServerHttpSecurity serverHttpSecurity,
                                        ServerWebExchangeMatcher permitAllPatternMatcher) {

        tokenAuthenticationWebFilter.setPermitAllPatternMatcher(permitAllPatternMatcher);

        configureLogin(serverHttpSecurity)
                .addFilterAt(tokenAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return serverHttpSecurity;
    }
}

