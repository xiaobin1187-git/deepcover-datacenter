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

import com.alibaba.fastjson.JSONArray;
import io.deepcover.datacenter.dal.dao.entity.SceneNodeModelEntity;
import io.deepcover.datacenter.dal.dao.repository.SceneModelNodeRepository;
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.SceneNodeModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.service.scheduler.AresDatecenterAsync;
import io.deepcover.datacenter.common.ResultEnum;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("SceneNodeModelService")
@Log4j2
public class SceneNodeModelServiceImpl implements SceneNodeModelService {

    @Autowired
    SceneModelNodeRepository sceneModelNodeRepository;

    @Autowired
    Neo4jUtil session;

    @Autowired
    AresDatecenterAsync datecenterAsync;

    @Autowired
    SceneModelService sceneModelService;

    @Value("${node.size}")
    private int nodeSize;

    @Override
    public BusinessResult addModelNode(List<SceneNodeModelEntity> nodeList) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            int page = 0;
            SceneNodeModelEntity firstNode = nodeList.get(0);
            long sceneId = firstNode.getSceneId();
            while (page < nodeList.size()) {
                List<SceneNodeModelEntity> pageNodeList = new ArrayList();
                if (page + nodeSize > nodeList.size()) {
                    pageNodeList = nodeList.subList(page, nodeList.size());
                } else {
                    pageNodeList = nodeList.subList(page, page + nodeSize);
                }
                pageNodeList.forEach(node -> {
                    node.setSceneId(sceneId);
                });
                session.saveSceneNode(sceneModelNodeRepository, pageNodeList);
                page += nodeSize;
            }
            List<Long> nodeIds = nodeList.stream().map(SceneNodeModelEntity::getId).collect(Collectors.toList());
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("id", sceneId);
            updateMap.put("nodeIds", nodeIds.toString());
            sceneModelService.setSceneModel(updateMap);
            datecenterAsync.addNodeRelation(nodeList);
            datecenterAsync.setDepth(nodeList);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("插入异常{}", ex + "---" + nodeList.size(), ",异常msg：", mgs);
            businessResult.setMessage(mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
        }
        return businessResult;
    }

    @Override
    public BusinessResult findSceneModelNode(Map<String, Object> params) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            Map<String, Object> sceneId = new HashMap<>();
            sceneId.put("id", params.get("id"));
            BusinessResult sceneResult = sceneModelService.findSceneModel(sceneId, "n.nodeIds");
            ArrayList returnData = (ArrayList) sceneResult.getData();
            if (returnData.size() > 0) {
                String sceneNodeIds = ((HashMap) returnData.get(0)).get("n.nodeIds").toString();
                if (!"[]".equals(sceneNodeIds)) {
                    String sql = "MATCH (n:SceneNodeModel) where id(n) in $ids return n";
                    Map<String, Object> queryParams = new HashMap<>();
                    queryParams.put("ids", parseIdList(sceneNodeIds));
                    List<Map<String, Object>> resultList = session.executeSql(sql, queryParams, true);
//                    Iterable<Map<String, Object>> maps = result.queryResults();
                    ArrayList returnList = new ArrayList<>();
                    for (Map<String, Object> map : resultList) {
                        Object nodeObj = map.get("n");
                        if (nodeObj instanceof org.neo4j.driver.types.Node) {
                            org.neo4j.driver.types.Node node = (org.neo4j.driver.types.Node) nodeObj;
                            SceneNodeModelEntity sceneNodeModelEntity = new SceneNodeModelEntity();
                            // 使用工具类将Node属性复制到实体对象
                            node.asMap().forEach((k, v) -> {
                                try {
                                    if ("lineNums".equals(k) && v != null) {
                                        sceneNodeModelEntity.setLineNums(JSONArray.parseArray(v.toString()));
                                    } else if ("parameters".equals(k)&& v != null) {
                                        sceneNodeModelEntity.setParameters(JSONArray.parseArray(v.toString()));
                                    }  else {
                                        BeanUtils.setProperty(sceneNodeModelEntity, k, v);
                                    }
                                } catch (Exception e) {
                                    log.error("Failed to set property {} to SceneModelEntity", k);
                                }
                            });

                            returnList.add(sceneNodeModelEntity);
                        }
                    }
                    businessResult.setData(returnList);
                }
            }
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("查询异常{}", ex + "---" + params.toString(), ",异常msg：", mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
            businessResult.setMessage(mgs);
        }
        return businessResult;
    }

    /**
     * Parse a string like "[1, 2, 3]" into a List<Long>
     */
    private List<Long> parseIdList(String idStr) {
        List<Long> ids = new ArrayList<>();
        if (idStr == null || idStr.isEmpty() || "[]".equals(idStr)) {
            return ids;
        }
        String cleaned = idStr.replace("[", "").replace("]", "");
        for (String part : cleaned.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                ids.add(Long.parseLong(trimmed));
            }
        }
        return ids;
    }
}
