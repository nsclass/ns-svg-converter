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

package com.acrocontext.cassandra.factory;

import com.acrocontext.cassandra.domain.AdminData;
import com.acrocontext.cassandra.domain.CommonData;
import com.acrocontext.common.provider.CustomJsonProvider;
import com.acrocontext.domain.Role;
import com.acrocontext.domain.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Profile({"dao_cassandra", "default"})
public class CassandraDomainDataFactory {
  private final CustomJsonProvider jsonProvider;

  public CassandraDomainDataFactory(CustomJsonProvider jsonProvider) {
    this.jsonProvider = jsonProvider;
  }

  public String getUserRowKey(String email) {
    return "USER_DATA$" + email;
  }

  public Mono<CommonData> createUserData(User user) {
    return jsonProvider
        .toJson(user)
        .map(value -> new CommonData(getUserRowKey(user.getEmail()), user.getId(), value));
  }

  public String getRoleRowKey(String roleName) {
    return "ROLE_DATA$" + roleName;
  }

  public Mono<AdminData> createRoleData(Role role) {
    return jsonProvider
        .toJson(role)
        .map(value -> new AdminData(getRoleRowKey(role.getRoleName()), role.getId(), value));
  }

  public Mono<AdminData> createAdminUserData(User user) {
    return jsonProvider
        .toJson(user)
        .map(value -> new AdminData(getUserRowKey(user.getEmail()), user.getId(), value));
  }
}
