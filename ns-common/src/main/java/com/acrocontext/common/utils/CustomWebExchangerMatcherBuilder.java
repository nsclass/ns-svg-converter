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

/*
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

package com.acrocontext.common.utils;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Date 11/22/17
 *
 * @author Nam Seob Seo
 */
public class CustomWebExchangerMatcherBuilder {
  private List<ServerWebExchangeMatcher> matchers = new ArrayList<>();

  public CustomWebExchangerMatcherBuilder addPattern(HttpMethod method, String pattern) {
    matchers.add(new PathPatternParserServerWebExchangeMatcher(pattern, method));
    return this;
  }

  public ServerWebExchangeMatcher build() {
    return new OrServerWebExchangeMatcher(matchers);
  }
}
