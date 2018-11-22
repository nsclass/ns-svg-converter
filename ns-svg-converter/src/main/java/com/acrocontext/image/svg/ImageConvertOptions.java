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

package com.acrocontext.image.svg;

import lombok.Builder;
import lombok.Data;

/**
 * Date 12/21/17
 *
 * @author Nam Seob Seo
 */

@Data
@Builder
public class ImageConvertOptions {
    @Builder.Default
    float lThreshold = 1f;
    @Builder.Default
    float qThreshold = 1f;
    @Builder.Default
    int pathOmit = 8;
    @Builder.Default
    boolean colorSampling = true;
    @Builder.Default
    int numberOfColors = 16;
    @Builder.Default
    float minColorRatio = 0.02f;
    @Builder.Default
    int colorQuantCycles = 3;
    @Builder.Default
    float scale = 1f;
    @Builder.Default
    float simplifyTolerance = 0f;
    @Builder.Default
    float roundCoords = 1f;
    @Builder.Default
    float lCpr = 0f;
    @Builder.Default
    float qCpr = 0f;
    @Builder.Default
    boolean showDescription = false;
    @Builder.Default
    float viewBox = 0f;
    @Builder.Default
    float blurRadius = 0f;
    @Builder.Default
    float blurDelta = 20f;
}
