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

package com.acrocontext.domain;

import lombok.Data;

/**
 * Date 11/25/17
 *
 * @author Nam Seob Seo
 */
@Data
public class ApplicationSettings {
  public ApplicationSettings() {
    this.tokenSettings = new TokenSettings();
    this.svgImageGenerationSettings = new SvgImageGenerationSettings();
  }

  @Data
  public static class SvgImageGenerationSettings {
    private boolean useLimitation = true;
    private int numberOfColors = 16;
    private int maxSupportedImageSize = 2 * 1024 * 1024;
    private int maxNumberOfColors = 16;
  }

  private SvgImageGenerationSettings svgImageGenerationSettings;

  @Data
  public static class TokenSettings {
    private int expireInSeconds = 2 * 60 * 60; // 2 hours
    private String secret = "a.c.r.o.s.e.c.r.e.t";
  }

  private TokenSettings tokenSettings;
}
