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

import com.acrocontext.common.services.ApplicationSettingsService;
import com.acrocontext.domain.ApplicationSettings;
import com.acrocontext.reactive.dao.memory.UserDaoMemoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Date 12/25/17
 *
 * @author Nam Seob Seo
 */
@RestController
@RequestMapping("/api/v1/admin")
@Profile("dao_memory")
public class MemoryDaoAdminController {

  private final UserDaoMemoryImpl userDaoMemory;

  private final ApplicationSettingsService applicationSettingsService;

  @Autowired
  public MemoryDaoAdminController(
      UserDaoMemoryImpl userDaoMemory, ApplicationSettingsService applicationSettingsService) {
    this.userDaoMemory = userDaoMemory;
    this.applicationSettingsService = applicationSettingsService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> clearData() {
    userDaoMemory.clearData();

    return applicationSettingsService
        .setTokenSettingsInAsync(new ApplicationSettings.TokenSettings())
        .then(
            applicationSettingsService.setSvgSettingsInAsync(
                new ApplicationSettings.SvgImageGenerationSettings()))
        .then(Mono.just("success"));
  }
}
