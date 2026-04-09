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

package io.deepcover.datacenter.service.config;/**
 * @Author: huangtai
 * @Description:
 * @Date: 2024-3-14 10:44
 */

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @author: 侯兰东
 * @date: 2024.03.14
 */
@Configuration
public class RocketMqConfig {
    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.producer.max-message-size}")
    private int maxMessageSize;

    @Value("${rocketmq.producer.send-message-timeout}")
    private int sendMessageTimeout;

    @Value("${rocketmq.producer.retry-times-when-send-failed}")
    private int retryTimesWhenSendFailed;

    @Value("${rocketmq.producer.retry-times-when-send-async-failed}")
    private int retryTimesWhenSendAsyncFailed;

    @Value("${rocketmq.producer.retry-next-server}")
    private boolean retryNextServer;

    @Value("${rocketmq.producer.compress-message-body-threshold}")
    private int compressMessageBodyThreshold;
    @Value("${mq.close}")
    private boolean mqClose;

    /**
     * 由于使用的Spring版本是3.0.0以上，与rocketMq不是很兼容，对于rocketMqTemplate
     * 的自动注入存在差异，如果不采用这种方式注入则会报出缺少bean的信息
     */
    @Bean("RocketMqTemplate")
    public RocketMQTemplate rocketMqTemplate() {
        RocketMQTemplate rocketMqTemplate = new RocketMQTemplate();
        DefaultMQProducer defaultMqProducer = new DefaultMQProducer();
        //#生产者组名，规定在一个应用里面必须唯一
        defaultMqProducer.setProducerGroup(producerGroup);
        //# mq的nameserver地址
        defaultMqProducer.setNamesrvAddr(nameServer);
        //最大的消息限制，默认为128K
        defaultMqProducer.setMaxMessageSize(maxMessageSize);
        //#消息发送的超时时间 默认3000ms
        defaultMqProducer.setSendMsgTimeout(sendMessageTimeout);
        //#异步消息发送失败重试的次数
        defaultMqProducer.setRetryTimesWhenSendAsyncFailed(retryTimesWhenSendAsyncFailed);
        //#同步消息发送失败重试次数
        defaultMqProducer.setRetryTimesWhenSendFailed(retryTimesWhenSendFailed);
        //#在内部发送失败时是否重试其他代理，这个参数在有多个broker时才生效
        defaultMqProducer.setRetryAnotherBrokerWhenNotStoreOK(retryNextServer);
        //#消息达到4096字节的时候，消息就会被压缩。默认 4096
        defaultMqProducer.setCompressMsgBodyOverHowmuch(compressMessageBodyThreshold);
        rocketMqTemplate.setProducer(defaultMqProducer);
        return rocketMqTemplate;
    }
}
