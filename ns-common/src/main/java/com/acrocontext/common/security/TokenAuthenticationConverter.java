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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class TokenAuthenticationConverter
    implements Function<ServerWebExchange, Mono<Authentication>> {

  private String usernameParameter = "username";

  private String passwordParameter = "password";

  @Override
  public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
    return serverWebExchange.getFormData().map(this::createAuthentication);
  }

  private UsernamePasswordAuthenticationToken createAuthentication(
      MultiValueMap<String, String> data) {
    String username = data.getFirst(this.usernameParameter);
    String password = data.getFirst(this.passwordParameter);
    return new UsernamePasswordAuthenticationToken(username, password);
  }

  /**
   * The parameter name of the form data to extract the username
   *
   * @param usernameParameter the username HTTP parameter
   */
  public void setUsernameParameter(String usernameParameter) {
    Assert.notNull(usernameParameter, "usernameParameter cannot be null");
    this.usernameParameter = usernameParameter;
  }

  /**
   * The parameter name of the form data to extract the password
   *
   * @param passwordParameter the password HTTP parameter
   */
  public void setPasswordParameter(String passwordParameter) {
    Assert.notNull(passwordParameter, "passwordParameter cannot be null");
    this.passwordParameter = passwordParameter;
  }
}
