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

package io.deepcover.datacenter.service.controller;

import io.deepcover.datacenter.dal.dao.entity.SceneModelEntity;
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.scheduler.HandleDeleteDataJob;
import io.deepcover.datacenter.common.BusinessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(value = "datacenter", tags = "场景模型")
@RestController
@RequestMapping("/sceneModel")
@CrossOrigin(origins = "*", maxAge = 3600)
@Log4j2
public class SceneModelController {

    @Autowired
    SceneModelService sceneModelService;

    @PostMapping("create")
    @ApiOperation("插入场景模型")
    public BusinessResult insertSceneModel(@Valid @RequestBody SceneModelEntity sceneModelEntity) {
        log.debug("insertSceneModel:" + sceneModelEntity);
        BusinessResult result = sceneModelService.insertSceneModel(sceneModelEntity);
        return result;
    }

    @PostMapping("find")
    @ApiOperation("查询模型")
    public BusinessResult findSceneModel(@RequestBody Map<String, Object> params) {
        log.debug("findSceneModel:" + params);
        BusinessResult result = sceneModelService.findSceneModel(params, "id(n) as id,n.heat as heat,n.branchIds as branchIds,n.isITCov as isITCov");
        return result;
    }

    @PostMapping("findByIds")
    @ApiOperation("通过ids查询模型")
    public BusinessResult findSceneByIds(@RequestBody Map<String, Object> params) {
        log.debug("findSceneModel:" + params);
        BusinessResult result = sceneModelService.findSceneModelByIds(params);
        return result;
    }

    @PostMapping("set")
    @ApiOperation("更新场景模型")
    public BusinessResult setSceneModel(@RequestBody Map<String, Object> params) {
        log.debug("setSceneModel:" + params);
        BusinessResult result = sceneModelService.setSceneModel(params);
        return result;
    }


    @PostMapping("addSceneTraceId")
    @ApiOperation("场景下增加TraceId")
    public BusinessResult addSceneTraceId(@RequestBody Map<String, Object> params) {
        log.debug("addSceneTraceId=" + params);
        BusinessResult result = sceneModelService.addSceneTraceId(params);
        return result;
    }

    @PostMapping("querySceneByModel")
    @ApiOperation("分页查询")
    public BusinessResult querySceneByModel(@RequestBody Map<String, String> params) {
        log.info("querySceneByModel:" + params);
        BusinessResult result = sceneModelService.querySceneByModel(params);
        return result;
    }

    @PostMapping("queryGatewayApi")
    @ApiOperation("查询网关API")
    public BusinessResult queryGatewayApi(@RequestBody Map<String, String> params) {
        log.info("queryGatewayApi:" + params);
        BusinessResult result = sceneModelService.queryGateway(params, " and n.api is not null return distinct n.api as api");
        return result;
    }

    @PostMapping("queryGatewayService")
    @ApiOperation("查询网关服务")
    public BusinessResult queryGatewayService() {
        BusinessResult result = sceneModelService.queryGateway(new HashMap<>(), " and n.serviceId is not null return distinct  n.serviceId as serviceName");
        return result;
    }

    @PostMapping("getSceneAuditLog")
    @ApiOperation("场景网关信息")
    public BusinessResult getSceneAuditLog(@RequestBody Map<String, Object> params) {
        log.debug("findSceneModel:" + params);
        BusinessResult result = sceneModelService.findSceneModel(params, "n.requestHeader as requestHeader,n.responseBody as responseBody");
        return result;
    }

    @GetMapping("updateSceneByIds")
    @ApiOperation("根据Id修改场景信息")
    public BusinessResult updateSceneByIds() {
        BusinessResult result=new BusinessResult();
        try {
             result = sceneModelService.updateSceneByIds();

        }catch (Exception e){
            log.error("updateSceneByIds操作异常", e);
            result.setData(e.getMessage());
            return result;

        }
        return result;
    }


}
