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
import com.alibaba.fastjson.JSONArray;
import io.deepcover.datacenter.dal.dao.entity.*;
import io.deepcover.datacenter.dal.dao.mapper.AgentServiceDAO;
import io.deepcover.datacenter.dal.dao.mapper.SceneLinkDateDAO;
import io.deepcover.datacenter.dal.dao.mapper.SceneRiskLevelDAO;
import io.deepcover.datacenter.dal.dao.mapper.SceneServiceClassDAO;
import io.deepcover.datacenter.dal.dao.mysqlentity.SceneLinkDate;
import io.deepcover.datacenter.dal.dao.mysqlentity.SceneRiskLevel;
import io.deepcover.datacenter.dal.dao.repository.LinkAnalysisModelRepository;
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.service.utils.DateTimeUtil;
import io.deepcover.datacenter.service.utils.MD5Utils;
import io.deepcover.datacenter.service.utils.ObjectConverterUtil;
import io.deepcover.datacenter.common.ResultEnum;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class LinkAnalysisModelService {

    @Autowired
    LinkAnalysisModelRepository linkAnalysisModelRepository;

    @Autowired
    Neo4jUtil session;

    @Autowired
    SceneModelService sceneModelService;
    @Autowired
    AgentServiceDAO serviceDAO;
    @Autowired
    SceneRiskLevelDAO riskLevelDAO;
    @Autowired
    SceneServiceClassDAO serviceClassDAO;
    @Autowired
    SceneLinkDateDAO linkDateDAO;
    List<String> orderedKeys = Arrays.asList("serviceName", "className", "methodName", "parameters", "lineNums");

    /**
     * 新增数据
     *
     * @param modelEntities
     * @return
     */
    public BusinessResult insertLinkAnalysisModel(List<LinkAnalysisModelEntity> modelEntities) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            List<Long> ids = new ArrayList<>();
            List<LinkAnalysisModelEntity> toSave = modelEntities.stream()
                    .filter(e -> {
                        if (e.getHashCode() == null) return true;
                        return linkAnalysisModelRepository.findByHashCode(e.getHashCode()) == null;
                    })
                    .collect(Collectors.toList());

            if (!toSave.isEmpty()) {
                session.saveAll(linkAnalysisModelRepository, toSave);
            }
            for (LinkAnalysisModelEntity modelEntity : modelEntities) {
                long sceneId = modelEntity.getSceneId();
                SceneLinkDate sceneLinkDate = null;
                try {
                 //   session.save(linkAnalysisModelRepository, modelEntity);
                    ids.add(modelEntity.getId());
                } catch (Exception ex) {
                    //唯一键冲突则忽略
                    String mgs = ex.getMessage();
                    if (mgs == null || (!mgs.contains("ConstraintValidationFailed") && !mgs.contains("Unique Index Conflict"))) {
                        throw ex;
                    }
                    //更新时间热度
                    List<SceneLinkDate> sceneLinkDates = linkDateDAO.findByHashCodeAndDate(modelEntity.getHashCode(), DateTimeUtil.getYMDByDate(0));
                    if (null != sceneLinkDates && sceneLinkDates.size() > 0) {
                        sceneLinkDate = sceneLinkDates.get(0);
                    }
                }
                //添加时间热度
                if (null == sceneLinkDate) {
                    sceneLinkDate = new SceneLinkDate();
                    sceneLinkDate.setHeat(1);
                    sceneLinkDate.setHashCode(modelEntity.getHashCode());
                    sceneLinkDate.setDate(DateTimeUtil.getYMDByDate(0));
                } else {
                    sceneLinkDate.setHeat(sceneLinkDate.getHeat() + 1);
                }
                sceneLinkDate.setSceneId(sceneId);
                linkDateDAO.save(sceneLinkDate);
            }
            //更新关联的方法ID
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("id", modelEntities.get(0).getSceneId());
            updateMap.put("analysisIds", ids.toString());
            sceneModelService.setSceneModel(updateMap);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("insertLinkAnalysisModel插入异常{}", ex + "---" + modelEntities.size() + ",异常msg：" + mgs);
            businessResult.setMessage(mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
        }
        return businessResult;
    }

    /**
     * 查询数据
     *
     * @param params
     * @return
     */

    public BusinessResult findLinkAnalysisModel(Map<String, Object> params) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("MATCH (n:LinkAnalysisModel) where 1=1");
            params.forEach((key, value) -> {
                if (value != null && StringUtils.isNotBlank(value.toString())) {
                    if ("id".equals(key)) {
                        stringBuffer.append(" and id(n)=$id");
                    } else {
                        stringBuffer.append(" and n." + key + "=$" + key);
                    }
                }
            });
            stringBuffer.append(" return id(n)");
            List<Map<String, Object>> result = session.executeSql(stringBuffer.toString(), params, true);
            businessResult.setData(result);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("findLinkAnalysisModel查询异常{}, 异常msg: {}", params, mgs, ex);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getCode());
            businessResult.setMessage(mgs);
        }
        return businessResult;
    }

    /**
     * 获取场景ID
     *
     * @param params
     * @return
     */
    public BusinessResult getSceneIds(Map<String, String> params) {

        String api = "";
        if (params.containsKey("api")) {
            api = params.get("api");
            params.remove("api");
        }

        int page = Integer.valueOf(params.get("page")) - 1;
        int pageSize = Integer.valueOf(params.get("pageSize"));
        String isITCov = "";
        if (params.containsKey("isITCov")) {
            isITCov = params.get("isITCov");
            params.remove("isITCov");
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("MATCH (n:LinkAnalysisModel) where 1=1");
        params.remove("page");
        params.remove("pageSize");
        params.forEach((key, value) -> {
            if (StringUtils.isNotBlank(value)) {
                stringBuffer.append(" and n." + key + " = $" + key);
            }
        });

        //TODO 删除
//        String queryEnd = " RETURN distinct n.sceneId";
//        Result queryEndResult = session.executeSqlV2(stringBuffer.toString() + queryEnd, params, true);
//        Iterable<Map<String, Object>> maps = queryEndResult.queryResults();
//        List<Long> sceneIds = new ArrayList<>();
//        for (Map<String, Object> map : maps) {
//            sceneIds.add(Long.valueOf(map.get("n.sceneId").toString()));
//        }
//        return sceneModelService.querySceneByIds(sceneIds, isITCov, page, pageSize, api);

        //TODO 历史数据结束可以切换到新的方式
        String queryEnd = " RETURN distinct n.hashCode";
        List<Map<String, Object>> queryEndResult = session.executeSqlV2(stringBuffer.toString() + queryEnd, params, true);
        Iterable<Map<String, Object>> maps = queryEndResult;
        List<String> hashCodes = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            hashCodes.add(map.get("n.hashCode").toString());
        }
        if(hashCodes==null ||hashCodes.size()==0){
            BusinessResult result = new BusinessResult(true);
            QuerySceneVO querySceneVO = new QuerySceneVO();
            querySceneVO.setQueryScenePage(new QueryScenePage());
            querySceneVO.setSceneModelEntitys(new ArrayList<>());
            querySceneVO.setApis(new ArrayList<>());
            result.setData(querySceneVO);
            return  result;
        }else{
            List<Map<String, Object>> sceneHeatList = linkDateDAO.findModel(DateTimeUtil.getDays(DateTimeUtil.getThisDayStart(-270), new Date()), hashCodes);
            return sceneModelService.queryScene(sceneHeatList, isITCov, page, pageSize, api);
        }

    }

    /**
     * 查询数据
     *
     * @param params
     * @return
     */
    public BusinessResult findLinkMessage(Map<String, Object> params, String returnMessage) {

        BusinessResult businessResult = new BusinessResult(true);
        try {

            int skip = 0;
            List allResult = new ArrayList();
            Map<String, Object> filteredParams = new HashMap<>(); // 新建过滤后的参数Map
            while (true) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("MATCH (n:LinkAnalysisModel) where 1=1");

// 先处理id
                if (params.containsKey("id")) {
                    Object idValue = params.get("id");
                    if (idValue != null && StringUtils.isNotBlank(idValue.toString())) {
                        stringBuffer.append(" and id(n)=$id");
                        filteredParams.put("id", idValue); // 将id加入过滤后的参数
                    }
                }

// 按顺序处理其他键
                for (String key : orderedKeys) {
                    if (params.containsKey(key)) {
                        Object value = params.get(key);
                        if (value != null && StringUtils.isNotBlank(value.toString())) {
                            stringBuffer.append(" and n." + key + "=$" + key);
                            filteredParams.put(key, value); // 只保留实际使用的参数
                        }
                    }
                }
                 // 使用过滤后的参数执行查询
                stringBuffer.append(" return distinct n." + returnMessage + " as " + returnMessage + " SKIP " + skip + " LIMIT " + 20);
                List<Map<String, Object>> result = session.executeSql(stringBuffer.toString(), filteredParams, true); // 使用filteredParams
                allResult.addAll(result);
                if (result.size() < 20) {
                    break;
                }
                skip += 20;
            }
            businessResult.setData(allResult);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("findLinkMessage查询异常{}", params.toString() + ",异常msg：" + mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getCode());
            businessResult.setMessage(mgs);
        }
        return businessResult;
    }

    /**
     * //TODO  历史数据处理完中高风险定义需要切换到信息统计方式
     */
    public void setSceneRiskLevelNew() {
        long startTime = System.currentTimeMillis();
        SceneRiskLevel sceneRiskLevel = riskLevelDAO.getById(1);
        List<String> serviceNames = serviceDAO.getAllServiceName();
        for (String serviceName : serviceNames) {

            //获取所有场景ID信息
            List<String> hashCodes = new ArrayList<>();
            String linkSql = "MATCH (n:LinkAnalysisModel) where n.serviceName=$serviceName RETURN distinct n.hashCode";
            Map<String, String> serviceParams = new HashMap<>();
            serviceParams.put("serviceName", serviceName);
            List<Map<String, Object>> linkSqlResult = session.executeSqlV2(linkSql, serviceParams, true);
            Iterable<Map<String, Object>> maps = linkSqlResult;
            log.info("根据服务名称获取场景Id成功" + serviceName);
            for (Map<String, Object> map : maps) {
                hashCodes.add(map.get("n.hashCode").toString());
            }
            List<Map<String, Object>> sceneHeatList = linkDateDAO.findModel(DateTimeUtil.getDays(DateTimeUtil.getThisDayStart(-270), new Date()), hashCodes);

            int totalHeat = 0;
            Map<Long, Integer> sceneFlowMap = new HashMap<>();
            for (Map<String, Object> map : sceneHeatList) {
                //获取场景的所有流量和 每个场景的流量
                long sceneId = Long.valueOf(map.get("sceneId").toString());
                int heat = Integer.valueOf(map.get("heat").toString());
                sceneFlowMap.put(sceneId, heat);
                totalHeat += heat;
            }
            //总流量的高、低分位的流量值
            int sum = 0, lowerQuantile = 0, higherQuantile = 0;
            for (long sceneId : sceneFlowMap.keySet()) {
                sum += sceneFlowMap.get(sceneId);
                float flowHeat = 1.0f * sum / totalHeat * 100;
                if (flowHeat > sceneRiskLevel.getHigherQuantile() && higherQuantile == 0) {
                    higherQuantile = sceneFlowMap.get(sceneId);
                    if (lowerQuantile == 0) {
                        lowerQuantile = sceneFlowMap.get(sceneId);
                    }
                    break;
                } else if (flowHeat > sceneRiskLevel.getLowerQuantile() && lowerQuantile == 0) {
                    lowerQuantile = sceneFlowMap.get(sceneId);
                }
            }
            //保存分位值到数据库
            SceneRiskLevel riskLevel = riskLevelDAO.getByServiceName(serviceName);
            if (null == riskLevel) {
                riskLevel = new SceneRiskLevel();
                riskLevel.setServiceName(serviceName);
                riskLevel.setHigherQuantile(higherQuantile);
                riskLevel.setLowerQuantile(lowerQuantile);
                riskLevel.setModifyDate(new Date());
                riskLevelDAO.save(riskLevel);
                log.info("新增服务风险分位值信息");
            } else {
                boolean flag = false;
                if (higherQuantile != riskLevel.getHigherQuantile()) {
                    riskLevel.setHigherQuantile(higherQuantile);
                    flag = true;
                }
                if (lowerQuantile != riskLevel.getLowerQuantile()) {
                    riskLevel.setLowerQuantile(lowerQuantile);
                    flag = true;
                }
                if (flag) {
                    riskLevel.setModifyDate(new Date());
                    riskLevelDAO.save(riskLevel);
                    log.info("修改服务风险分位值信息");
                }
            }
        }
        log.info("总耗时:" + (System.currentTimeMillis() - startTime));
    }

    public void setSceneRiskLevel() {
        long startTime = System.currentTimeMillis();
        SceneRiskLevel sceneRiskLevel = riskLevelDAO.getById(1);
        List<String> serviceNames = serviceDAO.getAllServiceName();
        for (String serviceName : serviceNames) {

            //获取所有场景ID信息
            List<Integer> sceneIds = new ArrayList<>();
            String linkSql = "MATCH (n:LinkAnalysisModel) where n.serviceName=$serviceName RETURN distinct n.sceneId";
            Map<String, String> serviceParams2 = new HashMap<>();
            serviceParams2.put("serviceName", serviceName);
            List<Map<String, Object>> linkSqlResult = session.executeSqlV2(linkSql, serviceParams2, true);
            Iterable<Map<String, Object>> maps = linkSqlResult;
            log.info("根据服务名称获取场景Id成功" + serviceName);
            for (Map<String, Object> map : maps) {
                sceneIds.add(Integer.valueOf(map.get("n.sceneId").toString()));
            }
            //获取场景的所有流量和
            Map<String, Object> params = new HashMap<>();
            params.put("id", sceneIds);
            params.put("date", DateTimeUtil.getDays(DateTimeUtil.getThisDayStart(-270), new Date()));
            String sceneSql = "MATCH p=(n:SceneModel)-[r:invoke]->(m:SceneModel) where  id(n) in $id and (m.date in $date or n.isCore=0)";
            List<Map<String, Object>> sceneSqlResult = session.executeSql(sceneSql + " RETURN sum(m.heat) as totalHeat", params, true);
            log.info("获取场景热度总和");
            List sceneSqlList = sceneSqlResult;
            Map<String, Long> sceneMap = (HashMap<String, Long>) sceneSqlList.get(0);
            long totalHeat = sceneMap.get("totalHeat");

            //分页数据查询结果
            List<SceneModelEntity> sceneModels = new ArrayList<>();
            int page = 1;
            while (true) {
                int size = 100;
                int skip = (page - 1) * size;
                String queryEnd = " RETURN id(n) as id, sum(m.heat) as flow,n.isCore as isCore order by isCore asc,flow desc SKIP " + skip + " LIMIT " + size;
                List<Map<String, Object>> querySceneResult = session.executeSql(sceneSql + queryEnd, params, true);
                log.info("分页获取场景流量信息");
                List<SceneModelEntity> sceneModelEntities = JSON.parseArray(JSON.toJSONString(querySceneResult), SceneModelEntity.class);
                sceneModels.addAll(sceneModelEntities);
                if (null == sceneModelEntities || sceneModelEntities.size() < size) {
                    break;
                }
                page++;
            }
            //总流量的高、低分位的流量值
            int sum = 0, lowerQuantile = 0, higherQuantile = 0;
            for (SceneModelEntity temp : sceneModels) {
                sum += temp.getFlow();
                float flowHeat = 1.0f * sum / totalHeat * 100;
                if (flowHeat > sceneRiskLevel.getHigherQuantile() && higherQuantile == 0) {
                    higherQuantile = temp.getFlow();
                    if (lowerQuantile == 0) {
                        lowerQuantile = temp.getFlow();
                    }
                    break;
                } else if (flowHeat > sceneRiskLevel.getLowerQuantile() && lowerQuantile == 0) {
                    lowerQuantile = temp.getFlow();
                }
            }
            //保存分位值到数据库
            SceneRiskLevel riskLevel = riskLevelDAO.getByServiceName(serviceName);
            if (null == riskLevel) {
                riskLevel = new SceneRiskLevel();
                riskLevel.setServiceName(serviceName);
                riskLevel.setHigherQuantile(higherQuantile);
                riskLevel.setLowerQuantile(lowerQuantile);
                riskLevel.setModifyDate(new Date());
                riskLevelDAO.save(riskLevel);
                log.info("新增服务风险分位值信息");
            } else {
                boolean flag = false;
                if (higherQuantile != riskLevel.getHigherQuantile()) {
                    riskLevel.setHigherQuantile(higherQuantile);
                    flag = true;
                }
                if (lowerQuantile != riskLevel.getLowerQuantile()) {
                    riskLevel.setLowerQuantile(lowerQuantile);
                    flag = true;
                }
                if (flag) {
                    riskLevel.setModifyDate(new Date());
                    riskLevelDAO.save(riskLevel);
                    log.info("修改服务风险分位值信息");
                }
            }
        }
        log.info("总耗时:" + (System.currentTimeMillis() - startTime));
    }

    public void setLinkDatesss(int start, int end) {

        List<String> nearDates = DateTimeUtil.getDays(DateTimeUtil.getThisDayStart(-30), DateTimeUtil.getThisDayStart(-1));
        String scene = "MATCH p=(n:SceneModel)-[r:invoke]->(m:SceneModel) where n.length>0 and id(n)>=$start and id(n)<$end and m.date in $dates RETURN distinct id(n),m.date,m.heat,n.serviceOrder,n.nodeIds";
        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
        params.put("dates", nearDates);
        List<Map<String, Object>> scenequery = session.executeSql(scene, params, true);
        Iterable<Map<String, Object>> maps = scenequery;
        Map<Long, SceneModelDate> ss = new HashMap<>();
        for (Map<String, Object> map : maps) {
            long sceneId = Long.valueOf(map.get("id(n)").toString());
            if (ss.containsKey(sceneId)) {
                Map<String, Integer> dateHeat = ss.get(sceneId).getDateHeat();
                dateHeat.put(map.get("m.date").toString(), Integer.valueOf(map.get("m.heat").toString()));
            } else {
                SceneModelDate temp = new SceneModelDate();
                temp.setSceneId(sceneId);
                temp.setNodeIds(map.get("n.nodeIds").toString());
                temp.setServiceOrder(map.get("n.serviceOrder").toString());
                Map<String, Integer> dateHeat = new HashMap<>();
                dateHeat.put(map.get("m.date").toString(), Integer.valueOf(map.get("m.heat").toString()));
                temp.setDateHeat(dateHeat);
                ss.put(sceneId, temp);
            }
        }
        if (ss.size() > 0) {
            for (long sceneId : ss.keySet()) {
                try {
                    SceneModelDate dddd = ss.get(sceneId);
                    String nodeSql = "MATCH (n:SceneNodeModel) where n.type=0 and id(n) in $nodeIds RETURN n.className as className,n.lineNums as lineNums,n.method as method,n.methodName as methodName,n.parameters as parameters,n.serviceName as serviceName,n.type as type";
                    Map<String, Object> nodeParams = new HashMap<>();
                    nodeParams.put("nodeIds", parseIdString(dddd.getNodeIds()));
                    List<Map<String, Object>> nodeSqlResult = session.executeSql(nodeSql, nodeParams, true);
                    String order = dddd.getServiceOrder();
                    List<SceneNodeModelBo> sceneModelEntities = JSON.parseArray(JSON.toJSONString(nodeSqlResult), SceneNodeModelBo.class);
                    //忽略上下游
                    Map<String, LinkAnalysisModelEntity> methodCollect = new HashMap<>();
                    for (SceneNodeModelBo object : sceneModelEntities) {
                        String methodKey = object.getServiceName() + "&" + object.getClassName() + "&" +
                                object.getMethodName() + "&" + object.getParameters();
                        if (methodCollect.containsKey(methodKey)) {
                            LinkAnalysisModelEntity tempModel = methodCollect.get(methodKey);
                            JSONArray lineNums = JSONArray.parseArray(tempModel.getLineNums());
                            lineNums.addAll(JSONArray.parseArray(object.getLineNums()));
                            tempModel.setLineNums(lineNums);
                            String hashCode = MD5Utils.md5(methodKey + "&" + tempModel.getServiceOrder() + "&" + tempModel.getLineNums());
                            tempModel.setHashCode(hashCode);
                        } else {
                            LinkAnalysisModelEntity linkAnalysisModel = new LinkAnalysisModelEntity();
                            ObjectConverterUtil.convert(object, linkAnalysisModel);
                            linkAnalysisModel.setLineNums(JSONArray.parseArray(object.getLineNums()));
                            linkAnalysisModel.setParameters(JSONArray.parseArray(object.getParameters()));
                            linkAnalysisModel.setServiceOrder(StringUtils.substringBefore(order, linkAnalysisModel.getServiceName()) +
                                    linkAnalysisModel.getServiceName());
                            linkAnalysisModel.setServiceName(linkAnalysisModel.getServiceName());
                            linkAnalysisModel.setSceneId(sceneId);
                            String hashCode = MD5Utils.md5(methodKey + "&" + linkAnalysisModel.getServiceOrder() + "&" + linkAnalysisModel.getLineNums());
                            linkAnalysisModel.setHashCode(hashCode);
                            methodCollect.put(methodKey, linkAnalysisModel);
                        }
                    }
                    List<LinkAnalysisModelEntity> sss = methodCollect.values().stream().collect(Collectors.toList());
                    try {
                        try {
                            List<LinkAnalysisModelEntity> toSave = sss.stream()
                                    .filter(e -> {
                                        if (e.getHashCode() == null) return true;
                                        return linkAnalysisModelRepository.findByHashCode(e.getHashCode()) == null;
                                    })
                                    .collect(Collectors.toList());

                            if (!toSave.isEmpty()) {
                                session.saveAll(linkAnalysisModelRepository, toSave);
                            }
                        } catch (Exception ex) {
                            //唯一键冲突则忽略
                            String mgs = ex.getMessage();
                            if (mgs == null || (!mgs.contains("ConstraintValidationFailed") && !mgs.contains("Unique Index Conflict"))) {
                                throw ex;
                            }
                        }
                        for (LinkAnalysisModelEntity modelEntity : sss) {

                            for (String date : dddd.getDateHeat().keySet()) {

                                List<SceneLinkDate> sceneLinkDates = linkDateDAO.findByHashCodeAndDate(modelEntity.getHashCode(), date);
                                SceneLinkDate sceneLinkDate = new SceneLinkDate();

                                //添加时间热度
                                if (null != sceneLinkDates && sceneLinkDates.size() > 0) {
                                    sceneLinkDate = sceneLinkDates.get(0);
                                    sceneLinkDate.setHeat(sceneLinkDate.getHeat() + dddd.getDateHeat().get(date));
                                } else {
                                    sceneLinkDate.setHeat(dddd.getDateHeat().get(date));
                                    sceneLinkDate.setHashCode(modelEntity.getHashCode());
                                    sceneLinkDate.setDate(date);
                                }
                                sceneLinkDate.setSceneId(sceneId);
                                linkDateDAO.save(sceneLinkDate);
                            }
                        }
                    } catch (Exception ex) {
                        String mgs = ex.getMessage();
                        log.error("insertLinkAnalysisModel插入异常{}");

                    }
                } catch (Exception e) {
                    log.error("操作异常", e);
                }
            }
        }
    }

    /**
     * Parse a string like "[1, 2, 3]" into a List<Long>
     */
    private List<Long> parseIdString(String idStr) {
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
