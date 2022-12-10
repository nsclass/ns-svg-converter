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
    private String secret = "YS5jLnIuby5zLmUuYy5yLmUudC52LmUuci55Lmwuby5uLmcucy5oLm8udS5sLmQuYi5lLjUuMS4yLnMuaS56LmUudC5oLmkucy5tLmkuZy5oLnQuYi5lLmUubi5vLnUuZy5o"; // a.c.r.o.s.e.c.r.e.t.v.e.r.y.l.o.n.g.s.h.o.u.l.d.b.e.5.1.2.s.i.z.e.t.h.i.s.m.i.g.h.t.b.e.e.n.o.u.g.h
  }

  private TokenSettings tokenSettings;
}
