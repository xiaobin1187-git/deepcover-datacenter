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

import io.deepcover.datacenter.dal.dao.entity.SceneModelEntity;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.service.utils.DateTimeUtil;
import org.springframework.stereotype.Component;
import io.deepcover.datacenter.common.BaseJobHandler;
import io.deepcover.datacenter.common.BaseJobHandler.ReturnT;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.util.*;

@Component("HandleDeleteDataJob")
@Log4j2
public class HandleDeleteDataJob extends BaseJobHandler {

    @Autowired
    Neo4jUtil session;
    @Value("${leave-day:}")
    int leaveDay;

    int num = 500;//每次查询数据的数量大小

    @Override
    public void execute() throws Exception {
        deleteDataTask(null);
    }

    public ReturnT<String> executeWithParam(String id) throws Exception {
        deleteDataTask(id);
        return ReturnT.SUCCESS;
    }


    @Async
    public void deleteDataTask(String id) {// 编写删除数据的逻辑
        String boundaryDate = DateTimeUtil.getYMDByDate(leaveDay);
        int skip = 0;
        if (StringUtils.isNotEmpty(id)) {
            skip = Integer.parseInt(id);
        }
        String dates = DateTimeUtil.getDaysArrayString(DateTimeUtil.getThisDayStart(leaveDay), new Date());
        int skipNum = deleteBatchData(skip, boundaryDate, dates);
        while (skipNum != Integer.MIN_VALUE) {
            System.out.println("跳过的个数:" + skipNum + "时间:" + new Date());
            skipNum = deleteBatchData(skipNum, boundaryDate, dates);
        }
        System.out.println("删除数据完毕");

    }

