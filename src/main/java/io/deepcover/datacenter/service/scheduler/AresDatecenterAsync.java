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

package io.deepcover.datacenter.service.scheduler;

import io.deepcover.datacenter.dal.dao.entity.SceneNodeModelEntity;
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: huangtai
 * @Description: 异步执行代码块
 * @Date: 2022/11/4 18:51
 */
@Component
@Slf4j
public class AresDatecenterAsync {

    @Autowired
    private Neo4jUtil neo4jUtil;

    @Autowired
    SceneModelService sceneModelService;


    /**
     * 新增节点关系
     *
     * @param list
     */
    @Async("threadPoolTaskExecutor")
    public void addNodeRelation(List<SceneNodeModelEntity> list) {
        ArrayList nodeIds = new ArrayList();
        SceneNodeModelEntity firtNode = list.get(0);
        nodeIds.add(firtNode.getId());
        //linkId和ID的对应关系
        Map<String, Long> linkMapId = list.stream().collect(Collectors.toMap(SceneNodeModelEntity::getLinkId, SceneNodeModelEntity::getId));
        for (int node = list.size() - 1; node > 0; node--) {
            //当前节点id
            long id = list.get(node).getId();
            nodeIds.add(id);
            //当前节点对应父节点的列表
            String[] parentLinkIds = StringUtils.split(list.get(node).getParentLinkId(), ",");
            String[] beginTimes = StringUtils.split(list.get(node).getBeginTime(), ",");
            for (int index = 0; index < parentLinkIds.length; index++) {
                //循环创建关系
                long parentId = linkMapId.get(parentLinkIds[index]);
                String sql = "MATCH (a:SceneNodeModel),(b:SceneNodeModel) WHERE id(a)=$id AND id(b)=$parentId CREATE (b)-[:invoke{beginTime:$beginTime}]->(a)";
                Map<String, Object> params = new HashMap<>();
                params.put("id", id);
                params.put("parentId", parentId);
                params.put("beginTime", beginTimes[index]);
                try {
                    neo4jUtil.query(sql, params);
                } catch (Exception e) {
                    //只用于数据库显示关联关系，跟页面展示无关，有报错可以舍弃
                    log.warn("节点:" + id + "跟节点:" + parentId + "关联关系失败", e);
                }
            }
        }
    }

    @Async("threadPoolTaskExecutor")
    public void setDepth(List<SceneNodeModelEntity> list) {
        SceneNodeModelEntity firtNode = list.get(0);
        firtNode.setOrder(1);
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("id", firtNode.getSceneId());
        updateMap.put("depth", getDepth(list));
        sceneModelService.setSceneModel(updateMap);
    }


    /**
     * 获取深度
     *
     * @param list
     * @return
     */
    public int getDepth(List<SceneNodeModelEntity> list) {
        List<String> ids = new ArrayList<>();
        for (SceneNodeModelEntity temp : list) {
            ids.add(temp.getLinkId());
            getDepth(list, temp, ids);
        }
        List<SceneNodeModelEntity> listTemp = list.stream().sorted(Comparator.comparing(SceneNodeModelEntity::getOrder).reversed()).collect(Collectors.toList());
        return listTemp.get(0).getOrder();
    }

    /**
     * 获取深度
     *
     * @param list
     * @param sceneNodeModelEntity
     * @param linkIdsTemp
     */
    public void getDepth(List<SceneNodeModelEntity> list, SceneNodeModelEntity sceneNodeModelEntity, List<String> linkIdsTemp) {

        List<String> linkIds = new ArrayList<>();
        linkIds.addAll(linkIdsTemp);
        for (SceneNodeModelEntity nodeModelEntity : list) {
            if (nodeModelEntity.getParentLinkId().contains(sceneNodeModelEntity.getLinkId()) &&
                    nodeModelEntity.getLinkId() != sceneNodeModelEntity.getLinkId() &&
                    !linkIds.contains(nodeModelEntity.getLinkId())) {
                if (nodeModelEntity.getOrder() == null) {
                    nodeModelEntity.setOrder(sceneNodeModelEntity.getOrder() + 1);
                } else {
                    if ((sceneNodeModelEntity.getOrder() + 1 > nodeModelEntity.getOrder())) {
                        linkIds.add(sceneNodeModelEntity.getLinkId());
                        nodeModelEntity.setOrder(sceneNodeModelEntity.getOrder() + 1);
                        getDepth(list, nodeModelEntity, linkIds);
                    }
                }
            }
        }
    }
}
