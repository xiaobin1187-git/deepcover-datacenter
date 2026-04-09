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


import io.deepcover.datacenter.dal.dao.entity.AuditLogQueryRequest;
import io.deepcover.datacenter.dal.dao.entity.AuditLogResult;
import io.deepcover.datacenter.dal.dao.entity.AuditLogSetResult;
import io.deepcover.datacenter.service.AuditLogService;
import io.deepcover.datacenter.service.utils.AliyunLogClient;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("auditLogService")
@Log4j2
public class AuditLogServiceImpl implements AuditLogService {

    private static final Map<String, String> GATEWAY_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("cerberus-open", "老开放网关");
        map.put("pre-cerberus-open", "老开放网关(模拟)");
        map.put("cerberus-universal", "新开放网关");
        map.put("pre-cerberus-universal", "新开放网关(模拟)");
        GATEWAY_MAP = Collections.unmodifiableMap(map);
    }

    @Autowired
    private AliyunLogClient aliyunLogClient;

    @Override
    public AuditLogSetResult getAuditLogs(AuditLogQueryRequest request) {
        AuditLogSetResult auditLogSetResult = new AuditLogSetResult();
        String query = buildQueryDataCenter(request);
        try {
            List<AuditLogResult> logItems = aliyunLogClient.query(
                    query,
                    request.getBeginDateTime(),
                    request.getEndDateTime(),
                    request.getIndex(),
                    request.getSize(),
                    AuditLogResult.class);
            auditLogSetResult.setItems(logItems);
        } catch (Exception e) {
            log.warn("traceId网关信息为空被过滤:{}", query, e);
        }
        return auditLogSetResult;
    }

    private String buildQueryDataCenter(AuditLogQueryRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("(Source : pre-cerberus-api or Source : cerberus-api or Source : cerberus-universal or Source : pre-cerberus-universal  or Source : cerberus-pre)");
        builder.append(" not method == \"options\" and ");
        if (StringUtils.isNotBlank(request.getTraceId())) {
            builder.append(request.getTraceId());
        }
        String query = builder.toString();
        return query;
    }
}
