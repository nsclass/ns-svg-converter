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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class TokenAuthentication extends AbstractAuthenticationToken {

  private final String token;
  private final UserDetails userDetails;

  public TokenAuthentication(UserDetails userDetails, String token) {
    super(userDetails.getAuthorities());
    super.setAuthenticated(true);
    super.setDetails(userDetails);

    this.userDetails = userDetails;
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  @Override
  public String getCredentials() {
    return token;
  }

  @Override
  public UserDetails getPrincipal() {
    return userDetails;
  }
}
