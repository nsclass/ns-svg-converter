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

package com.acrocontext.image.svg.utils;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Date 12/23/17
 *
 * @author Nam Seob Seo
 */
public class OperationManager<T> {

  private final OperationProgressListener progressListener;

  @Value
  @AllArgsConstructor
  private static class OperationCommand<T> {
    private String description;
    private Function<T, T> function;
  }

  public OperationManager(OperationProgressListener listener) {
    this.progressListener = listener;
  }

  private List<OperationCommand<T>> operationCommandList = new ArrayList<>();

  public OperationManager<T> addOperation(String description, Function<T, T> operation) {
    operationCommandList.add(new OperationCommand<>(description, operation));
    return this;
  }

  public T execute(T context) {
    int idx = 0;
    int totalCount = operationCommandList.size();
    for (OperationCommand<T> operationCommand : operationCommandList) {
      Instant start = Instant.now();

      operationCommand.function.apply(context);
      Instant end = Instant.now();
      progressListener.onProgressInfo(
          "Done: " + operationCommand.getDescription(),
          idx,
          totalCount,
          Duration.between(start, end));

      ++idx;
    }

    return context;
  }
}
