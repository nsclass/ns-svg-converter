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
package com.acrocontext.cassandra.config;

import com.acrocontext.cassandra.dao.CommonDataRepository;
import com.acrocontext.cassandra.domain.CommonData;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DataCenterReplication;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
import org.springframework.lang.NonNull;

@Configuration
@EnableConfigurationProperties
@EnableReactiveCassandraRepositories(basePackageClasses = CommonDataRepository.class)
@Profile({"dao_cassandra", "default"})
public class NSCassandraConfiguration extends AbstractReactiveCassandraConfiguration {

  @Value("${cassandra.keyspace}")
  private String keyspace;

  @Value("${cassandra.dataCenter}")
  private String dataCenter;

  @Value("${cassandra.replicateFactor}")
  private int replicateFactor = 1;

  @Override
  @NonNull protected String getKeyspaceName() {
    return keyspace;
  }

  @Override
  @NonNull public SchemaAction getSchemaAction() {
    return SchemaAction.CREATE_IF_NOT_EXISTS;
  }

  @Override
  @NonNull public String[] getEntityBasePackages() {
    return new String[] {CommonData.class.getPackage().getName()};
  }

  @Override
  @NonNull protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {

    CreateKeyspaceSpecification defaultKeySpace =
        CreateKeyspaceSpecification.createKeyspace(keyspace)
            .ifNotExists()
            .withNetworkReplication(DataCenterReplication.of(dataCenter, replicateFactor));

    return Collections.singletonList(defaultKeySpace);
  }
}
