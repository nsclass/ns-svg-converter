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

package com.acrocontext.reactive.error;

import com.acrocontext.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
public class DefaultReactiveExceptionHandler extends AbstractErrorWebExceptionHandler {

    public DefaultReactiveExceptionHandler(ServerCodecConfigurer serverCodecConfigurer,
                                           ErrorAttributes errorAttributes,
                                           ResourceProperties resourceProperties,
                                           ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, applicationContext);

        super.setMessageReaders(serverCodecConfigurer.getReaders());
        super.setMessageWriters(serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {

        return RouterFunctions
                .route(this::isUserRegistrationException, this::registrationErrorResponse)
                .andRoute(this::isAccessDeniedException, this::accessDeniedErrorResponse)
                .andRoute(this::isBeanValidationException, this::beanValidationErrorResponse)
                .andRoute(this::isTokenSecurityException, this::tokenSecurityErrorResponse)
                .andRoute(this::isSvgConversionException, this::svgConversionErrorResponse)
                .andRoute(this::isChangePasswordException, this::changePasswordErrorResponse);
    }

    private Mono<ServerResponse> svgConversionErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));

        HttpStatus errorStatus = HttpStatus.BAD_REQUEST;
        return ServerResponse.status(errorStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error)
                .doOnNext(resp -> logError(request, errorStatus));
    }

    private boolean isSvgConversionException(ServerRequest request) {
        Throwable exception = getError(request);
        if (exception instanceof CompletionException) {
            CompletionException ex = (CompletionException) exception;
            exception = ex.getCause();
        }

        if (exception instanceof SvgImageGenerationError) {
            return true;
        }

        return false;
    }

    private boolean isBeanValidationException(ServerRequest request) {
        Throwable exception = getError(request);
        if (exception instanceof BeanValidationException) {
            return true;
        }

        return false;
    }

    private Mono<ServerResponse> beanValidationErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));

        HttpStatus errorStatus = HttpStatus.BAD_REQUEST;
        return ServerResponse.status(errorStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error)
                .doOnNext(resp -> logError(request, errorStatus));
    }


    private boolean isAccessDeniedException(ServerRequest request) {
        Throwable exception = getError(request);
        if (exception instanceof AccessDeniedException) {
            return true;
        }

        return false;
    }

    private Mono<ServerResponse> accessDeniedErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));

        HttpStatus errorStatus = HttpStatus.FORBIDDEN;
        return ServerResponse.status(errorStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error)
                .doOnNext(resp -> logError(request, errorStatus));
    }

    private boolean isTokenSecurityException(ServerRequest request) {
        Throwable exception = getError(request);
        if (exception instanceof TokenAuthExceptionBase) {
            return true;
        }

        return false;
    }

    private Mono<ServerResponse> tokenSecurityErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));

        HttpStatus errorStatus = HttpStatus.FORBIDDEN;
        return ServerResponse.status(errorStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error)
                .doOnNext(resp -> logError(request, errorStatus));
    }


    private boolean isUserRegistrationException(ServerRequest request) {
        Throwable exception = getError(request);
        if (exception instanceof UserRegistrationExceptionBase) {
            return true;
        }

        return false;

    }

    private Mono<ServerResponse> registrationErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));

        HttpStatus errorStatus = HttpStatus.METHOD_NOT_ALLOWED;
        return ServerResponse.status(errorStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error)
                .doOnNext(resp -> logError(request, errorStatus));
    }

    private boolean isChangePasswordException(ServerRequest serverRequest) {
        Throwable exception = getError(serverRequest);
        if (exception instanceof ChangePasswordExceptionBase) {
            return true;
        }

        return false;
    }

    private Mono<ServerResponse> changePasswordErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));

        HttpStatus errorStatus = HttpStatus.FORBIDDEN;
        return ServerResponse.status(errorStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error)
                .doOnNext(resp -> logError(request, errorStatus));
    }

    private void logError(ServerRequest request, HttpStatus errorStatus) {
        Throwable ex = getError(request);
        log.error("Exception [" + request.methodName() + " "
                + request.uri() + "]", ex);
    }
}
