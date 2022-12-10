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

package com.acrocontext.common.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class CustomJsonProvider {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public CustomJsonProvider() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  public Mono<String> toJson(Object entity) {
    try {
      return Mono.just(objectMapper.writeValueAsString(entity));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }

  public <T> Mono<T> fromString(String value, Class<T> entityType) {
    try {
      return Mono.just(entityType.cast(objectMapper.readValue(value, entityType)));
    } catch (IOException e) {
      return Mono.error(e);
    }
  }
}
