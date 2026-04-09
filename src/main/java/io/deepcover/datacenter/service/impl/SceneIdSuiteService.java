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

import io.deepcover.datacenter.dal.dao.entity.SceneIdSuiteIdEntity;
import io.deepcover.datacenter.dal.dao.repository.SceneIdSuiteIdModelRepository;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class SceneIdSuiteService {

    @Autowired
    Neo4jUtil session;
    @Autowired
    SceneIdSuiteIdModelRepository sceneIdSuiteIdModelRepository;

    public BusinessResult insertSceneSuiteId(SceneIdSuiteIdEntity sceneIdSuiteIdEntity) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            session.save(sceneIdSuiteIdModelRepository, sceneIdSuiteIdEntity);

        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("insertSceneBranch异常:" + "异常msg：" + mgs);
            businessResult = new BusinessResult(false);
            log.error("操作异常", ex);
            businessResult.setMessage(ex.getMessage());
        }
        return businessResult;
    }

    public BusinessResult searchSuiteId(SceneIdSuiteIdEntity sceneIdSuiteIdEntity) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            String sql = "MATCH (n:SceneIdSuiteIdModel) where n.sceneId=$sceneId return n.suiteId,n.suiteName";
            Map<String, Object> params = new HashMap<>();
            params.put("sceneId", sceneIdSuiteIdEntity.getSceneId());
            List<Map<String, Object>> resultList = session.executeSql(sql, params, true);
            List<Map> suiteIdList = new ArrayList<>();
            int size = resultList.size();
            if (size == 0) {
                return businessResult;
            }
            for (int i = 0; i < size; i++) {
                HashMap<String, Object> map = (HashMap<String, Object>) resultList.get(i);
                Map<String, Object> data = new HashMap<>();
                data.put("suiteId", map.get("n.suiteId"));
                data.put("suiteName", map.get("n.suiteName"));
//                Long suiteId = map.get("n.suiteId");
                suiteIdList.add(data);
            }
            businessResult.setData(suiteIdList);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("insertSceneBranch异常:" + "异常msg：" + mgs);
            businessResult = new BusinessResult(false);
            log.error("操作异常", ex);
            businessResult.setMessage(ex.getMessage());
        }
        return businessResult;
    }

    public BusinessResult searchSuiteByIds(Map<String, Object> params) {
        BusinessResult businessResult = new BusinessResult(true);
        String sql = "MATCH (n:SceneIdSuiteIdModel) where n.sceneId in $id" + " return n.sceneId as sceneId, n.suiteId as suiteId,n.suiteName as suiteName";
        List<Map<String, Object>> result = session.executeSql(sql, params, true);
        businessResult.setData(result);
        return businessResult;
    }
}
