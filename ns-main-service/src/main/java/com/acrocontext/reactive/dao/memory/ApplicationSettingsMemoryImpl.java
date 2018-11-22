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

package com.acrocontext.reactive.dao.memory;

import com.acrocontext.common.dao.ApplicationSettingsDao;
import com.acrocontext.domain.ApplicationSettings;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Date 12/25/17
 *
 * @author Nam Seob Seo
 */

@Repository
@Profile("dao_memory")
public class ApplicationSettingsMemoryImpl implements ApplicationSettingsDao {

    private ApplicationSettings applicationSettings = new ApplicationSettings();

    @Override
    public Mono<ApplicationSettings> loadApplicationSettings() {
        return Mono.just(applicationSettings);
    }

    @Override
    public Mono<ApplicationSettings> saveApplicationSettings(ApplicationSettings applicationSettings) {

        this.applicationSettings = applicationSettings;
        return Mono.just(applicationSettings);
    }
}
