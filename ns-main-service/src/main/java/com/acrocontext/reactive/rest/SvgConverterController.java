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

package com.acrocontext.reactive.rest;

import com.acrocontext.common.services.ApplicationSettingsService;
import com.acrocontext.domain.ApplicationSettings;
import com.acrocontext.exceptions.SvgImageGenerationError;
import com.acrocontext.image.svg.ImageConvertOptions;
import com.acrocontext.image.svg.ImageSvgConverter;
import com.acrocontext.reactive.domain.SvgConvertRequestView;
import com.acrocontext.reactive.domain.SvgConvertRespondView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * Date 12/24/17
 *
 * @author Nam Seob Seo
 */

@RestController
@RequestMapping("/api/v1/svg")
public class SvgConverterController {
    private final ImageSvgConverter imageSvgConverter;
    private final ApplicationSettingsService applicationSettingsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SvgConverterController(ImageSvgConverter imageSvgConverter,
                                  ApplicationSettingsService applicationSettingsService,
                                  ObjectMapper objectMapper) {
        this.imageSvgConverter = imageSvgConverter;
        this.applicationSettingsService = applicationSettingsService;
        this.objectMapper = objectMapper;

    }


    private SvgConvertRespondView convertRespondView(SvgConvertRequestView convertRequestView) {
        byte[] imageData = createBytesFromBase64(convertRequestView.getImageDataBase64());
        try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            ApplicationSettings applicationSettings = applicationSettingsService.getApplicationSettingsInCache();
            ApplicationSettings.SvgImageGenerationSettings settings = applicationSettings.getSvgImageGenerationSettings();

            int imageSize = bufferedImage.getData().getDataBuffer().getSize();
            if (settings.isUseLimitation()) {
                if (imageSize > settings.getMaxSupportedImageSize()) {
                    throw new SvgImageGenerationError("Not supported image size");
                }

                if (convertRequestView.getNumberOfColors() > settings.getMaxNumberOfColors()) {
                    throw new SvgImageGenerationError("Not supported number of colors");
                }
            }

            ImageConvertOptions options = ImageConvertOptions
                    .builder()
                    .numberOfColors(convertRequestView.getNumberOfColors())
                    .colorSampling(true)
                    .build();

            String svgString = imageSvgConverter.convertImageToSVG(bufferedImage, options, ((description, current, total, duration) ->
                    System.out.println(String.format("%s, %d/%d, %s", description, current, total, duration))));

            return SvgConvertRespondView.builder()
                    .filename(convertRequestView.getImageFilename())
                    .svgString(svgString)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T toRequestBody(InputStream inputStream, Class<T> classValue) {
        try {
            return objectMapper.readValue(inputStream, classValue);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static class InputStreamCollector {
        private InputStream inputStream;

        public void collectInputStream(InputStream inputStream) {
            if (this.inputStream == null) {
                this.inputStream = inputStream;
            }

            this.inputStream = new SequenceInputStream(this.inputStream, inputStream);
        }

        public InputStream getInputStream() {
            return this.inputStream;
        }
    }

    @PutMapping(path = "/conversion",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SvgConvertRespondView> convertImage(ServerWebExchange exchange) {
        return exchange.getRequest().getBody()
                .collect(InputStreamCollector::new, (inputStreamCollector, dataBuffer)->
                        inputStreamCollector.collectInputStream(dataBuffer.asInputStream()))
                .map(InputStreamCollector::getInputStream)
                .map(inputStream -> toRequestBody(inputStream, SvgConvertRequestView.class))
                .map(this::convertRespondView);
    }

    private static byte[] createBytesFromBase64(String data) {
        String base64Image = data.split(",")[1];
        return javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
    }

}
