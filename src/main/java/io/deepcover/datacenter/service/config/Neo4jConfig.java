/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepcover.datacenter.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * Neo4j配置类 - 使用Spring Boot自动配置
 * Neo4j 4.x版本可以完全依赖自动配置
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "io.deepcover.datacenter.dal.dao.repository")
public class Neo4jConfig {
    // Spring Boot会自动配置：
    // 1. Driver bean (基于application.properties中的配置)
    // 2. Neo4jTransactionManager
    // 3. Neo4jTemplate等其他必要的bean
}