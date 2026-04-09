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

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.Serializable;

@Node("LinkAnalysisModel")
@Getter
@Setter
public class LinkAnalysisModelEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    //类名
    private String className;

    //方法名称
    private String methodName;

    //方法参数
    private String parameters;

    //服务名
    private String serviceName;

    //行号
    private String lineNums;

    //应用顺序
    private String serviceOrder;

    //场景ID
    private long sceneId;

    //场景是否重复的唯一标识
    private String hashCode;

    public void setParameters(JSONArray parameters) {
        if (parameters != null) {
            this.parameters = parameters.toString();
        }
    }

    public void setLineNums(JSONArray lineNums) {
        if (lineNums != null) {
            this.lineNums = lineNums.toString();
        }
    }
}
