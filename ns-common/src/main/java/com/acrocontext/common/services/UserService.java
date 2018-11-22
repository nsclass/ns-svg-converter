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

package com.acrocontext.common.services;

import com.acrocontext.common.dao.UserDao;
import com.acrocontext.domain.User;
import com.acrocontext.domain.UserRegistration;
import com.acrocontext.exceptions.UserRegistrationAlreadyExist;
import com.acrocontext.exceptions.UserRegistrationInvalidData;
import com.acrocontext.exceptions.UserRegistrationPasswordMismatch;
import com.acrocontext.factory.NSDomainFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class UserService {

    private final UserDao userDao;
    private final NSDomainFactory nsDomainFactory;
    private final RoleService roleService;

    @Autowired
    public UserService(UserDao userDao,
                       NSDomainFactory nsDomainFactory,
                       RoleService roleService) {
        this.userDao = userDao;
        this.nsDomainFactory = nsDomainFactory;
        this.roleService = roleService;
    }

    public Mono<User> findUserByEmail(String email) {
        return userDao.getUser(email);
    }

    public Mono<User> registerUser(UserRegistration userRegistration) {
        if (StringUtils.isEmpty(userRegistration.getEmail()) ||
                StringUtils.isEmpty(userRegistration.getPassword())) {
            return Mono.error(new UserRegistrationInvalidData("Invalid user registration data"));
        }

        if (!userRegistration.getPassword().equals(userRegistration.getPasswordConfirm())) {
            return Mono.error(new UserRegistrationPasswordMismatch("password mismatched"));
        }

        Mono<User> result = userDao.getUser(userRegistration.getEmail())
                .flatMap(user -> Mono.error(new UserRegistrationAlreadyExist(user.getEmail() + " already exists")));

        return result.switchIfEmpty(saveUser(userRegistration));
    }

    public Mono<User> updateUser(User user) {
        return userDao.updateUser(user);
    }

    private Mono<User> saveUser(UserRegistration userRegistration) {
        User user = nsDomainFactory.createUser(userRegistration.getEmail(),
                userRegistration.getPassword(), roleService.getMapAuthorities("MEMBER"));

        return userDao.addUser(user);
    }

}
