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

import com.acrocontext.domain.Product;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

  @PreAuthorize("hasRole('MEMBER')")
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<Product> getAllProduct() {
    return Flux.just(new Product("test", "test", "test"));
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<Product> getProductList() {
    return Flux.just(new Product("test1", "test1", "test1"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<Product> getProductListForAdmin() {
    return Flux.just(new Product("test_admin", "test_admin", "test_admin"));
  }
}
