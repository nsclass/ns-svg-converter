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
package com.acrocontext.reactive.services;

import com.acrocontext.common.utils.SimpleValidation;
import com.acrocontext.exceptions.BeanValidationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Date 12/5/17
 *
 * @author Nam Seob Seo
 */
@Service
public class BeanValidatorService {

  private final Validator validator;

  @Autowired
  public BeanValidatorService(Validator validator) {
    this.validator = validator;
  }

  public <T> BeanValidationException validate(T object) {
    return SimpleValidation.validate(validator, object);
  }
}
