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

import com.alibaba.fastjson.JSON;
import io.deepcover.datacenter.dal.dao.entity.SceneBranchEntity;
import io.deepcover.datacenter.dal.dao.repository.SceneBranchRepository;
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class SceneBranchService {

    @Autowired
    Neo4jUtil session;
    @Autowired
    SceneBranchRepository sceneBranchRepository;
//    @Autowired
//    SceneModelService sceneModelService;

    public BusinessResult insertSceneBranch(SceneBranchEntity sceneBranchEntity) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            //查询该场景是否已经关联分支
            String sql = "MATCH (n:SceneBranchModel) where n.sceneId=$sceneId and n.branch=$branch return id(n)";
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("sceneId", sceneBranchEntity.getSceneId());
            queryParams.put("branch", sceneBranchEntity.getBranch());
            List<Map<String, Object>> resultList = session.executeSql(sql, queryParams, true);
            if (resultList.size() == 0) {
                //如果没有关联，则关联
                session.save(sceneBranchRepository, sceneBranchEntity);
                //查询关联的分支Id
                String sqlScene = "MATCH (n:SceneModel) where id(n)=$sceneId return n.branchIds";
                Map<String, Object> sceneParams = new HashMap<>();
                sceneParams.put("sceneId", sceneBranchEntity.getSceneId());
                List<Map<String, Object>> result = session.executeSql(sqlScene, sceneParams, true);
//                Result resultScene = session.executeSql(sqlScene, new HashMap<>(), true);
//                List queryResult = (ArrayList) resultScene.queryResults();
                if (result.size() > 0) {
                    HashMap<String, Object> map = (HashMap<String, Object>) result.get(0);
                    String branchIds = (String) map.get("n.branchIds");
                    if (StringUtils.isBlank(branchIds)) {
                        branchIds = sceneBranchEntity.getId() + "";
                    } else {
                        branchIds = branchIds + "," + sceneBranchEntity.getId();
                    }
                    //更新场景关联的分支
                    Map<String, Object> sceneBranchIds = new HashMap<>();
                    sceneBranchIds.put("id", sceneBranchEntity.getSceneId());
                    sceneBranchIds.put("branchIds", branchIds);
                    setSceneModel(sceneBranchIds);
                }
            }
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("insertSceneBranch异常:" + "异常msg：" + mgs);
            businessResult = new BusinessResult(false);
            log.error("操作异常", ex);
            businessResult.setMessage(ex.getMessage());
        }
        return businessResult;
    }
    //升级springboot后，不能循环依赖，移除对SceneModelService的直接依赖，直接在本类新增接口setSceneModel
    public void setSceneModel(Map<String, Object> params) {
            StringBuffer stringBufferquery = new StringBuffer();
            stringBufferquery.append("MATCH (n:SceneModel) where id(n) = $id Set");
            params.forEach((key, value) -> {
                if (!"id".equals(key)) {
                    stringBufferquery.append(" n." + key + "=$" + key + ",");
                }
            });
            stringBufferquery.delete(stringBufferquery.length() - 1, stringBufferquery.length());
            stringBufferquery.append(" return id(n)");
            session.executeSql(stringBufferquery.toString(), params, false);

    }
    /**
     * 根据分支查询场景id
     *
     * @param branch
     * @return
     */
    public List<Long> getSceneIdsByBranch(String branch) {

        String sql = "MATCH (n:SceneBranchModel) where n.branch=$branch return distinct n.sceneId";
        Map<String, String> branchParams = new HashMap<>();
        branchParams.put("branch", branch);
        List<Map<String, Object>> queryEndResult = session.executeSqlV2(sql, branchParams, true);
        Iterable<Map<String, Object>> maps = queryEndResult;
        List<Long> sceneIds = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            sceneIds.add(Long.valueOf(map.get("n.sceneId").toString()));
        }
        return sceneIds;
    }

    /**
     * 查询所有分支
     *
     * @return
     */
    public BusinessResult querySceneBranch() {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            String sql = "MATCH (n:SceneBranchModel) RETURN distinct n.branch as branch";
            List<Map<String, Object>> result = session.executeSql(sql, new HashMap<>(), true);
            List<SceneBranchEntity> branchEntities = JSON.parseArray(JSON.toJSONString(result), SceneBranchEntity.class);
            businessResult.setData(branchEntities);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("querySceneBranch:" + "异常msg：" + mgs);
            businessResult = new BusinessResult(false);
            log.error("操作异常", ex);
            businessResult.setMessage(ex.getMessage());
        }
        return businessResult;
    }
}
