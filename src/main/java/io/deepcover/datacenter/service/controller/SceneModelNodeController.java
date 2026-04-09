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

import io.deepcover.datacenter.dal.dao.entity.SceneNodeModelEntity;
import io.deepcover.datacenter.service.SceneNodeModelService;
import io.deepcover.datacenter.common.BusinessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(value = "datacenter", tags = "场景模型节点")
@RestController
@RequestMapping("/sceneNodeModel")
@CrossOrigin(origins = "*", maxAge = 3600)
@Log4j2
public class SceneModelNodeController {

    @Autowired
    SceneNodeModelService sceneNodeModelService;

    @PostMapping("create")
    @ApiOperation("插入场景模型节点")
    public BusinessResult insertSceneModelNode(@Valid @RequestBody List<SceneNodeModelEntity> nodeList) {
        log.debug("insertSceneModelNode:" + nodeList);
        BusinessResult result = sceneNodeModelService.addModelNode(nodeList);
        return result;
    }

    @PostMapping("find")
    @ApiOperation("查询模型节点")
    public BusinessResult findSceneModelNode(@RequestBody Map<String, Object> params) {
        log.info("findSceneModelNode:" + params);
        BusinessResult result = sceneNodeModelService.findSceneModelNode(params);
        return result;
    }
}
