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
import com.acrocontext.reactive.domain.ApplicationConstants;
import com.acrocontext.reactive.domain.dto.AppSettingsDto;
import com.acrocontext.reactive.domain.dto.AppSvgSettingsDto;
import com.acrocontext.reactive.domain.dto.AppTokenSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Date 11/25/17
 *
 * @author Nam Seob Seo
 */
@RestController
@RequestMapping("/api/v1/settings")
public class ApplicationSettingsController {

  private final ApplicationSettingsService applicationSettingsService;

  @Autowired
  public ApplicationSettingsController(ApplicationSettingsService applicationSettingsService) {
    this.applicationSettingsService = applicationSettingsService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<AppSettingsDto> getApplicationSettings() {
    return applicationSettingsService
        .getApplicationSettingsInAsync()
        .map(this::fromApplicationSettings);
  }

  private AppSettingsDto fromApplicationSettings(ApplicationSettings applicationSettings) {
    AppSettingsDto appSettingsDto = new AppSettingsDto();
    appSettingsDto.setAppTokenSettings(
        fromAppTokenSettings(applicationSettings.getTokenSettings()));
    appSettingsDto.setAppSvgSettings(
        fromAppSvgSettings(applicationSettings.getSvgImageGenerationSettings()));

    return appSettingsDto;
  }

  private AppTokenSettingsDto fromAppTokenSettings(
      ApplicationSettings.TokenSettings tokenSettings) {
    AppTokenSettingsDto appTokenSettingsDto = new AppTokenSettingsDto();
    appTokenSettingsDto.setExpireInSeconds(tokenSettings.getExpireInSeconds());
    return appTokenSettingsDto;
  }

  private AppSvgSettingsDto fromAppSvgSettings(
      ApplicationSettings.SvgImageGenerationSettings settings) {
    AppSvgSettingsDto svgSettings = new AppSvgSettingsDto();
    svgSettings.setUseLimit(settings.isUseLimitation());
    svgSettings.setImageSizeLimitation(settings.getMaxSupportedImageSize());
    svgSettings.setNumberOfColorLimitation(settings.getMaxNumberOfColors());

    return svgSettings;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(
      path = "/tokenSettings",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<AppTokenSettingsDto> updateTokenSettings(
      @RequestBody AppTokenSettingsDto inputTokenSettings) {
    ApplicationSettings.TokenSettings tokenSettings = new ApplicationSettings.TokenSettings();

    int expireInSeconds = ApplicationConstants.DEFAULT_TOKEN_EXPIRE_TIME_IN_SECONDS;
    if (inputTokenSettings.getExpireInSeconds() > 0) {
      expireInSeconds = inputTokenSettings.getExpireInSeconds();
    }
    tokenSettings.setExpireInSeconds(expireInSeconds);
    return this.applicationSettingsService
        .setTokenSettingsInAsync(tokenSettings)
        .map(
            settings -> {
              AppTokenSettingsDto appTokenSettingsDto = new AppTokenSettingsDto();
              appTokenSettingsDto.setExpireInSeconds(settings.getExpireInSeconds());
              return appTokenSettingsDto;
            });
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(
      path = "/svgSettings",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<AppSvgSettingsDto> updateTokenSettings(
      @RequestBody AppSvgSettingsDto appSvgSettingsDto) {
    ApplicationSettings.SvgImageGenerationSettings svgImageGenerationSettings =
        new ApplicationSettings.SvgImageGenerationSettings();

    svgImageGenerationSettings.setUseLimitation(appSvgSettingsDto.isUseLimit());
    svgImageGenerationSettings.setMaxNumberOfColors(appSvgSettingsDto.getNumberOfColorLimitation());
    svgImageGenerationSettings.setMaxSupportedImageSize(appSvgSettingsDto.getImageSizeLimitation());

    return this.applicationSettingsService
        .setSvgSettingsInAsync(svgImageGenerationSettings)
        .map(this::fromAppSvgSettings);
  }
}
