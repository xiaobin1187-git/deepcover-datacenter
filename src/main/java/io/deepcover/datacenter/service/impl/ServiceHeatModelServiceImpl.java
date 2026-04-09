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

import io.deepcover.datacenter.dal.dao.entity.ServiceHeatModelEntity;
import io.deepcover.datacenter.dal.dao.repository.ServiceHeatModelRepository;
import io.deepcover.datacenter.service.ServiceHeatModelService;
import io.deepcover.datacenter.service.config.Neo4jUtil;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ServiceHeatModelService")
@Log4j2
public class ServiceHeatModelServiceImpl implements ServiceHeatModelService {

    @Autowired
    ServiceHeatModelRepository serviceHeatModelRepository;

    @Autowired
    Neo4jUtil neo4jUtil;

    @Override
    public BusinessResult insertServiceHeatModel(List<ServiceHeatModelEntity> modelEntity) {

        BusinessResult businessResult = new BusinessResult(true);
        try {
            neo4jUtil.saveAll(serviceHeatModelRepository, modelEntity);
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("insertServiceHeatModel异常msg:" + "异常msg：" + mgs);
            log.error("insertServiceHeatModel操作异常", ex);
            businessResult = new BusinessResult(false);
            businessResult.setMessage(ex.getMessage());
        }
        return businessResult;
    }
}
