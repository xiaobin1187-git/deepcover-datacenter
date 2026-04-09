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

package io.deepcover.datacenter.service.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.QueriedLog;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.request.GetHistogramsRequest;
import com.aliyun.openservices.log.request.GetLogsRequest;
import com.aliyun.openservices.log.response.GetHistogramsResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class AliyunLogClient {

    /**
     * 最达可查询期限
     */
    private static final int MAX_QUERY_LOG_SECONDS = 3600 * 24 * 15;

    private AliyunLogProperty aliyunLogProperty;

    private Client client;

    @Autowired
    public AliyunLogClient(AliyunLogProperty aliyunLogProperty) {
        client = new Client(aliyunLogProperty.getHost(), aliyunLogProperty.getAccessId(), aliyunLogProperty.getAccessKey());
        this.aliyunLogProperty = aliyunLogProperty;
    }

    /**
     * 查询数据列表
     *
     * @param query         查询语句
     * @param beginDateTime 开始时间毫秒时间戳
     * @param endDateTime   结束时间毫秒时间戳
     * @param index         页码
     * @param size          每页显示记录数
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> query(String query, Long beginDateTime, Long endDateTime, int index, int size, Class<T> clazz) throws LogException {
        int[] timeArray = getFromToTime(beginDateTime, endDateTime);
        int offset = (index - 1) * size;
        GetLogsRequest request = new GetLogsRequest(aliyunLogProperty.getProject(), aliyunLogProperty.getStore()
                , timeArray[0], timeArray[1], "", query, offset, size, true);
        ArrayList<QueriedLog> queriedLogs = null;
        try {
            queriedLogs = client.GetLogs(request).GetLogs();
        } catch (LogException e) {
            log.error("日志查询出错:" + e.getMessage(), e);
            throw new LogException(e.GetErrorCode(), "日志查询出错", e.GetRequestId());
        }
        List<T> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(queriedLogs)) {
            return result;
        }
        queriedLogs.stream().forEach(queriedLog -> {
            JSONObject jsonLog = new JSONObject();
            queriedLog.mLogItem.GetLogContents().stream().forEach(content -> {
                try {
                    jsonLog.put(content.mKey, content.mValue);
                } catch (JSONException e) {
                    log.warn("日志JSON解析异常", e);
                }
            });
            result.add(JSON.parseObject(jsonLog.toString(), clazz));
        });
        return result;
    }

    /**
     * 查询一条数据
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T queryOne(String query, Class<T> clazz) throws LogException {
        List<T> results = query(query, null, null, 1, 1, clazz);
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0);
    }


    /**
     * 查询记录数
     *
     * @param query
     * @param beginDateTime
     * @param endDateTime
     * @return
     */
    public long queryCount(String query, Long beginDateTime, Long endDateTime) throws LogException {
        int[] timeArray = getFromToTime(beginDateTime, endDateTime);
        GetHistogramsRequest histogramsRequest = new GetHistogramsRequest(aliyunLogProperty.getProject(), aliyunLogProperty.getStore(), "", query, timeArray[0], timeArray[1]);
        GetHistogramsResponse response = null;
        try {
            response = client.GetHistograms(histogramsRequest);
        } catch (LogException e) {
            log.error("日志查询出错:" + e.getMessage(), e);
            throw new LogException(e.GetErrorCode(), "日志查询出错", e.GetRequestId());
        }
        return response.GetTotalCount();
    }

    private int[] getFromToTime(Long beginDateTime, Long endDateTime) {
        long currentTime = System.currentTimeMillis();
        long minimumFrom = currentTime - MAX_QUERY_LOG_SECONDS * 1000;
        if (beginDateTime == null && endDateTime != null) {
            log.error("开始时间和结束时间必须全为空或全不为空");
        }
        if (beginDateTime != null && endDateTime == null) {
            log.error("开始时间和结束时间必须全为空或全不为空");
        }
        if (beginDateTime == null && endDateTime == null) {
            endDateTime = currentTime;
            beginDateTime = minimumFrom;
        }
        if (beginDateTime >= endDateTime) {
            log.error("开始时间必须小于结束时间");
        }
        if (beginDateTime < minimumFrom) {
            log.error("只能查询最近两周的数据");
        }
        int[] timeArray = new int[2];
        timeArray[0] = (int) (beginDateTime / 1000L);
        timeArray[1] = (int) (endDateTime / 1000L);
        return timeArray;
    }
}
