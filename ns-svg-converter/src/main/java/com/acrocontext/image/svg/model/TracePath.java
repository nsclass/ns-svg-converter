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
package com.acrocontext.image.svg.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Date 12/24/17
 *
 * @author Nam Seob Seo
 */
@Value
@AllArgsConstructor
public class TracePath {

  private final List<Double[]> tracePath;

  public double valueAt(int idx1, int idx2) {
    return tracePath.get(idx1)[idx2];
  }
}
