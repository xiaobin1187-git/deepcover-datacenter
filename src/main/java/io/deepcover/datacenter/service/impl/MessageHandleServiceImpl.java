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
import io.deepcover.datacenter.dal.dao.entity.AuditLogQueryRequest;
import io.deepcover.datacenter.dal.dao.entity.AuditLogSetResult;
import io.deepcover.datacenter.dal.dao.mapper.AgentServiceDAO;
import io.deepcover.datacenter.dal.dao.mapper.UserLogDAO;
import io.deepcover.datacenter.service.MessageHandleService;
import io.deepcover.datacenter.service.controller.AresCollectController;
import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service("MessageHandleService")
@Log4j2
public class MessageHandleServiceImpl implements MessageHandleService {
    @Value("${rocketmq.consumer.tpoic.hbase}")
    String hbaseTopic;

    @Value("${hbase.table.auditLog}")
    String hbaseAuditLog;

    @Value("${hbase.table.trace}")
    String hbaseTrace;
    @Value("${traceIds}")
    String traceIds;
    @Value("${blackUrl}")
    String apis;
    @Autowired
    HbaseService hbaseService;
    @Autowired
    AuditLogServiceImpl auditLogService;

    @Autowired
    AresCollectController aresCollectController;
    @Autowired
    AgentServiceDAO agentServiceDAO;
//    @Autowired
//    UserLogDAO userLogDAO;

    // 用于去重的简单缓存，存储正在处理的traceId
    private static final int MAX_PROCESSING_TRACE_IDS = 10_000;
    private static final Set<String> processingTraceIds = ConcurrentHashMap.newKeySet();

    @Override
    public void messageHandle(HashMap<String, Object> msgBodyMap) {
        String traceId = String.valueOf(msgBodyMap.getOrDefault("traceId", ""));

        // 容量保护：超出上限时清空缓存，防止内存泄漏
        if (processingTraceIds.size() > MAX_PROCESSING_TRACE_IDS) {
            processingTraceIds.clear();
        }
        // 检查是否正在处理该traceId，避免重复处理
        if (!processingTraceIds.add(traceId)) {
            log.info("traceId={} 正在处理中，跳过重复处理", traceId);
            return;
        }
        
        try {
            //排除黑名单，减少异常分析
            if (StringUtils.isNotEmpty(traceIds)) {
                List<String> blackTraceIds = Arrays.asList(StringUtils.split(traceIds, ","));
                if (blackTraceIds.contains(traceId)) {
                    return;
                }
            }
            String url = msgBodyMap.getOrDefault("url", "").toString();
            if (!url.endsWith("/health")) {
                if (StringUtils.isNotEmpty(traceId) && !"null".equals(traceId) && !traceId.isEmpty()) {
                    String s = "{\"traceId\":\"" + traceId + "\"}";
                    AuditLogQueryRequest request = JSON.parseObject(s, AuditLogQueryRequest.class);
                    BusinessResult traceIdaceResult = hbaseService.getData(traceId, hbaseTrace);

                    List<Map<String, Object>> list = (List) traceIdaceResult.getData();
                    // 从hbase里的捞取该trace的上报信息为空，说明是第一次上报，需要先捞一下网关日志然后插到日志表里，延迟30分钟发送消息
                    if (list == null || list.size() == 0) {
                        log.debug("traceId={},第一次上报,正常查询网关信息", traceId);
                        String serviceName = msgBodyMap.get("serviceName").toString();
                        int local = agentServiceDAO.getLocalByServiceName(serviceName);

                        if (0 == local) {
                            log.debug("traceId={},是公有云服务", traceId);
                            AuditLogSetResult auditLogSetResult = auditLogService.getAuditLogs(request);
                            if (CollectionUtils.isNotEmpty(auditLogSetResult.getItems())) {
                                log.debug("traceId={},获取到网关信息", traceId);
                                String auditLog = JSON.toJSONString(auditLogSetResult.getItems().get(0));
                                Map mapAuditLog = JSON.parseObject(auditLog, Map.class);

                                if (StringUtils.isNotEmpty(apis)) {
                                    String[] blackapis = StringUtils.split(apis, ",");
                                    for (String str : blackapis) {
                                        String[] arrapi = StringUtils.split(str, ":");
                                        if (arrapi.length == 2 && mapAuditLog.containsKey("serviceId") && arrapi[0].equals(mapAuditLog.get("serviceId").toString())
                                                && mapAuditLog.containsKey("api") && arrapi[1].equals(mapAuditLog.get("api").toString())) {
                                            return;
                                        }
                                    }
                                }

                                // 去掉一些不必要的日志信息再插入hbase
                                mapAuditLog.remove("requestTime");
                                mapAuditLog.remove("requestId");
                                mapAuditLog.remove("authBlocked");
                                mapAuditLog.remove("authError");
                                mapAuditLog.remove("authTimeout");
                                mapAuditLog.remove("clientIp");
                                mapAuditLog.remove("duration");
                                mapAuditLog.remove("account");
                                mapAuditLog.remove("requestId");

                                //从网关捞出来的traceId信息插入hbase
                                hbaseService.putData(hbaseAuditLog, "traceInfo", traceId, mapAuditLog);

                            } else {
                                log.info("traceId={},serviceName={},url={},在网关查询为空", traceId, serviceName, url);
                                return;
                            }
                        }
                        //第一次写hbase时间
                        msgBodyMap.put("firstTime", System.currentTimeMillis());
                        hbaseService.putData(hbaseTrace, "traceInfo", traceId, msgBodyMap);
                        aresCollectController.aSyncSendDelayTraceId(traceId, hbaseTopic, 17);

                        //RocketMQ则不支持任意时间的延迟，它提供了18个延迟级别，源码中delayLevel=1 表示延迟1s，delayLevel=2 表示延迟5s，以此类推
                        //1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
                    } else {
                        // 该traceId的非第一次上报
                        long firstEntityBeginTime = list.stream().mapToLong((a) -> Long.valueOf(String.valueOf(a.get("firstTime") == null ? 0 : a.get("firstTime")))).max().getAsLong();
                        if (firstEntityBeginTime == 0) {
                            log.info("第一次写hbase时间设置失败");
                            firstEntityBeginTime = Long.valueOf(String.valueOf(msgBodyMap.get("beginTime")));
                        } else {
                            log.debug("第一次上报时间=" + firstEntityBeginTime + "---当前时间=" + System.currentTimeMillis());
                        }
                        hbaseService.putData(hbaseTrace, "traceInfo", traceId, msgBodyMap);

                        // 该traceId第一次写hbase距离当前时间超出60分钟时立即发送消息通知分析中心再次进行分析
                        if (firstEntityBeginTime - (System.currentTimeMillis() - 60 * 60 * 1000) < 0) {
                            log.info("补偿措施再次发送traceId");
                            aresCollectController.aSyncSendTraceId(traceId, hbaseTopic);
                        }

                    }
                }
            }
        } finally {
            // 处理完成后从正在处理集合中移除
            processingTraceIds.remove(traceId);
        }
    }

}