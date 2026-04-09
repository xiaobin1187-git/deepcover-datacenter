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

import com.alibaba.fastjson.JSON;
import io.deepcover.datacenter.dal.dao.entity.NodeCreateResult;
import io.deepcover.datacenter.service.impl.HbaseService;
import io.deepcover.datacenter.common.BusinessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;


@Log4j2
@Api(value = "datacenter", tags = "hbase")
@RestController
@RequestMapping("/trace")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TraceHbaseController {

    @Autowired
    HbaseService hbaseService;

    @Value("${hbase.table.trace}")
    String hbaseTrace;

    @Value("${hbase.table.auditLog}")
    String hbaseAuditLog;

    @PostMapping("put")
    @ApiOperation("插入链路信息到hbase")
    public BusinessResult<NodeCreateResult> putTrace(@Valid @RequestBody String json) {
        Map<String, Object> map = JSON.parseObject(json, Map.class);
        String traceId = map.get("traceId").toString();
        BusinessResult<NodeCreateResult> result = hbaseService.putData(
                hbaseTrace,
                "traceInfo",
                traceId,
                map);
        return result;
    }

    @GetMapping("get")
    @ApiOperation("查询该traceId所有服务上报的信息")
    public BusinessResult<NodeCreateResult> getTraceInfo(
            @ApiParam(required = true, name = "traceId", value = "traceId") @RequestParam String traceId) {
        log.debug("进入查询traceId信息的方法");
        BusinessResult<NodeCreateResult> result = hbaseService.getData(traceId, hbaseTrace);
        return result;
    }

    @GetMapping("getAuditLog")
    @ApiOperation("查询该traceId的网关信息")
    public BusinessResult<NodeCreateResult> getAuditLog(
            @ApiParam(required = true, name = "traceId", value = "traceId") @RequestParam String traceId) {
        log.debug("进入查询traceId网关信息的方法");
        BusinessResult<NodeCreateResult> result = hbaseService.getData(traceId, hbaseAuditLog);
        return result;
    }
}
