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

package io.deepcover.datacenter.dal.dao.mapper;

import io.deepcover.datacenter.dal.dao.mysqlentity.AgentService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2023-07-26
 */
public interface AgentServiceDAO extends JpaRepository<AgentService, Long> {

    @Query(value = "SELECT LOCAL FROM agent_service WHERE service_name= ?1 ", nativeQuery = true)
    int getLocalByServiceName(String serviceName);

    @Query(value = "SELECT DISTINCT service_name FROM agent_service", nativeQuery = true)
    List<String> getAllServiceName();
}
