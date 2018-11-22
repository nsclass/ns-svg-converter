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

import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Date 12/22/17
 *
 * @author Nam Seob Seo
 */

public class ImageSvgConverterTest {
    @Test
    public void shouldGenerateSvgCorrectly() throws IOException {

//        ImageSvgConverter imageSvgConverter = new ImageSvgConverter();
//
//        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//        try (InputStream inputStream = classloader.getResourceAsStream("panda.png")) {
//
//            BufferedImage image = ImageIO.read(inputStream);
//            ImageData imageData = imageSvgConverter.loadImageData(image);
//            ImageConvertOptions options = ImageConvertOptions.builder().colorSampling(false).build();
//            String svg = imageSvgConverter.imageDataToSVG(imageData, options, null);
//
//            try (InputStream svgInputStream = classloader.getResourceAsStream("panda.png.svg")) {
//                try (InputStreamReader inputStreamReader = new InputStreamReader(svgInputStream)) {
//                    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
//                        String result = bufferedReader.lines()
//                                .parallel().collect(Collectors.joining("\n"));
//
//                        Assert.assertEquals(svg, result);
//                    }
//                }
//            }
//        }

    }
}