    private int deleteBatchData(int skipNum, String boundaryDate, String dates) {
        int newSkipNum = skipNum + num;
        String sql = "MATCH (n:SceneModel) where n.date<$boundaryDate WITH n.parentId as parentId ,id(n) as id SKIP  " + skipNum + " limit " + num + " RETURN DISTINCT parentId,id";
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("boundaryDate", boundaryDate);
            List<Map<String, Object>> parentIdList = session.executeSql(sql, params, true);

            if (parentIdList.size()>0) {
//                ArrayList<HashMap<String, Long>> parentIdList = (ArrayList) sceneResult.queryResults();

                ArrayList filterParentList = new ArrayList();
                ArrayList onlyDateList = new ArrayList();
                ArrayList screenList = new ArrayList();
                ArrayList screenDateList = new ArrayList();
                ArrayList sceneNodeIdsList = new ArrayList();
                ArrayList branchIdsList = new ArrayList();
                ArrayList analysisIdsList = new ArrayList();


                    for (Map<String, Object> parentIdMap : parentIdList) {
                        long parentId = (long) parentIdMap.get("parentId");
                        long id = (long) parentIdMap.get("id");
                        sql = "MATCH (n:SceneModel) where n.parentId=$parentId and n.date in $dateList RETURN id(n) limit 1";
                        Map<String, Object> checkParams = new HashMap<>();
                        checkParams.put("parentId", parentId);
                        checkParams.put("dateList", parseDateArrayString(dates));
                        List<Map<String, Object>> lastResult = session.executeSql(sql, checkParams, true);
//                        sql = "MATCH (n:SceneModel) where id(n)=" + parentId  + " RETURN id(n)";
//                        Result isExistScreenResult = session.executeSql(sql, new HashMap<>(), true);
//                        if (!lastResult.queryResults().iterator().hasNext()||!isExistScreenResult.queryResults().iterator().hasNext()) {//说明没有最近日期，删除,或者不存在场景id
                       if (lastResult.size()<=0) {//说明没有最近日期，删除
                            filterParentList.add(parentId);//添加对应父类的id，就是场景id
                            onlyDateList.add(id);//日期的id，主要为了有的日期过期父类id已经不存在但是没删除该日期
                        }
                    }
                    if (filterParentList.size() != 0) {
                        sql = "MATCH (n:SceneModel) where id(n) in $ids RETURN n";
                        Map<String, Object> filterParams = new HashMap<>();
                        filterParams.put("ids", filterParentList);
                        List<Map<String, Object>> maps = session.executeSql(sql, filterParams, true);
                        ArrayList<SceneModelEntity> sceneResultList = new ArrayList();

                        for (Map<String, Object> map : maps) {
                            Object nodeObj = map.get("n");
                            if (nodeObj instanceof org.neo4j.driver.types.Node) {
                                org.neo4j.driver.types.Node node = (org.neo4j.driver.types.Node) nodeObj;
                                SceneModelEntity sceneNodeEntity = new SceneModelEntity();
                                // 使用工具类将Node属性复制到实体对象
                                node.asMap().forEach((k, v) -> {
                                    try {
                                        BeanUtils.setProperty(sceneNodeEntity, k, v);
                                    } catch (Exception e) {
                                        log.warn("Failed to set property {} to SceneModelEntity", k);
                                    }
                                });
                                sceneResultList.add(sceneNodeEntity);
                            }
                        }
                        if (sceneResultList.size() != 0) {

                            for (SceneModelEntity sceneModelEntity : sceneResultList) {
                                String dateIds = sceneModelEntity.getDateIds();

                                if (sceneModelEntity.getIsCore() == 1 && StringUtils.isNotBlank(dateIds)) {//非核心
                                    String sceneId = String.valueOf(sceneModelEntity.getId());
                                    screenList.add(sceneId);
                                    screenDateList.addAll(Arrays.asList(dateIds.split(",")));
                                    String sceneNodeIds = sceneModelEntity.getNodeIds();
                                    String branchIds = sceneModelEntity.getBranchIds();
                                    String analysisIds = sceneModelEntity.getAnalysisIds();
                                    if (!"[]".equals(analysisIds) && StringUtils.isNotBlank(analysisIds)) {
                                        analysisIdsList.addAll(Arrays.asList(analysisIds.substring(1, analysisIds.length() - 1).split(",")));
                                    }

                                    if (StringUtils.isNotBlank(branchIds)) {
                                        branchIdsList.add(branchIds);
                                    }
                                    if (!"[]".equals(sceneNodeIds) && StringUtils.isNotBlank(sceneNodeIds)) {
                                        sceneNodeIdsList.addAll(Arrays.asList(sceneNodeIds.substring(1, sceneNodeIds.length() - 1).split(",")));

                                    }
                                } else {//核心的，不能删除，移除dateIds

                                    if (onlyDateList != null && dateIds != null) {
                                        onlyDateList.removeAll(Arrays.asList(dateIds.split(",")));
                                    }
                                }
                            }

                        }

                        //删除节点信息
                        if (sceneNodeIdsList.size() > 0) {
                            sql = getSql("SceneNodeModel", sceneNodeIdsList);
                            session.executeSql(sql, new HashMap<>(), false);
                        }
                        //删除分支
                        if (branchIdsList.size() > 0) {
                            sql = getSql("SceneBranchModel", branchIdsList);
                            session.executeSql(sql, new HashMap<>(), false);
                        }
                        if (analysisIdsList.size() > 0) {
                            sql = getSql("LinkAnalysisModel", analysisIdsList);
                            session.executeSql(sql, new HashMap<>(), false);
                        }
                        //   删除SceneTraceIdModel里面的数据
                        if (screenList.size() > 0) {
                            sql = getSql("SceneTraceIdModel", screenList);
                            session.executeSql(sql, new HashMap<>(), false);
                        }
                        //删除场景
                        if (screenList.size() > 0) {
                            newSkipNum = newSkipNum - screenList.size();//如果id删除了，跳过的多了，会漏掉数据

                            screenDateList.addAll(screenList);
                            sql = getSql("SceneModel", screenDateList);
                            session.executeSql(sql, new HashMap<>(), false);

                        }
                        //parentId不存在时要删除的日期id
                        if (onlyDateList.size() > 0) {
                            newSkipNum = newSkipNum - onlyDateList.size();//如果id删除了，跳过的多了，会漏掉数据

                            sql = getSql("SceneModel", onlyDateList);
                            session.executeSql(sql, new HashMap<>(), false);

                        }
//                        System.out.println("场景screenList："+screenList);
//                        System.out.println("节点sceneNodeIdsList："+sceneNodeIdsList);
//                        System.out.println("日期加场景screenDateList："+screenDateList);
//                        System.out.println("分组branchIdsList："+branchIdsList);
//                        System.out.println("要删除的日期idonlyDateList："+onlyDateList);
                    }

            } else {
                newSkipNum = Integer.MIN_VALUE;
            }

        } catch (Exception e) {
            log.error("删除数据异常{},异常msg{}:", sql, e.getMessage());
            log.error("操作异常", e);
            if (e.getMessage() != null && (e.getMessage().contains("The database") || e.getMessage().contains("driver"))) {
                newSkipNum = Integer.MIN_VALUE;
            }
        }

        if (skipNum > newSkipNum && newSkipNum != Integer.MIN_VALUE) {
            newSkipNum = skipNum;
        }
        return newSkipNum;

    }

    private String getSql(String model, ArrayList list) {
        String sql = "CALL apoc.periodic.commit(\"" +
                "      MATCH (n:" + model + ")\n" +
                "       WHERE ID(n) in $id\n" +  // 修改这里，将{id}改为$id
                "       with n \n" +
                "       limit 200\n" +
                "       DETACH   DELETE n\n" +
                "       RETURN count(*) \",{id:" + list + "}) \n" +
                "       YIELD updates\n" +
                "       RETURN updates";

        return sql;
    }

    /**
     * Parse a date array string like "['2024-01-01','2024-01-02']" into a List<String>
     */
    private List<String> parseDateArrayString(String dates) {
        List<String> result = new ArrayList<>();
        if (dates == null || dates.isEmpty()) {
            return result;
        }
        String cleaned = dates.replace("[", "").replace("]", "").replace("'", "");
        for (String part : cleaned.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

}

