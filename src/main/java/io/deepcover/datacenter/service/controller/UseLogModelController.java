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

import io.deepcover.datacenter.dal.dao.mapper.UserLogDAO;
import io.deepcover.datacenter.dal.dao.mysqlentity.UserLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author: huangtai
 * @Description: 用户日志
 * @Date: 2023-8-21 14:44
 */
@Api(value = "datacenter", tags = "操作日志")
@RestController
@RequestMapping("/useLogModel")
@CrossOrigin(origins = "*", maxAge = 3600)
@Log4j2
public class UseLogModelController {

    @Autowired
    UserLogDAO userLogDAO;

    @PostMapping("insertUseLogModel")
    @ApiOperation("插入模型")
    public void insertUseLogModel(@Valid @RequestBody UserLog userLog) {
        userLogDAO.save(userLog);
    }
}
