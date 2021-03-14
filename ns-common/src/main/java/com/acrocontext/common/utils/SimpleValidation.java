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

package com.acrocontext.common.utils;

import com.acrocontext.exceptions.BeanValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Date 12/5/17
 *
 * @author Nam Seob Seo
 */
public class SimpleValidation {

  public static <T> BeanValidationException validate(Validator validator, T object) {

    Set<ConstraintViolation<T>> validatorSet = validator.validate(object);

    if (!validatorSet.isEmpty()) {
      StringBuilder errorMsg = new StringBuilder();
      validatorSet.forEach(
          cv -> {
            errorMsg.append(cv.getPropertyPath() + " - " + cv.getMessage() + "\n");
          });

      return new BeanValidationException(errorMsg.toString());
    }

    return null;
  }
}
