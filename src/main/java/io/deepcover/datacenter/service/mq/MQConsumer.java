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

package io.deepcover.datacenter.service.mq;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.deepcover.datacenter.service.MessageHandleService;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: huangtai
 * @Description:
 * @Date: 2024-3-20 10:58
 */
@Log4j2
@Component
public class MQConsumer {

    @Value("${mq.close}")
    private boolean mqClose;

    @Value("${rocketmq.consumer.group.ares}")
    private String consumerGroupAres;

    @Value("${rocketmq.consumer.tpoic.ares}")
    private String consumerTpoicAres;

    @Value("${rocketmq.name-server}")
    private String nameServer;
    @Autowired
    MessageHandleService messageHandleService;

    @PostConstruct
    public void init() throws MQClientException {
        if (!mqClose) {
            //创建消费者
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroupAres);
            //绑定nameserver
            consumer.setNamesrvAddr(nameServer);
            //订阅topic，设置过滤规则
            consumer.subscribe(consumerTpoicAres, "*");
            // 判断消息数量是否超过限制（默认为1000），超过则本次不请求拉取消息，提交延迟任务请求
            consumer.setPullThresholdForQueue(40);
            // 判断消息大小是否超过限制（默认为100M），超过则本次不请求拉取消息，提交延迟任务请求
            consumer.setPullThresholdSizeForQueue(50);
            // 设置最大消费线程数 Max consumer thread number
            consumer.setConsumeThreadMax(32);
            //注册监听，当队列有消息时，触发消费
            //MessageListenerConcurrently，用的多线程模式消费，即使只有一个消费者也是并发消费消息
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,
                                                                ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    try {
                        for (MessageExt messageExt : list) {
                            log.debug("开始进入到onMessage,nowTime={}" ,System.currentTimeMillis());
                            HashMap<String, Object> msgBodyMap = JSONObject.parseObject(new String(messageExt.getBody()),
                                    new TypeReference<HashMap<String, Object>>() {
                                    });
                            messageHandleService.messageHandle(msgBodyMap);
                        }
                    } catch (Exception e) {
                        log.error("MQ消费信息异常", e);
                        log.error("MQ消费信息异常:" + e.getMessage());
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            consumer.start();
        }
    }
}
