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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Date 12/23/17
 *
 * @author Nam Seob Seo
 */
@Slf4j
public class ParallelOperationUtils {
  public static void executeTasks(List<Supplier<Integer>> tasks) {

    CompletableFuture[] list = new CompletableFuture[tasks.size()];
    for (int idx = 0; idx < tasks.size(); ++idx) {
      list[idx] = CompletableFuture.supplyAsync(tasks.get(idx));
    }

    CompletableFuture.allOf(list).join();
  }

  public static <T> T[] execute(Class<T> c, List<Supplier<Pair<Integer, T>>> tasks) {

    CompletableFuture<Pair<Integer, T>>[] list = new CompletableFuture[tasks.size()];
    for (int idx = 0; idx < tasks.size(); ++idx) {
      list[idx] = CompletableFuture.supplyAsync(tasks.get(idx));
    }

    CompletableFuture.allOf(list);

    @SuppressWarnings(value = "unchecked")
    final T[] results = (T[]) Array.newInstance(c, tasks.size());
    Arrays.stream(list)
        .map(CompletableFuture::join)
        .forEach(item -> results[item.getKey()] = item.getValue());

    return results;
  }
}
