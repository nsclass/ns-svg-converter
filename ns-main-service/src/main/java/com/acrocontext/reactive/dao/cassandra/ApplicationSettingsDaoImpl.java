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

import com.acrocontext.cassandra.dao.ApplicationSettingsRepository;
import com.acrocontext.cassandra.domain.ApplicationSettingsData;
import com.acrocontext.common.dao.ApplicationSettingsDao;
import com.acrocontext.common.provider.CustomJsonProvider;
import com.acrocontext.domain.ApplicationSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Date 11/25/17
 *
 * @author Nam Seob Seo
 */
@Repository
@Profile({"dao_cassandra", "default"})
public class ApplicationSettingsDaoImpl implements ApplicationSettingsDao {

  private final CustomJsonProvider jsonProvider;
  private final ApplicationSettingsRepository applicationSettingsRepository;

  @Autowired
  public ApplicationSettingsDaoImpl(
      CustomJsonProvider jsonProvider,
      ApplicationSettingsRepository applicationSettingsRepository) {
    this.jsonProvider = jsonProvider;
    this.applicationSettingsRepository = applicationSettingsRepository;
  }

  @Override
  public Mono<ApplicationSettings> loadApplicationSettings() {
    return applicationSettingsRepository
        .loadSettings()
        .flatMap(
            settings -> {
              return jsonProvider.fromString(settings.getCustomData(), ApplicationSettings.class);
            });
  }

  @Override
  public Mono<ApplicationSettings> saveApplicationSettings(
      ApplicationSettings applicationSettings) {
    return jsonProvider
        .toJson(applicationSettings)
        .flatMap(
            settingsData -> {
              ApplicationSettingsData data =
                  new ApplicationSettingsData(ApplicationSettingsData.ROW_KEY, settingsData);
              return applicationSettingsRepository.save(data);
            })
        .flatMap(
            data -> {
              return jsonProvider.fromString(data.getCustomData(), ApplicationSettings.class);
            });
  }
}
