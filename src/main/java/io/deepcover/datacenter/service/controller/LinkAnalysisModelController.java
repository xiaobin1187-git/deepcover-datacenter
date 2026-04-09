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

import io.deepcover.datacenter.dal.dao.entity.LinkAnalysisModelEntity;
import io.deepcover.datacenter.service.impl.LinkAnalysisModelService;
import io.deepcover.datacenter.common.BusinessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "datacenter", tags = "链路模型")
@RestController
@RequestMapping("/linkAnalysisModel")
@CrossOrigin(origins = "*", maxAge = 3600)
@Log4j2
public class LinkAnalysisModelController {

    @Autowired
    LinkAnalysisModelService linkAnalysisModelService;
    @Value("${start.id}")
    private int startId;
    @Value("${end.id}")
    private int endId;


    @PostMapping("create")
    @ApiOperation("插入链路模型")
    public BusinessResult insertSceneModel(@Valid @RequestBody List<LinkAnalysisModelEntity> linkAnalysisModelEntity) {
        BusinessResult result = linkAnalysisModelService.insertLinkAnalysisModel(linkAnalysisModelEntity);
        return result;
    }

    @PostMapping("find")
    @ApiOperation("查询模型")
    public BusinessResult findLinkAnalysisModel(@RequestBody Map<String, Object> params) {
        log.info("findLinkAnalysisModel:" + params);
        BusinessResult result = linkAnalysisModelService.findLinkAnalysisModel(params);
        return result;
    }

    @PostMapping("getSceneIds")
    @ApiOperation("获取场景ID")
    public BusinessResult getSceneIds(@RequestBody Map<String, String> params) {
        log.debug("getSceneIds:" + params);
        BusinessResult result = linkAnalysisModelService.getSceneIds(params);
        return result;
    }

    @PostMapping("queryAllService")
    @ApiOperation("查询应用列表")
    public BusinessResult queryAllService() {
        BusinessResult result = linkAnalysisModelService.findLinkMessage(new HashMap<>(), "serviceName");
        return result;
    }

    @PostMapping("queryClassName")
    @ApiOperation("查询类名")
    public BusinessResult queryClassName(@RequestBody Map<String, Object> params) {
        log.info("queryClassName:" + params);
        BusinessResult result = linkAnalysisModelService.findLinkMessage(params, "className");
        return result;
    }

    @PostMapping("queryMethodName")
    @ApiOperation("查询方法名称")
    public BusinessResult queryMethodName(@RequestBody Map<String, Object> params) {
        log.info("queryMethodName:" + params);
        BusinessResult result = linkAnalysisModelService.findLinkMessage(params, "methodName");
        return result;
    }

    @PostMapping("queryParameters")
    @ApiOperation("查询方法参数")
    public BusinessResult queryParameters(@RequestBody Map<String, Object> params) {
        log.debug("queryParameters:" + params);
        BusinessResult result = linkAnalysisModelService.findLinkMessage(params, "parameters");
        return result;
    }

    @PostMapping("queryLineNums")
    @ApiOperation("查询行号")
    public BusinessResult queryLineNums(@RequestBody Map<String, Object> params) {
        log.info("queryLineNums:" + params);
        BusinessResult result = linkAnalysisModelService.findLinkMessage(params, "lineNums");
        return result;
    }


    @GetMapping("setSceneRiskLevel")
    @ApiOperation("场景风险等级")
    public void setSceneRiskLevel() {
        //TODO 改为调用setSceneRiskLevelNew 犯法
        linkAnalysisModelService.setSceneRiskLevelNew();
    }
}
