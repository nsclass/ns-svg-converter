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

package com.acrocontext.reactive.dao.cassandra;

import com.acrocontext.cassandra.dao.AdminDataRepository;
import com.acrocontext.cassandra.dao.CommonDataRepository;
import com.acrocontext.cassandra.domain.CommonData;
import com.acrocontext.cassandra.factory.CassandraDomainDataFactory;
import com.acrocontext.common.dao.UserDao;
import com.acrocontext.common.provider.CustomJsonProvider;
import com.acrocontext.common.services.RoleService;
import com.acrocontext.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@Profile({"dao_cassandra", "default"})
public class UserDaoImpl implements UserDao {
  private final CustomJsonProvider jsonProvider;
  private final CassandraDomainDataFactory domainDataFactory;
  private final CommonDataRepository commonDataRepository;
  private final AdminDataRepository adminDataRepository;
  private final RoleService roleService;

  @Autowired
  public UserDaoImpl(
      CustomJsonProvider jsonProvider,
      CassandraDomainDataFactory domainDataFactory,
      CommonDataRepository commonDataRepository,
      AdminDataRepository adminDataRepository,
      RoleService roleService) {
    this.jsonProvider = jsonProvider;
    this.domainDataFactory = domainDataFactory;
    this.commonDataRepository = commonDataRepository;
    this.adminDataRepository = adminDataRepository;
    this.roleService = roleService;
  }

  @Override
  public Mono<User> getUser(String email) {
    String rowKey = domainDataFactory.getUserPartitionKey(email);
    if (isAdmin(email)) {
      return adminDataRepository
          .findByRowKey(rowKey)
          .flatMap(data -> jsonProvider.fromString(data.getCustomData(), User.class));
    } else {
      return commonDataRepository
          .findByRowKey(rowKey)
          .flatMap(data -> jsonProvider.fromString(data.getCustomData(), User.class));
    }
  }

  private boolean isAdmin(String email) {
    return email.equals("admin@admin.com");
  }

  @Override
  public Mono<User> addUser(User user) {
    if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getPassword())) {

      String userKey = domainDataFactory.getUserPartitionKey(user.getEmail());
      return commonDataRepository
          .findByRowKey(userKey)
          .switchIfEmpty(createUser(user))
          .flatMap(commonData -> jsonProvider.fromString(commonData.getCustomData(), User.class));
    } else {
      return Mono.error(new RuntimeException("User email or password is empty"));
    }
  }

  @Override
  public Mono<User> updateUser(User user) {
    if (isAdmin(user.getEmail())) {
      return domainDataFactory
          .createAdminUserData(user)
          .flatMap(adminDataRepository::save)
          .flatMap(adminData -> jsonProvider.fromString(adminData.getCustomData(), User.class));

    } else {
      return domainDataFactory
          .createUserData(user)
          .flatMap(commonDataRepository::save)
          .flatMap(commonData -> jsonProvider.fromString(commonData.getCustomData(), User.class));
    }
  }

  private Mono<CommonData> createUser(User user) {
    // generate Id
    if (ObjectUtils.isEmpty(user.getId())) {
      user.setId(UUID.randomUUID().toString());
    }

    return domainDataFactory.createUserData(user).flatMap(commonDataRepository::save);
  }
}
