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

package io.deepcover.datacenter.dal.dao.mysqlentity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author mybatis-plus-generator
 * @since 2023-07-26
 */
@Data
@Table(name = "agent_service")
@Entity
public class AgentService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 在线节点数
     */
    private Integer onLineNum;

    /**
     * 映射的包名
     */
    private String packageName;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 过滤不需要采集的接口
     */
    private String ignoreUrls;

    /**
     * 过滤不需要采集的类
     */
    private String ignoreClasses;

    /**
     * 过滤不需要采集的方法
     */
    private String ignoreMethods;

    /**
     * 过滤不需要采集的注解
     */
    private String ignoreAnnos;

    /**
     * 采样率
     */
    private Long sampleRate;

    /**
     * 服务状态上报的频率，单位是秒
     */
    private Integer reportPeriod;

    /**
     * 异常阈值，超过则熔断不采集
     */
    private Long exceptionThreshold;

    /**
     * 状态：0-初始化，1-采集中，2-停止采集
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date modifiedTime;

    /**
     * 创建用户
     */
    private String addBy;

    /**
     * 更新用户
     */
    private String updateBy;

    //1本地化 0非本地
    private Integer local;
}
