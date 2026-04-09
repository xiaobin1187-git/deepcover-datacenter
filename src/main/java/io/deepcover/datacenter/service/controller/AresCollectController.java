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

import io.deepcover.datacenter.dal.dao.entity.CodeEntity;
import io.deepcover.datacenter.service.AresCollectService;
//import io.deepcover.datacenter.service.mq.MQConsumer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.Map;

@Log4j2
@RestController
public class AresCollectController {

    @Value("${rocketmq.consumer.tpoic.ares}")
    private String consumerTpoicAres;

    @Autowired
    private AresCollectService aresCollectService;

    /**
     * 发送普通消息
     * convertAndSend(String destination, Object payload) 发送字符串比较方便
     */
    @PostMapping("/send")
    public void send() {
        aresCollectService.send(consumerTpoicAres, "test-message");
    }

    /**
     * 发送同步消息
     */
    @PostMapping("/syncSend")
    public void syncSend() {
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        aresCollectService.syncSend(consumerTpoicAres, "同步消息测试");
    }

    /**
     * 发送异步消息
     */
    @PostMapping("/aSyncSend")
    public String aSyncSend(@RequestBody CodeEntity codeEntity) {
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        aresCollectService.aSyncSend(consumerTpoicAres, codeEntity.getCodeInfo());
        return "ok";
    }


    /**
     * 发送异步延迟消息
     */
    @PostMapping("/aSyncSendDelay")
    public String aSyncSendDelay(@RequestBody String codeInfo, HttpServletRequest request) {
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        String traceId = request.getHeader("traceid");
        String serviceName = request.getHeader("servicename");
        String processId = request.getHeader("processid");
        String codeInfoSize = request.getHeader("codeInfoSize");
        String url = request.getHeader("url");
        aresCollectService.aSyncSendDelay(consumerTpoicAres, codeInfo, traceId, serviceName, processId, codeInfoSize, url);
        return traceId + "," + processId;
    }

    /**
     * 发送异步延迟消息并传递traceId
     */
    @PostMapping("/aSyncSendDelayWithTraceId/{traceId}")
    public String aSyncSendDelayWithTraceId(@RequestBody String codeInfo, @PathVariable("traceId") String traceId) {
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
//        String traceId =request.getHeader("traceid");
        aresCollectService.aSyncSendDelay(consumerTpoicAres, codeInfo, traceId, "unknow", "unknow", "unknow", null);
        return traceId;
    }

    /**
     * 发送异步延迟消息
     */
    @PostMapping("/aSyncSendDelayForm")
    public void aSyncSendDelayForm(@Context HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.size() == 0) {
            return;
        }
        String traceId = request.getHeader("traceid");
        String serviceName = request.getHeader("servicename");
        String processId = request.getHeader("processid");
        aresCollectService.aSyncSendDelay(consumerTpoicAres, parameterMap.get("codeInfo")[0], traceId, serviceName, processId, "unknow", null);
    }

    /**
     * 发送异步延迟消息
     */
    @PostMapping("/aSyncSendDelayTraceId")
    public void aSyncSendDelayTraceId(@RequestBody String traceId, String topic, int delayLevel) {
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        aresCollectService.aSyncSendDelayTraceId(traceId, topic, delayLevel);
    }

    /**
     * 发送异步消息
     */
    @PostMapping("/aSyncSendTraceId")
    public void aSyncSendTraceId(@RequestBody String traceId, String topic) {
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        aresCollectService.aSyncSendTraceId(traceId, topic);
    }
}
