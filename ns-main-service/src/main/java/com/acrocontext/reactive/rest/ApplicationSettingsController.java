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

import com.acrocontext.common.services.ApplicationSettingsService;
import com.acrocontext.domain.ApplicationSettings;
import com.acrocontext.reactive.domain.AppSettingsView;
import com.acrocontext.reactive.domain.AppTokenSettingsView;
import com.acrocontext.reactive.domain.ApplicationConstants;
import com.acrocontext.reactive.domain.AppSvgSettingsView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
    public Mono<AppSettingsView> getApplicationSettings() {
        return applicationSettingsService.getApplicationSettingsInAsync()
                .map(this::fromApplicationSettings);
    }

    private AppSettingsView fromApplicationSettings(ApplicationSettings applicationSettings) {
        AppSettingsView appSettingsView = new AppSettingsView();
        appSettingsView.setAppTokenSettings(fromAppTokenSettings(applicationSettings.getTokenSettings()));
        appSettingsView.setAppSvgSettings(fromAppSvgSettings(applicationSettings.getSvgImageGenerationSettings()));

        return appSettingsView;
    }

    private AppTokenSettingsView fromAppTokenSettings(ApplicationSettings.TokenSettings tokenSettings) {
        AppTokenSettingsView appTokenSettingsView = new AppTokenSettingsView();
        appTokenSettingsView.setExpireInSeconds(tokenSettings.getExpireInSeconds());
        return appTokenSettingsView;
    }

    private AppSvgSettingsView fromAppSvgSettings(ApplicationSettings.SvgImageGenerationSettings settings) {
        AppSvgSettingsView svgSettings = new AppSvgSettingsView();
        svgSettings.setUseLimit(settings.isUseLimitation());
        svgSettings.setImageSizeLimitation(settings.getMaxSupportedImageSize());
        svgSettings.setNumberOfColorLimitation(settings.getMaxNumberOfColors());

        return svgSettings;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path ="/tokenSettings",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AppTokenSettingsView> updateTokenSettings(@RequestBody AppTokenSettingsView inputTokenSettings) {
        ApplicationSettings.TokenSettings tokenSettings = new ApplicationSettings.TokenSettings();

        int expireInSeconds = ApplicationConstants.DEFAULT_TOKEN_EXPIRE_TIME_IN_SECONDS;
        if (inputTokenSettings.getExpireInSeconds() > 0) {
            expireInSeconds = inputTokenSettings.getExpireInSeconds();
        }
        tokenSettings.setExpireInSeconds(expireInSeconds);
        return this.applicationSettingsService.setTokenSettingsInAsync(tokenSettings)
                .map(settings -> {
                    AppTokenSettingsView appTokenSettingsView = new AppTokenSettingsView();
                    appTokenSettingsView.setExpireInSeconds(settings.getExpireInSeconds());
                    return appTokenSettingsView;
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path ="/svgSettings",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AppSvgSettingsView> updateTokenSettings(@RequestBody AppSvgSettingsView appSvgSettingsView) {
        ApplicationSettings.SvgImageGenerationSettings svgImageGenerationSettings = new ApplicationSettings.SvgImageGenerationSettings();

        svgImageGenerationSettings.setUseLimitation(appSvgSettingsView.isUseLimit());
        svgImageGenerationSettings.setMaxNumberOfColors(appSvgSettingsView.getNumberOfColorLimitation());
        svgImageGenerationSettings.setMaxSupportedImageSize(appSvgSettingsView.getImageSizeLimitation());

        return this.applicationSettingsService.setSvgSettingsInAsync(svgImageGenerationSettings)
                .map(this::fromAppSvgSettings);
    }

}
