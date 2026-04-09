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

import io.deepcover.datacenter.dal.dao.mapper.AgentServiceDAO;
import io.deepcover.datacenter.common.BusinessResult;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Log4j2
@RestController
@RequestMapping("/agentService")
public class AgentServiceController {

    @Autowired
    private AgentServiceDAO agentServiceDAO;

    @GetMapping("getLocalByServiceName")
    @ApiOperation("是否本地服务")
    public BusinessResult getLocalByServiceName(@RequestParam String serviceName) {
        BusinessResult result = new BusinessResult(true);
        result.setData(Arrays.asList(agentServiceDAO.getLocalByServiceName(serviceName)));
        return result;
    }
}
