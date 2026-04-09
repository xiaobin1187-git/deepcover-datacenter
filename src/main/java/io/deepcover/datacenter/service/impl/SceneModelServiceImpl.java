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
import io.deepcover.datacenter.dal.dao.entity.QueryScenePage;
import io.deepcover.datacenter.dal.dao.entity.QuerySceneVO;
import io.deepcover.datacenter.dal.dao.entity.SceneModelEntity;
import io.deepcover.datacenter.dal.dao.mapper.SceneNewDAO;
import io.deepcover.datacenter.dal.dao.repository.SceneModelRepository;
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.service.utils.DateTimeUtil;
import io.deepcover.datacenter.common.ResultEnum;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("SceneModelService")
@Log4j2
public class SceneModelServiceImpl implements SceneModelService {

    @Autowired
    SceneModelRepository sceneModelRepository;

    @Autowired
    SceneBranchService sceneBranchService;

    @Autowired
    Neo4jUtil session;

    @Autowired
    SceneNewDAO sceneNewDAO;
    //从配置中心获取加了过滤条件
    @Value("${filter.delete.desc:}")
    String filterDeleteDesc;
    @Override
    public BusinessResult insertSceneModel(SceneModelEntity sceneModelEntity) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            // 添加hashCode重复检查
            String hashCode = sceneModelEntity.getHashCode();
            if (hashCode != null) {
                SceneModelEntity existing = sceneModelRepository.findByHashCode(hashCode);
                if (existing != null) {
                    businessResult.setMessage("场景hashCode已存在");
                    return businessResult;
                }
            }
            
