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

import com.acrocontext.common.provider.CustomJsonProvider;
import com.acrocontext.common.services.ApplicationSettingsService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@EnableConfigurationProperties
public class TokenAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;
  private final CustomJsonProvider objectMapper;
  private final ApplicationSettingsService applicationSettingsService;

  @Autowired
  public TokenAuthenticationSuccessHandler(
      TokenProvider tokenProvider,
      CustomJsonProvider objectMapper,
      ApplicationSettingsService applicationSettingsService) {
    this.tokenProvider = tokenProvider;
    this.objectMapper = objectMapper;
    this.applicationSettingsService = applicationSettingsService;
  }

  @Override
  public Mono<Void> onAuthenticationSuccess(
      WebFilterExchange webFilterExchange, Authentication authentication) {

    User user = (User) authentication.getPrincipal();
    String token = tokenProvider.generateToken(user.getUsername());

    return applicationSettingsService
        .getApplicationSettingsInAsync()
        .flatMap(
            settings ->
                processLogin(
                    webFilterExchange, token, settings.getTokenSettings().getExpireInSeconds()));
  }

  private Mono<Void> processLogin(
      WebFilterExchange webFilterExchange, String token, int tokenExpire) {
    UserTokenData userTokenData = new UserTokenData(token, tokenExpire);

    return objectMapper
        .toJson(userTokenData)
        .flatMap(
            str -> {
              ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
              response.setStatusCode(HttpStatus.OK);
              response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

              DataBuffer data = encodeString(str, response.bufferFactory());
              return response
                  .writeWith(Mono.just(data))
                  .doOnError(error -> DataBufferUtils.release(data));
            });
  }

  private DataBuffer encodeString(String str, DataBufferFactory bufferFactory) {
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = bufferFactory.wrap(bytes);
    return buffer;
  }

  @Data
  private class UserTokenData {
    private String token;
    private int expires;

    UserTokenData(String token, int expires) {
      this.token = token;
      this.expires = expires;
    }
  }
}
