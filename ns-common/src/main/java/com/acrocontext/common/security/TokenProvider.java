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
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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

  @Autowired
  public TokenProvider(ApplicationSettingsService applicationSettingsService) {
    this.applicationSettingsService = applicationSettingsService;
  }

  public Mono<String> getUsernameFromToken(String token) {
    return getClaimsFromToken(token).map(Claims::getSubject);
  }


  private SecretKey createKey() {
    return Keys.hmacShaKeyFor(applicationSettingsService.getTokenSettingsInCache().getSecret().getBytes(StandardCharsets.UTF_8));
  }
  public String generateToken(String username) {
    return Jwts.builder()
        .setIssuer(appName)
        .setSubject(username)
        .setIssuedAt(generateCurrentDate())
        .setExpiration(generateExpirationDate())
        .signWith(createKey())
        .compact();
  }
  public Mono<Claims> getClaimsFromToken(String token) {
    try {
      var parser = Jwts.parserBuilder().setSigningKey(createKey()).build();
      Claims claims = parser
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
    localDateTime =
        localDateTime.plusSeconds(
            applicationSettingsService.getTokenSettingsInCache().getExpireInSeconds());

    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