            session.save(sceneModelRepository, sceneModelEntity);
            long scendId = sceneModelEntity.getId();
            Map<String, Object> params = new HashMap<>();
            params.put("id", scendId);
            params.put("date", DateTimeUtil.getYMDByDate(0));
            params.put("heat", 1);
            params.put("parentId", scendId);
            return addSceneTraceId(params);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            if (mgs != null && (mgs.contains("ConstraintValidationFailed") || mgs.contains("Unique Index Conflict"))) {
                log.debug("插入数据违反唯一约束，忽略");
                businessResult.setCode("1000009");
            } else {
                log.error("插入SceneModel失败:" + mgs);
                businessResult.setMessage(mgs);
                businessResult.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
            }
        }
        return businessResult;
    }

    // 白名单：允许查询的字段，防止Cypher注入
    private static final Set<String> ALLOWED_QUERY_FIELDS = new HashSet<>(Arrays.asList(
        "sceneName", "sceneId", "serviceOrder", "isCore", "isDelete", "isITCov",
        "description", "length", "depth", "isNew", "hashCode", "source", "api",
        "method", "requestBody", "requestHeader", "responseBody", "serviceId",
        "url", "appId", "nodeIds", "dateIds", "branchIds", "analysisIds", "flow"
    ));

    @Override
    public BusinessResult findSceneModel(Map<String, Object> params, String returnMessage) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("MATCH (n:SceneModel) where 1=1");
            params.forEach((key, value) -> {
                // 添加更严格的空值检查和字段白名单验证
                if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                    if ("id".equals(key)) {
                        stringBuffer.append(" and id(n)=$id");
                    } else if (ALLOWED_QUERY_FIELDS.contains(key)) {
                        // 只允许白名单中的字段，防止Cypher注入
                        stringBuffer.append(" and n." + key + "=$" + key);
                    } else {
                        log.warn("findSceneModel: 忽略非法查询字段: {}", key);
                    }
                }
            });
            stringBuffer.append(" return " + returnMessage);
            List<Map<String, Object>> result = session.executeSql(stringBuffer.toString(), params, true);
            businessResult.setData(result);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            // 修改日志记录方式，避免params.toString()可能为null
            log.error("findSceneModel查询异常，参数: {}, 异常信息: {}",
                params != null ? params : "null",
                mgs != null ? mgs : "null");
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getCode());
            businessResult.setMessage(mgs);
        }
        return businessResult;
    }

    @Override
    public BusinessResult findSceneModelByIds(Map<String, Object> params) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            String sql = "MATCH (n:SceneModel) where  id(n) in $id " +
                    "RETURN n.isNew as isNew,n.serviceOrder as serviceOrder,n.serviceId as serviceId,n.sceneId as sceneId,n.method as method,n.api as api,n.requestBody as requestBody,n.requestHeader as requestHeader,n.responseBody as responseBody,n.sceneName as sceneName,n.url as url,n.isDelete as isDelete,n.depth as depth,n.length as length,id(n) as strId,n.isCore as isCore";
            List<Map<String, Object>> queryEndResult = session.executeSql(sql, params, true);
            businessResult.setData(queryEndResult);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("findSceneModel查询异常{}", ex + params.toString(), ",异常msg", mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getCode());
            businessResult.setMessage(mgs);
        }
        return businessResult;
    }

    @Override
    @Transactional
    public BusinessResult updateSceneByIds() {
        BusinessResult businessResult = new BusinessResult(true);
        List<Long> sceneIds = sceneNewDAO.getSceneIds(DateTimeUtil.getYMDByDate(-1));
        session.deleteByPage(sceneIds.toString(), "SceneModel", " set n.isNew=1");
        sceneNewDAO.deleteBySceneIdIn(sceneIds);
        return businessResult;
    }


    @Override
    public BusinessResult setSceneModel(Map<String, Object> params) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            StringBuffer stringBufferquery = new StringBuffer();
            stringBufferquery.append("MATCH (n:SceneModel) where id(n) = $id Set");
            params.forEach((key, value) -> {
                if (!"id".equals(key)) {
                    stringBufferquery.append(" n." + key + "=$" + key + ",");
                }
            });
            stringBufferquery.delete(stringBufferquery.length() - 1, stringBufferquery.length());
            stringBufferquery.append(" return id(n)");
            List<Map<String, Object>> result = session.executeSql(stringBufferquery.toString(), params, false);
            businessResult.setData(result);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("setSceneModel修改异常{}", ex + params.toString(), ",异常msg", mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getCode());
            businessResult.setMessage(mgs);
        }
        return businessResult;
    }

    @Override
    public BusinessResult addSceneTraceId(Map<String, Object> params) {
        BusinessResult businessResult = new BusinessResult(true);
        try {
            String s = "Match(n:SceneModel) where id(n)=$id create(p:SceneModel {})<-[w:invoke]-(n) return id(p),n.dateIds";
            StringBuffer stringBufferquery = new StringBuffer();
            params.forEach((key, value) -> {
                if (!"id".equals(key)) {
                    stringBufferquery.append(key + ":$" + key + ",");
                }
            });
            stringBufferquery.delete(stringBufferquery.length() - 1, stringBufferquery.length());
            String query = s.replace("{}", "{" + stringBufferquery.toString() + "}");
            List<Map<String, Object>> result = session.executeSql(query, params, false);
            //把子节点ID关联到模型中方便后续查询删除用
            List queryResult = result;
            if (queryResult.size() > 0) {
                HashMap<String, Object> map = (HashMap<String, Object>) queryResult.get(0);
                String dateId = map.get("id(p)").toString();
                String dateIds = "";
                Object object = map.get("n.dateIds");
                if (object == null) {
                    dateIds = dateId;
                } else {
                    dateIds = object + "," + dateId;
                }
                Map<String, Object> sceneDateIds = new HashMap<>();
                sceneDateIds.put("id", params.get("id"));
                sceneDateIds.put("dateIds", dateIds);
                setSceneModel(sceneDateIds);
                businessResult.setData(Arrays.asList(params.get("id")));
            }
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("addSceneTraceId新增节点异常{}", ex + params.toString(), ",异常msg", mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
            businessResult.setMessage(mgs);
        }
        return businessResult;
    }

    public BusinessResult queryScene(List<Map<String, Object>> sceneHeatList, String isITCov, int page, int pageSize, String api) {
        List<Long> sceneIds = new ArrayList<>();
        Map<Long, Integer> sceneFlowMap = new HashMap<>();
        int totalHeat = 0;
        for (Map<String, Object> map : sceneHeatList) {
            long sceneId = Long.valueOf(map.get("sceneId").toString());
            int heat = Integer.valueOf(map.get("heat").toString());
            sceneIds.add(sceneId);
            sceneFlowMap.put(sceneId, heat);
            totalHeat += heat;
        }
        BusinessResult result = new BusinessResult(true);
        QuerySceneVO querySceneVO = new QuerySceneVO();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", sceneIds);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("MATCH (n:SceneModel) where id(n) in $sceneIds");
            params.put("sceneIds", sceneIds);

            if ("否".equals(isITCov)) {
                stringBuffer.append(" and (n.isITCov is null or n.isITCov=$isITCov)");
                params.put("isITCov", isITCov);
            } else if ("是".equals(isITCov)) {
                stringBuffer.append(" and n.isITCov=$isITCov");
                params.put("isITCov", isITCov);
            }
            if (StringUtils.isNotEmpty(api)) {
                stringBuffer.append(" and n.api=$api");
                params.put("api", api);
            } else {
                querySceneVO.setApis(findApisByIds(sceneIds));
            }

            int skip = page * pageSize;
            String queryEnd = " RETURN  n.isITCov as isITCov, n.sceneId as sceneId,n.isNew as isNew,n.serviceOrder as serviceOrder,n.serviceId as serviceId,n.method as method,n.api as api,n.requestBody as requestBody,n.sceneName as sceneName,n.url as url,n.isDelete as isDelete,n.depth as depth,n.length as length,id(n) as id,n.isCore as isCore order by isCore asc SKIP " + skip + " LIMIT " + pageSize;
            String countEnd = " RETURN count(distinct id(n)) as totalNum,max(n.length) as maxLength,max(n.depth) as maxDepth";

            //分页数据查询结果
            List<Map<String, Object>> queryEndResult = session.executeSql(stringBuffer.toString() + queryEnd, params, true);
            //查询总数
            List<Map<String, Object>> countEndResultList = session.executeSql(stringBuffer.toString() + countEnd, params, true);
            List<SceneModelEntity> sceneModelEntities = JSON.parseArray(JSON.toJSONString(queryEndResult), SceneModelEntity.class);
            for (SceneModelEntity sceneModelEntity : sceneModelEntities) {
                sceneModelEntity.setFlow(sceneFlowMap.get(sceneModelEntity.getId()));
            }
            querySceneVO.setSceneModelEntitys(sceneModelEntities);
//            List countEndResultList = (ArrayList) countEndResult.queryResults();
            if (countEndResultList.isEmpty()) {
                result.setData(querySceneVO);
                return result;
            }
            QueryScenePage queryScenePage = new QueryScenePage();
            BeanUtils.populate(queryScenePage, (Map) countEndResultList.get(0));
            queryScenePage.setTotalHeat(totalHeat);
            querySceneVO.setQueryScenePage(queryScenePage);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("queryScene查询场景信息异常", mgs);
            result.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
            result.setMessage(mgs);
        }
        result.setData(querySceneVO);
        return result;
    }

    @Override
    public BusinessResult querySceneByModel(Map<String, String> params) {
        BusinessResult businessResult = new BusinessResult(true);
        QuerySceneVO querySceneVO = new QuerySceneVO();
        try {
            //根据分支获取场景Id
            List<Long> sceneIds = new ArrayList<>();
            if (params.containsKey("branch")) {
                String branch = params.get("branch");
                sceneIds = sceneBranchService.getSceneIdsByBranch(branch);
                params.remove("branch");
            }
            //分页信息
            int page = Integer.valueOf(params.get("page")) - 1;
            int size = Integer.valueOf(params.get("pageSize"));
            params.remove("page");
            params.remove("pageSize");
            //获取最近7天时间
            List<String> date = DateTimeUtil.getDays(DateTimeUtil.getThisDayStart(-270), new Date());
            //组装查询语句 - 使用 Object 参数 Map 以支持列表类型
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.putAll(params);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("MATCH p=(n:SceneModel)-[r:invoke]->(m:SceneModel) where n.depth>0  and (m.date in $dates or n.isCore=0)");
            queryParams.put("dates", date);
            if (params.containsKey("isITCov")) {
                String isITCov = params.get("isITCov");
                if ("否".equals(isITCov)) {
                    stringBuffer.append(" and (n.isITCov is null or n.isITCov=$isITCov)");
                    queryParams.put("isITCov", isITCov);
                } else if ("是".equals(isITCov)) {
                    stringBuffer.append(" and n.isITCov=$isITCov");
                    queryParams.put("isITCov", isITCov);
                }
                queryParams.remove("isITCov");
            }

            params.forEach((key, value) -> {
                if (StringUtils.isNotBlank(value) && !"isITCov".equals(key) && !"page".equals(key) && !"pageSize".equals(key) && !"branch".equals(key)) {
                    stringBuffer.append(" and n." + key + " = $" + key);
                }
            });
            int skip = page * size;
            String queryEnd = " RETURN n.isITCov as isITCov,n.sceneId as sceneId,n.isNew as isNew,n.serviceOrder as serviceOrder,n.serviceId as serviceId,n.method as method,n.api as api,n.requestBody as requestBody,n.sceneName as sceneName,n.url as url,n.isDelete as isDelete,n.depth as depth,id(n) as id,sum(m.heat) as flow,n.isCore as isCore order by isCore asc,flow desc SKIP " + skip + " LIMIT " + size;
            String countEnd = " RETURN count(distinct id(n)) as totalNum,sum(m.heat) as totalHeat";

            if (sceneIds.size() > 0) {
                stringBuffer.append(" and id(n) in $sceneIds");
                queryParams.put("sceneIds", sceneIds);
            }
            //清理分页和分支参数
            queryParams.remove("page");
            queryParams.remove("pageSize");
            queryParams.remove("branch");
            //分页数据查询结果]
            List<Map<String, Object>> queryEndResult = session.executeSql(stringBuffer.toString() + queryEnd, queryParams, true);
            //查询总数
            List<Map<String, Object>> countEndResultList = session.executeSql(stringBuffer.toString() + countEnd, queryParams, true);
            //封装为分页类
            List<SceneModelEntity> sceneModelEntities = JSON.parseArray(JSON.toJSONString(queryEndResult), SceneModelEntity.class);
            querySceneVO.setSceneModelEntitys(sceneModelEntities);
//            List countEndResultList = (ArrayList) countEndResult.queryResults();
            if (countEndResultList.isEmpty()) {
                businessResult.setData(querySceneVO);
                return businessResult;
            }
            QueryScenePage queryScenePage = new QueryScenePage();
            BeanUtils.populate(queryScenePage, (Map) countEndResultList.get(0));
            querySceneVO.setQueryScenePage(queryScenePage);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("querySceneByModel分页查询异常{}", ex + params.toString(), ",异常msg", mgs);
            businessResult.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
            businessResult.setMessage(mgs);
        }
        businessResult.setData(querySceneVO);
        return businessResult;
    }

    /**
     * 获取网关的api
     *
     * @return
     */
    @Override
    public BusinessResult queryGateway(Map<String, String> params, String returnParam) {
        BusinessResult result = new BusinessResult(true);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("MATCH (n:SceneModel) where 1=1");
        params.forEach((key, value) -> {
            if (StringUtils.isNotBlank(value)) {
                stringBuffer.append(" and n." + key + " = $" + key);
            }
        });
        List<Map<String, Object>> queryResult = session.executeSqlV2(stringBuffer.toString() + returnParam, params, true);
        result.setData(queryResult);
        return result;
    }

    public List findApisByIds(List<Long> sceneIds) {

        List<String> apis = new ArrayList<>();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", sceneIds);
            String sql = "MATCH (n:SceneModel) where id(n) in $id RETURN distinct n.api";
            List<Map<String, Object>> queryEndResult = session.executeSql(sql, params, true);
            Iterable<Map<String, Object>> maps = queryEndResult;
            for (Map<String, Object> map : maps) {
                apis.add(map.get("n.api").toString());
            }
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("findSceneModel查询异常{}", ex + sceneIds.toString(), ",异常msg", mgs);
        }
        return apis;
    }

    public BusinessResult querySceneByIds(List<Long> sceneIds, String isITCov, int page, int pageSize, String api) {

        BusinessResult result = new BusinessResult(true);
        QuerySceneVO querySceneVO = new QuerySceneVO();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", sceneIds);
            params.put("date", DateTimeUtil.getDays(DateTimeUtil.getThisDayStart(-270), new Date()));
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("MATCH p=(n:SceneModel)-[r:invoke]->(m:SceneModel) where n.depth>0 and id(n) in $id and (m.date in $date or n.isCore=0)");

            if ("否".equals(isITCov)) {
                stringBuffer.append(" and (n.isITCov is null or n.isITCov=$isITCov)");
                params.put("isITCov", isITCov);
            } else if ("是".equals(isITCov)) {
                stringBuffer.append(" and n.isITCov=$isITCov");
                params.put("isITCov", isITCov);
            }
            if (StringUtils.isNotEmpty(api)) {
                stringBuffer.append(" and n.api=$api");
                params.put("api", api);
            } else {
                querySceneVO.setApis(findApisByIds(sceneIds));
            }

            int skip = page * pageSize;
            String queryEnd = " RETURN  n.isITCov as isITCov, n.sceneId as sceneId,n.isNew as isNew,n.serviceOrder as serviceOrder,n.serviceId as serviceId,n.method as method,n.api as api,n.requestBody as requestBody,n.sceneName as sceneName,n.url as url,n.isDelete as isDelete,n.depth as depth,n.length as length,id(n) as id,sum(m.heat) as flow,n.isCore as isCore order by isCore asc,flow desc SKIP " + skip + " LIMIT " + pageSize;
            String countEnd = " RETURN count(distinct id(n)) as totalNum,sum(m.heat) as totalHeat,max(n.length) as maxLength,max(n.depth) as maxDepth";

            //分页数据查询结果
            List<Map<String, Object>> queryEndResult = session.executeSql(stringBuffer.toString() + queryEnd, params, true);
            //查询总数
            List<Map<String, Object>> countEndResult = session.executeSql(stringBuffer.toString() + countEnd, params, true);
            List<SceneModelEntity> sceneModelEntities = JSON.parseArray(JSON.toJSONString(queryEndResult), SceneModelEntity.class);
            querySceneVO.setSceneModelEntitys(sceneModelEntities);
            List countEndResultList = (ArrayList) countEndResult;
            if (countEndResultList.isEmpty()) {
                result.setData(querySceneVO);
                return result;
            }
            QueryScenePage queryScenePage = new QueryScenePage();
            BeanUtils.populate(queryScenePage, (Map) countEndResultList.get(0));
            querySceneVO.setQueryScenePage(queryScenePage);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("queryScene查询场景信息异常", mgs);
            result.setCode(ResultEnum.SYSTEM_ERROR.getErrCode() + "");
            result.setMessage(mgs);
        }
        result.setData(querySceneVO);
        return result;
    }

}
