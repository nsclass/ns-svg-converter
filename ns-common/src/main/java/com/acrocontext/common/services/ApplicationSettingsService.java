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

import com.acrocontext.common.dao.ApplicationSettingsDao;
import com.acrocontext.domain.ApplicationSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Date 11/25/17
 *
 * @author Nam Seob Seo
 */
@Service
public class ApplicationSettingsService {
  private final ApplicationSettingsDao applicationSettingsDao;
  private ApplicationSettings applicationSettings;

  @Autowired
  public ApplicationSettingsService(ApplicationSettingsDao applicationSettingsDao) {
    this.applicationSettingsDao = applicationSettingsDao;
  }

  private void refresh() {
    applicationSettingsDao
        .loadApplicationSettings()
        .switchIfEmpty(applicationSettingsDao.saveApplicationSettings(new ApplicationSettings()))
        .subscribe(
            settings -> {
              applicationSettings = settings;
            });
  }

  public Mono<ApplicationSettings> getApplicationSettingsInAsync() {
    return applicationSettingsDao
        .loadApplicationSettings()
        .switchIfEmpty(applicationSettingsDao.saveApplicationSettings(new ApplicationSettings()));
  }

  public ApplicationSettings getApplicationSettingsInCache() {
    if (applicationSettings == null) {
      applicationSettings = new ApplicationSettings();
      refresh();
    }

    return applicationSettings;
  }

  public ApplicationSettings.TokenSettings getTokenSettingsInCache() {
    return getApplicationSettingsInCache().getTokenSettings();
  }

  public Mono<ApplicationSettings.TokenSettings> setTokenSettingsInAsync(
      ApplicationSettings.TokenSettings tokenSettings) {
    getApplicationSettingsInCache().setTokenSettings(tokenSettings);
    return applicationSettingsDao
        .saveApplicationSettings(applicationSettings)
        .map(ApplicationSettings::getTokenSettings);
  }

  public Mono<ApplicationSettings.SvgImageGenerationSettings> setSvgSettingsInAsync(
      ApplicationSettings.SvgImageGenerationSettings svgImageGenerationSettings) {
    getApplicationSettingsInCache().setSvgImageGenerationSettings(svgImageGenerationSettings);
    return applicationSettingsDao
        .saveApplicationSettings(applicationSettings)
        .map(ApplicationSettings::getSvgImageGenerationSettings);
  }
}
