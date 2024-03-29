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
package com.acrocontext.cassandra.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Date 11/25/17
 *
 * @author Nam Seob Seo
 */
@Data
@AllArgsConstructor
@Table("app_settings_data")
public class ApplicationSettingsData {

  public static final String ROW_KEY = "APP_SETTINGS_DATA";

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
  private String partitionKey = ROW_KEY;

  private String customData;
}
