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

import com.acrocontext.common.services.ApplicationSettingsService;
import com.acrocontext.exceptions.TokenExpiredException;
import com.acrocontext.exceptions.TokenGeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@Slf4j
@Component
@EnableConfigurationProperties
public class TokenProvider {

    @Value("${app.name}")
    private String appName;

    private final ApplicationSettingsService applicationSettingsService;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    @Autowired
    public TokenProvider(ApplicationSettingsService applicationSettingsService) {
        this.applicationSettingsService = applicationSettingsService;
    }

    public Mono<String> getUsernameFromToken(String token) {
        return getClaimsFromToken(token)
                .map(Claims::getSubject);
    }

    public String generateToken(String username) {
        String token = Jwts.builder()
                .setIssuer(appName)
                .setSubject(username)
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM,
                        applicationSettingsService.getTokenSettingsInCache().getSecret())
                .compact();
        return token;
    }

    public Mono<Claims> getClaimsFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(applicationSettingsService.getTokenSettingsInCache().getSecret())
                    .parseClaimsJws(token)
                    .getBody();

            return Mono.just(claims);
        } catch (ExpiredJwtException e) {
            return Mono.error(new TokenExpiredException(e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Mono.error(new TokenGeneralException(e.getMessage()));
        }
    }


    private Date generateCurrentDate() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Date generateExpirationDate() {

        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.plusSeconds(applicationSettingsService.getTokenSettingsInCache().getExpireInSeconds());

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
