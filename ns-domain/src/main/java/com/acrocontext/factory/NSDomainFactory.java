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

package com.acrocontext.factory;

import com.acrocontext.domain.User;
import com.acrocontext.provider.CustomPasswordProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class NSDomainFactory {
  private final CustomPasswordProvider passwordProvider;

  @Autowired
  public NSDomainFactory(CustomPasswordProvider passwordProvider) {
    this.passwordProvider = passwordProvider;
  }

  public User createUser(String email, String password, List<String> roles) {
    return new User(
        UUID.randomUUID().toString(),
        email,
        passwordProvider.encode(password),
        true,
        roles,
        ZonedDateTime.now());
  }
}
