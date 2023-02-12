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
package com.acrocontext.reactive;

import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import com.acrocontext.NSDomainSpringConfiguration;
import com.acrocontext.cassandra.NSCassandraSpringConfiguration;
import com.acrocontext.cassandra.config.NSCassandraConfiguration;
import com.acrocontext.common.NSCommonSpringConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Import({
  NSDomainSpringConfiguration.class,
  NSCommonSpringConfiguration.class,
  NSCassandraSpringConfiguration.class,
  NSCassandraConfiguration.class
})
@ComponentScan(basePackageClasses = ApplicationSpringConfiguration.class)
@EnableWebFlux
public class ApplicationSpringConfiguration {

  @Value("classpath:/static/index.html")
  private Resource indexHtml;

  @Bean
  RouterFunction<?> routerFunction() {
    RouterFunction router =
        RouterFunctions.resources("/**", new ClassPathResource("static/"))
            // workaround solution for forwarding / to /index.html
            .andRoute(
                GET("/"),
                request -> {
                  return ServerResponse.ok().contentType(TEXT_HTML).bodyValue(indexHtml);
                });
    return router;
  }
}
