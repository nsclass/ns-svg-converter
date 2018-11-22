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

package com.acrocontext.reactive.rest;

import com.acrocontext.common.services.UserService;
import com.acrocontext.domain.UserRegistration;
import com.acrocontext.exceptions.BeanValidationException;
import com.acrocontext.exceptions.ChangePasswordNotMatchOldPassword;
import com.acrocontext.exceptions.ChangePasswordUserNotFound;
import com.acrocontext.exceptions.GeneralUserNotFound;
import com.acrocontext.provider.CustomPasswordProvider;
import com.acrocontext.reactive.domain.GeneralResponseView;
import com.acrocontext.reactive.domain.UserView;
import com.acrocontext.reactive.services.BeanValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final CustomPasswordProvider passwordProvider;
    private final BeanValidatorService validatorService;

    @Autowired
    public UserController(UserService userService,
                          CustomPasswordProvider passwordProvider,
                          BeanValidatorService validatorService) {
        this.userService = userService;
        this.passwordProvider = passwordProvider;
        this.validatorService = validatorService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserView> getUser(@PathVariable String username) {

        return userService.findUserByEmail(username)
                .map(user -> {
                    return new UserView(user.getEmail(), user.isActive(), user.getCreatedUtcDateTime().toString());
                });
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping(path = "/operations/profile",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserView> getUserProfile() {

        return ReactiveSecurityContextHolder
                .getContext()
                .flatMap(securityContext -> {
                    UserDetails userDetails = (UserDetails)securityContext.getAuthentication()
                            .getDetails();

                    return userService.findUserByEmail(userDetails.getUsername());
                })
                .switchIfEmpty(Mono.error(new GeneralUserNotFound("Failed to find a user")))
                .map(user -> {
                    return new UserView(user.getEmail(), user.isActive(), user.getCreatedUtcDateTime().toString());
                });

    }

    @PostMapping(path = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserView> addUser(@RequestBody UserRegistration userRegistration) {

        BeanValidationException exception = validatorService.validate(userRegistration);
        if (exception != null)
            return Mono.error(exception);

        return userService.registerUser(userRegistration)
                .map(user -> {
                    return new UserView(user.getEmail(), user.isActive(), user.getCreatedUtcDateTime().toString());
                });
    }

    @PatchMapping(path = "/operations/password",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GeneralResponseView> changePassword(@RequestBody MultiValueMap<String, String> data) {
        String oldPassword = data.getFirst("oldpassword");
        String newPassword = data.getFirst("newpassword");

        return ReactiveSecurityContextHolder
                .getContext()
                .flatMap(securityContext -> {
                    UserDetails userDetails = (UserDetails)securityContext.getAuthentication()
                            .getDetails();

                    if (passwordProvider.matches(oldPassword, userDetails.getPassword())) {
                        return userService.findUserByEmail(userDetails.getUsername())
                                .flatMap(user -> {
                                    user.setPassword(passwordProvider.encode(newPassword));
                                   return userService.updateUser(user);
                                })
                                .switchIfEmpty(Mono.error(new ChangePasswordUserNotFound("User not found")));

                    } else {
                        return Mono.error(new ChangePasswordNotMatchOldPassword("Password not matched"));
                    }
                })
                .map(user -> new GeneralResponseView("Successfully changed a password"));
    }
}
