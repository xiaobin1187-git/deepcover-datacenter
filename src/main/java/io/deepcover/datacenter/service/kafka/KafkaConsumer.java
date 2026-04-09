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

package io.deepcover.datacenter.service.kafka;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.deepcover.datacenter.service.AresCollectService;
import io.deepcover.datacenter.service.MessageHandleService;
import io.deepcover.datacenter.service.mq.MQConsumer;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Log4j2
@Component
public class KafkaConsumer {
//    @Autowired
//    MessageHandleService messageHandleService;

    @Value("${rocketmq.consumer.tpoic.ares}")
    private String consumerTpoicAres;

    @Autowired
    private AresCollectService aresCollectService;

    /**
     * 雷达实时轨迹数据加载呈现<1s，即时数据立刻消费
     *
     * @param records
     * @param ack
     */
    @KafkaListener(
            containerGroup = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.template.default-topic}",
            autoStartup = "${kafka.start}"
    )
    public void receiverRealTimeDataRecord(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        int size = records.size();
        log.debug("kafka RECV MSG COUNT: {}", size);
        for (ConsumerRecord<String, String> consumerRecord : records) {
            HashMap<String, Object> msgBodyMap = JSONObject.parseObject(consumerRecord.value(),
                    new TypeReference<HashMap<String, Object>>() {
                    });
            String traceId = msgBodyMap.getOrDefault("traceId", "null").toString();
            String serviceName = msgBodyMap.getOrDefault("serviceName", "null").toString();
            String processId = msgBodyMap.getOrDefault("processId", "null").toString();
            String codeInfoSize = msgBodyMap.getOrDefault("codeInfoSize", "null").toString();
            String url = msgBodyMap.getOrDefault("url", "null").toString();
            aresCollectService.aSyncSendDelay(consumerTpoicAres,consumerRecord.value(),traceId,serviceName,processId,codeInfoSize,url);
        }
        log.debug("kafka [RealTimeData] {} 消费完成", size);
        //确认单当前消息（及之前的消息）offset均已被消费完成
        ack.acknowledge();
    }
}
