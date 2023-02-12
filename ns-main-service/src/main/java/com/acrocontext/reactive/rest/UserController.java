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
package com.acrocontext.reactive.rest;

import com.acrocontext.common.services.UserService;
import com.acrocontext.domain.UserRegistration;
import com.acrocontext.exceptions.BeanValidationException;
import com.acrocontext.exceptions.ChangePasswordNotMatchOldPassword;
import com.acrocontext.exceptions.ChangePasswordUserNotFound;
import com.acrocontext.exceptions.GeneralUserNotFound;
import com.acrocontext.provider.CustomPasswordProvider;
import com.acrocontext.reactive.domain.dto.GeneralResponseDto;
import com.acrocontext.reactive.domain.dto.UserDto;
import com.acrocontext.reactive.services.BeanValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  private final CustomPasswordProvider passwordProvider;

  private final BeanValidatorService validatorService;

  @Autowired
  public UserController(
      UserService userService,
      CustomPasswordProvider passwordProvider,
      BeanValidatorService validatorService) {
    this.userService = userService;
    this.passwordProvider = passwordProvider;
    this.validatorService = validatorService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<UserDto> getUser(@PathVariable String username) {

    return userService
        .findUserByEmail(username)
        .map(
            user ->
                new UserDto(
                    user.getEmail(), user.isActive(), user.getCreatedUtcDateTime().toString()));
  }

  @PreAuthorize("hasRole('MEMBER')")
  @GetMapping(path = "/operations/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<UserDto> getUserProfile() {

    return ReactiveSecurityContextHolder.getContext()
        .flatMap(
            securityContext -> {
              UserDetails userDetails =
                  (UserDetails) securityContext.getAuthentication().getDetails();

              return userService.findUserByEmail(userDetails.getUsername());
            })
        .switchIfEmpty(Mono.error(new GeneralUserNotFound("Failed to find a user")))
        .map(
            user -> {
              return new UserDto(
                  user.getEmail(), user.isActive(), user.getCreatedUtcDateTime().toString());
            });
  }

  @PostMapping(
      path = "/register",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<UserDto> addUser(@RequestBody UserRegistration userRegistration) {

    BeanValidationException exception = validatorService.validate(userRegistration);
    if (exception != null) return Mono.error(exception);

    return userService
        .registerUser(userRegistration)
        .map(
            user ->
                new UserDto(
                    user.getEmail(), user.isActive(), user.getCreatedUtcDateTime().toString()));
  }

  @PatchMapping(
      path = "/operations/password",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<GeneralResponseDto> changePassword(ServerWebExchange exchange) {

    Mono<MultiValueMap<String, String>> data = exchange.getFormData();
    return data.flatMap(
        formData -> {
          String oldPassword = formData.getFirst("oldpassword");
          String newPassword = formData.getFirst("newpassword");

          return ReactiveSecurityContextHolder.getContext()
              .flatMap(
                  securityContext -> {
                    UserDetails userDetails =
                        (UserDetails) securityContext.getAuthentication().getDetails();

                    if (passwordProvider.matches(oldPassword, userDetails.getPassword())) {
                      return userService
                          .findUserByEmail(userDetails.getUsername())
                          .flatMap(
                              user -> {
                                user.setPassword(passwordProvider.encode(newPassword));
                                return userService.updateUser(user);
                              })
                          .switchIfEmpty(
                              Mono.error(new ChangePasswordUserNotFound("Invalid user/password")));

                    } else {
                      return Mono.error(
                          new ChangePasswordNotMatchOldPassword("Invalid user/password"));
                    }
                  })
              .map(user -> new GeneralResponseDto("Successfully changed a password"));
        });
  }
}
