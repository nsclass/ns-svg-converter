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

import com.acrocontext.common.dao.UserDao;
import com.acrocontext.common.services.RoleService;
import com.acrocontext.domain.User;
import com.acrocontext.factory.NSDomainFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Date 12/25/17
 *
 * @author Nam Seob Seo
 */
@Service
@Profile({"dao_memory"})
public class AppBaseMemoryDataInitializer {

  private final NSDomainFactory nsDomainFactory;

  private final RoleService roleService;

  private final UserDao userDao;

  public AppBaseMemoryDataInitializer(
      NSDomainFactory nsDomainFactory, RoleService roleService, UserDao userDao) {
    this.nsDomainFactory = nsDomainFactory;
    this.roleService = roleService;
    this.userDao = userDao;
  }

  @PostConstruct
  void init() {
    // add admin user
    User adminUser =
        nsDomainFactory.createUser(
            "admin@admin.com", "pleasechangepassword", roleService.getMapAuthorities("ADMIN"));

    userDao.addUser(adminUser);
  }
}
