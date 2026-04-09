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

package io.deepcover.datacenter.dal.dao.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@ApiModel("审计日志查询")
public class AuditLogQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("traceId")
    private String traceId;
    @ApiModelProperty("开始时间")
    private Long beginDateTime;
    @ApiModelProperty("结束时间")
    private Long endDateTime;
    @ApiModelProperty("页码")
    private Integer index = 1;
    @ApiModelProperty("每页数目")
    private Integer size = 10;

    public Long getBeginDateTime() {
        Long timeMillis = System.currentTimeMillis();
        // 开始时间以当前时间向前7天
        beginDateTime = timeMillis - 7 * 24 * 60 * 60;
        return beginDateTime;
    }

    public Long getEndDateTime() {
        endDateTime = System.currentTimeMillis();
        return endDateTime;
    }
}
