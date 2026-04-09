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
import io.deepcover.datacenter.service.SceneModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.service.impl.LinkAnalysisModelService;
import io.deepcover.datacenter.service.utils.DateTimeUtil;
import org.springframework.stereotype.Component;
import io.deepcover.datacenter.common.BaseJobHandler;
import io.deepcover.datacenter.common.BaseJobHandler.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.util.*;

@Component("HandleSetScreenDepthJob")
@Slf4j
public class HandleSetScreenDepthJob extends BaseJobHandler {

    @Autowired
    LinkAnalysisModelService linkAnalysisModelService;
    @Autowired
    SceneModelService sceneModelService;
    @Override
    public void execute() throws Exception {
        try {
            sceneModelService.updateSceneByIds();
            linkAnalysisModelService.setSceneRiskLevelNew();
        } catch (Exception e) {
            log.error("处理异常：", e);
        }
    }



}
