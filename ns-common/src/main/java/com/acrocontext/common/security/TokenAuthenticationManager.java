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
import com.acrocontext.provider.CustomPasswordProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class TokenAuthenticationManager implements ReactiveAuthenticationManager {
  private final CustomUserDetailService userDetailService;
  private final CustomPasswordProvider passwordProvider;

  @Autowired
  public TokenAuthenticationManager(
      CustomUserDetailService userDetailService, CustomPasswordProvider passwordProvider) {
    this.userDetailService = userDetailService;
    this.passwordProvider = passwordProvider;
  }

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    final String username = authentication.getName();
    return userDetailService
        .findByUsername(username)
        .publishOn(Schedulers.parallel())
        .filter(
            u ->
                passwordProvider.matches((String) authentication.getCredentials(), u.getPassword()))
        .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid Credentials")))
        .map(u -> new UsernamePasswordAuthenticationToken(u, u.getPassword(), u.getAuthorities()));
  }
}
