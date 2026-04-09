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

package io.deepcover.datacenter.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.deepcover.datacenter.dal.dao.entity.SceneTraceIdEntity;
import io.deepcover.datacenter.dal.dao.mapper.UserLogDAO;
import io.deepcover.datacenter.dal.dao.repository.SceneTraceIdModelRepository;
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.common.ResultEnum;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;


import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
public class SceneTraceIdModelService {
    // 添加缓存
    private static final Cache<String, Boolean> traceIdCache = Caffeine.newBuilder()
        .maximumSize(10_000) // 限制最大缓存数量
        .expireAfterWrite(2, TimeUnit.MINUTES) // 写入后2分钟过期
        .recordStats() // 记录统计信息
        .build();
    @Autowired
    Neo4jUtil session;
    @Autowired
    SceneTraceIdModelRepository sceneTraceIdModelRepository;
    @Autowired
    SceneModelService sceneModelService;

//    @Autowired
//    UserLogDAO userLogDAO;
    @Value("${traceIds}")
    String traceIds;

    @Transactional
    public BusinessResult insertSceneTraceIdModel(SceneTraceIdEntity sceneTraceIdEntity) {
        BusinessResult businessResult = new BusinessResult(true);
        try {
            // ✅ 使用MERGE语句 - 原子操作，避免并发重复
            String sql = "MERGE (n:SceneTraceIdModel {sceneId: $sceneId, traceId: $traceId}) " +
                        "ON CREATE SET n.isExist = $isExist " +
                        "RETURN id(n)";
            
            Map<String, Object> params = new HashMap<>();
            params.put("sceneId", sceneTraceIdEntity.getSceneId());
            params.put("traceId", sceneTraceIdEntity.getTraceId());
            params.put("isExist", sceneTraceIdEntity.getIsExist());
            
            List<Map<String, Object>> resultList = session.executeSql(sql, params, false);
            
            // 更新缓存
            if (resultList.size() > 0) {
                traceIdCache.put(sceneTraceIdEntity.getTraceId(), true);
            }
            
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("insertSceneTraceIdModel异常msg:" + "异常msg：" + mgs);
            businessResult = new BusinessResult(false);
            log.error("操作异常", ex);
            businessResult.setMessage(ex.getMessage());
        }
        return businessResult;
    }

    /**
     * 校验traceId是否存在
     *
     * @param traceId
     * @return
     */
    public BusinessResult checkTraceId(String traceId) {
        BusinessResult businessResult = new BusinessResult(true);

        // 使用Caffeine缓存
        Boolean cached = traceIdCache.getIfPresent(traceId);
        if (cached != null) {
            businessResult.setData(Arrays.asList(cached));
            return businessResult;
        }
        
        //排除黑名单，无效分析
        if (StringUtils.isNotEmpty(traceIds)) {
            List<String> blackTraceIds = Arrays.asList(StringUtils.split(traceIds, ","));
            if (blackTraceIds.contains(traceId)) {
                businessResult.setData(Arrays.asList(false));
                return businessResult;
            }
        }
        businessResult.setData(Arrays.asList(true));
        try {
            // 检查缓存后执行原有逻辑
            String sql = "MATCH (n:SceneTraceIdModel) where n.traceId=$traceId return id(n),n.sceneId,n.isExist";
            Map<String, Object> params = new HashMap<>();
            params.put("traceId", traceId);
            List<Map<String, Object>> resultList = session.executeSql(sql, params, true);
            
            boolean exists = resultList.size() > 0;
            // 查询后更新缓存
            traceIdCache.put(traceId, exists);
            return businessResult;
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("根据traceId查询SceneTraceIdModel异常，traceId=" + traceId + "异常msg=" + mgs);
            log.error("操作异常", ex);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getCode());
            businessResult.setMessage(mgs);
            businessResult.setData(Arrays.asList(false));
        }
        return businessResult;
    }

    public BusinessResult getTraceId(String sceneId) {
        BusinessResult businessResult = new BusinessResult(true);
        businessResult.setData(null);
        try {
            String sql = "MATCH (n:SceneTraceIdModel) where n.sceneId=$sceneId return n.traceId";
            Map<String, Object> params = new HashMap<>();
            params.put("sceneId", sceneId);
            List<Map<String, Object>> resultList = session.executeSql(sql, params, true);
            List<String> traceIdList = new ArrayList<>();
            int size = resultList.size();
            if (size == 0) {
                return businessResult;
            }
            for (int i = 0; i < size; i++) {
                HashMap<String, Object> map = (HashMap<String, Object>) resultList.get(i);
                String traceId = (String) map.get("n.traceId");
                traceIdList.add(traceId);
            }
            businessResult.setData(traceIdList);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("根据sceneId查询SceneTraceIdModel异常，sceneId:" + sceneId + "异常msg：" + mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getCode());
            businessResult.setMessage(mgs);
            businessResult.setData(Arrays.asList(false));
        }
        return businessResult;
    }
}
