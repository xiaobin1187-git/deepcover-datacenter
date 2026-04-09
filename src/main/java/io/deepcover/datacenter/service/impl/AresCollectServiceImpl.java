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

import io.deepcover.datacenter.dal.dao.mapper.UserLogDAO;
import io.deepcover.datacenter.service.AresCollectService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("AresCollectService")
@Log4j2
public class AresCollectServiceImpl implements AresCollectService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
//
//    @Autowired
//    UserLogDAO userLogDAO;
//
    @Value("${traceIds}")
    String traceIds;
    /**
     * 发送普通消息
     * convertAndSend(String destination, Object payload) 发送字符串比较方便
     */
    @Override
    public void send(String topic,String msg){
        rocketMQTemplate.convertAndSend(topic,msg);
    }

    /**
     * 发送同步消息
     */
    @Override
    public void syncSend(String topic,String msg){
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        SendResult sendResult = rocketMQTemplate.syncSend(topic,msg);
        log.info("消息发送成功，{}",sendResult);
    }

    /**
     * 发送异步消息
     */
    @Override
    public void aSyncSend(String topic,String msg){
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        rocketMQTemplate.asyncSend(
                topic,
                msg,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("消息发送成功，{}",sendResult);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("消息发送异常",throwable);
//                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * 发送异步延迟消息
     */
    @Override
    public void aSyncSendDelay(String topic,String codeInfo,String traceId,String serviceName,String processId,String codeInfoSize,String url){
        //排除黑名单，减少异常分析
//       String traceIds = userLogDAO.getBlackTraceId(1);
        if (StringUtils.isNotEmpty(traceIds)) {
            List<String> blackTraceIds = Arrays.asList(StringUtils.split(traceIds, ","));
            if (blackTraceIds.contains(traceId)) {
                return;
            }
        }
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        Message<?> message = MessageBuilder.withPayload(codeInfo).build();
        try{
            rocketMQTemplate.asyncSend(
                    topic,
                    message,
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.info("消息发送成功,toipc={},traceId={},serviceName={},processId={},codeInfoSize={},url={},{}",topic,traceId,serviceName,processId,codeInfoSize,url,sendResult);
                        }

                        @Override
                        public void onException(Throwable throwable) {

                            log.error("topic:{},消息发送异常，codeInfo:{}",topic,codeInfo,throwable);
//                            throwable.printStackTrace();
                        }
                    },15000,4);//"1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h"
        }catch (Exception e){
            log.error("topic:{},codeInfo:{},aSyncSendDelay异常",topic,codeInfo,e);
        }

    }


    /**
     * 发送异步延迟消息
     */
    @Override
    public void aSyncSendDelayTraceId(String traceId, String topic, int delayLevel){
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        String json = "{\"traceId\":\"" + traceId + "\"}";
        Message <?> message = MessageBuilder.withPayload(json).build();
        rocketMQTemplate.asyncSend(
                topic,
                message,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("topic={},traceId={},延迟发送消息,结果{}",topic,traceId,sendResult);

                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("topic：{},traceId：{},消息发送异常",topic,traceId,throwable);
//                        throwable.printStackTrace();
                    }
                },15000,delayLevel);
    }

    /**
     * 发送异步消息
     */
    public void aSyncSendTraceId(String traceId, String topic){
        //参数一：topic   如果想添加tag,可以使用"topic:tag"的写法
        //参数二：消息内容
        //参数三：回调
        String json = "{\"traceId\":\"" + traceId + "\"}";
        rocketMQTemplate.asyncSend(topic, json, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("topic={},traceId={}.立即发送消息，结果{}",topic,traceId,sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("topic：{},traceId：{},消息发送异常",topic,traceId,throwable);
//                throwable.printStackTrace();
            }
        });
    }
}
