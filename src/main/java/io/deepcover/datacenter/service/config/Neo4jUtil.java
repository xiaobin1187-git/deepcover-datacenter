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

import io.deepcover.datacenter.dal.dao.entity.SceneNodeModelEntity;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: huangtai
 * @Description:
 * @Date: 2023-7-17 18:11
 */
@Component
@Log4j2
public class Neo4jUtil {

    @Autowired
    private Driver driver;

    @Value("${node.size}")
    int nodeSize;
    // 添加节点类型判断
    private static final org.neo4j.driver.types.Type NODE_TYPE = InternalTypeSystem.TYPE_SYSTEM.NODE();
    public List<Map<String, Object>> executeSql(String sql, Map<String, Object> params, boolean readOnly) {
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        try (Session session = driver.session(SessionConfig.builder()
                .withDefaultAccessMode(readOnly ? AccessMode.READ : AccessMode.WRITE)
                .build())) {

            List<Record> records = session.run(sql, params).list();
            for (Record record : records) {
                Map<String, Object> row = new HashMap<>();
                record.keys().forEach(key -> {
                    org.neo4j.driver.Value value = record.get(key);
                    // 保留原始节点对象
                    if (value.hasType(NODE_TYPE)) {
                        row.put(key, value.asNode());
                    } else {
                        row.put(key, value.asObject());
                    }
                });
                resultList.add(row);
            }

            log.info("sql:" + sql + ",执行时间:" + (System.currentTimeMillis() - startTime));
        }
        return resultList;
    }

    public List<Map<String, Object>> executeSqlV2(String sql, Map<String, String> params, boolean readOnly) {
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        try (Session session = driver.session(SessionConfig.builder()
                .withDefaultAccessMode(readOnly ? AccessMode.READ : AccessMode.WRITE)
                .build())) {
                
            // 参数转换逻辑保持不变
            Map<String, Object> convertedParams = new HashMap<>();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (entry.getValue() != null) {
                        try {
                            if (entry.getValue().contains(".")) {
                                convertedParams.put(entry.getKey(), Double.parseDouble(entry.getValue()));
                            } else {
                                convertedParams.put(entry.getKey(), Long.parseLong(entry.getValue()));
                            }
                        } catch (NumberFormatException e) {
                            convertedParams.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }

            List<Record> records = session.run(sql, convertedParams).list();
            for (Record record : records) {
                Map<String, Object> row = new HashMap<>();
                record.keys().forEach(key -> {
                    org.neo4j.driver.Value value = record.get(key);
                    // 保留原始节点对象
                    if (value.hasType(NODE_TYPE)) {
                        row.put(key, value.asNode());
                    } else {
                        row.put(key, value.asObject());
                    }
                });
                resultList.add(row);
            }

            log.info("sql:" + sql + ",执行时间:" + (System.currentTimeMillis() - startTime));
        }
        return resultList;
    }

    public void save(Neo4jRepository neo4jRepository, Object object) {
        long startTime = System.currentTimeMillis();
        neo4jRepository.save(object);
        log.info("sql:" + object.toString() + ",执行时间:" + (System.currentTimeMillis() - startTime));
    }

    public void saveSceneNode(Neo4jRepository neo4jRepository, List<SceneNodeModelEntity> objects) {
        long startTime = System.currentTimeMillis();
        neo4jRepository.saveAll(objects);
        log.info("sql:" + objects.get(0).toString() + ",执行时间:" + (System.currentTimeMillis() - startTime));
    }

    public void saveAll(Neo4jRepository neo4jRepository, List objects) {
        int page = 0;
        while (page < objects.size()) {
            List pageNodeList = new ArrayList();
            if (page + nodeSize > objects.size()) {
                pageNodeList = objects.subList(page, objects.size());
            } else {
                pageNodeList = objects.subList(page, page + nodeSize);
            }
            long startTime = System.currentTimeMillis();
            neo4jRepository.saveAll(pageNodeList);
            log.info("sql:" + pageNodeList.get(0).toString() + ",执行时间:" + (System.currentTimeMillis() - startTime));
            page += nodeSize;
        }
    }

    public void deleteByPage(String str, String modelName, String returnParams) {
        int page = 0;
        str = str.substring(1, str.length() - 1);
        List<String> idList = new ArrayList<>();
        Collections.addAll(idList, StringUtils.split(str, ","));
        while (page < idList.size()) {
            List<String> pageIdList = new ArrayList<>();
            if (page + nodeSize > idList.size()) {
                pageIdList = idList.subList(page, idList.size());
            } else {
                pageIdList = idList.subList(page, page + nodeSize);
            }
            String sql = "MATCH (n:" + modelName + ") where id(n) in " + pageIdList + returnParams;
            executeSql(sql, new HashMap<>(), false);
            page += nodeSize;
        }
    }

    public void query(String sql, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        try (Session session = driver.session(SessionConfig.builder()
                .withDefaultAccessMode(AccessMode.WRITE)
                .build())) {
            session.run(sql, params);
            log.info("sql:" + sql + ",执行时间:" + (System.currentTimeMillis() - startTime));
        }
    }
}
