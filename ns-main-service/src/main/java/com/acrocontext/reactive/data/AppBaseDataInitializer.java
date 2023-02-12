/*
 * Copyright 2017-2023, Nam Seob Seo
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
package com.acrocontext.reactive.data;

import com.acrocontext.cassandra.dao.AdminDataRepository;
import com.acrocontext.cassandra.factory.CassandraDomainDataFactory;
import com.acrocontext.common.services.RoleService;
import com.acrocontext.domain.Role;
import com.acrocontext.domain.User;
import com.acrocontext.factory.NSDomainFactory;
import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile({"dao_cassandra", "default"})
public class AppBaseDataInitializer {

  private final AdminDataRepository adminDataRepository;

  private final CassandraDomainDataFactory cassandraDomainDataFactory;

  private final NSDomainFactory nsDomainFactory;

  private final RoleService roleService;

  @Autowired
  public AppBaseDataInitializer(
      AdminDataRepository adminDataRepository,
      CassandraDomainDataFactory cassandraDomainDataFactory,
      NSDomainFactory nsDomainFactory,
      RoleService roleService) {
    this.adminDataRepository = adminDataRepository;
    this.cassandraDomainDataFactory = cassandraDomainDataFactory;
    this.nsDomainFactory = nsDomainFactory;
    this.roleService = roleService;
  }

  @PostConstruct
  void init() {

    // add roles
    for (String roleName : roleService.getAllRoles()) {
      Role role = new Role(UUID.randomUUID().toString(), roleName);
      cassandraDomainDataFactory
          .createRoleData(role)
          .flatMap(
              data -> {
                return adminDataRepository
                    .findByRowKey(cassandraDomainDataFactory.getRolePartitionKey(roleName))
                    .switchIfEmpty(adminDataRepository.save(data));
              })
          .subscribe(value -> log.info("Role: " + value.getPartitionKey()));
    }

    // add admin user
    User adminUser =
        nsDomainFactory.createUser(
            "admin@admin.com", "pleasechangepassword", roleService.getMapAuthorities("ADMIN"));
    cassandraDomainDataFactory
        .createAdminUserData(adminUser)
        .flatMap(
            data -> {
              return adminDataRepository
                  .findByRowKey(
                      cassandraDomainDataFactory.getUserPartitionKey(adminUser.getEmail()))
                  .switchIfEmpty(adminDataRepository.save(data));
            })
        .subscribe(value -> log.info("Admin user: " + value.getPartitionKey()));
  }
}
