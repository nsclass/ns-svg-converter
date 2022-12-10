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
import org.springframework.data.util.Pair;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Date 12/23/17
 *
 * @author Nam Seob Seo
 */
@Slf4j
public class ParallelOperationUtils {
  public static void executeTasks(List<Supplier<Integer>> tasks) {
    CompletableFuture<?>[] list = new CompletableFuture[tasks.size()];
    for (int idx = 0; idx < tasks.size(); ++idx) {
      list[idx] = CompletableFuture.supplyAsync(tasks.get(idx));
    }

    CompletableFuture.allOf(list).join();
  }

  public record ExecuteItem<T>(int idx, T item) {}

  public static <T> T[] execute(T[] array, List<Supplier<ExecuteItem<T>>> tasks) {
    List<CompletableFuture<ExecuteItem<T>>> list = tasks.stream()
            .map(CompletableFuture::supplyAsync)
            .toList();

    var computableList = list.toArray(new CompletableFuture[0]);
    CompletableFuture.allOf(computableList);

    var results = list.stream()
            .map(CompletableFuture::join)
            .sorted(Comparator.comparingInt(x -> x.idx))
            .map(item -> item.item)
            .toList();

    return results.toArray(array);
  }
}
