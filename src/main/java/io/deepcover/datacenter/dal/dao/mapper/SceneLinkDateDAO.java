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

import io.deepcover.datacenter.dal.dao.mysqlentity.SceneLinkDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2023-07-26
 */
public interface SceneLinkDateDAO extends JpaRepository<SceneLinkDate, Long> {

    List<SceneLinkDate> findByHashCodeAndDate(String hashCode, String date);

    @Query(value = "SELECT SUM(a.heat) as heat,a.sceneId as sceneId FROM ( SELECT SUM(heat) AS heat ,MAX(scene_id) AS sceneId FROM scene_link_date   WHERE date in ?1 and  hash_code in ?2 GROUP  BY hash_code) AS a GROUP BY a.sceneId ORDER BY heat DESC", nativeQuery = true)
    List<Map<String, Object>> findModel(List<String> dates, List<String> hashCodes);
}
