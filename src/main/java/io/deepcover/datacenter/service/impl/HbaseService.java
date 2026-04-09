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

import io.deepcover.datacenter.common.BusinessResult;
import lombok.extern.log4j.Log4j2;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class HbaseService {

    @Autowired
    private Connection connection;

    public BusinessResult putData(String tableName, String colFamily, String rowKey, Map<String, Object> data) {
        BusinessResult businessResult = new BusinessResult();
        TableName table = TableName.valueOf(tableName);
        try (Table t = connection.getTable(table)) {
            Put put = new Put(Bytes.toBytes(rowKey));
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                put.addColumn(Bytes.toBytes(colFamily),
                        Bytes.toBytes(entry.getKey()),
                        Bytes.toBytes(entry.getValue().toString()));
            }
            t.put(put);
            log.info("traceId={},插入hbase表={},正常", rowKey, tableName);
            businessResult.setCode("200");
            businessResult.setMessage("成功");

        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("插入异常{}", mgs);
            businessResult.setMessage(mgs);
            businessResult.setCode("500");
            businessResult.setData(new ArrayList<>());
        }
        return businessResult;
    }

    public BusinessResult getData(String traceId, String tableName) {
        BusinessResult businessResult = new BusinessResult();
        TableName table = TableName.valueOf(tableName);
        try (Table t = connection.getTable(table)) {
            List<Map<String, Object>> entity = new ArrayList<>();
            Get get = new Get(traceId.getBytes());
            get.readVersions(10);
            get.addFamily("traceInfo".getBytes());
            Result result = t.get(get);
            List<org.apache.hadoop.hbase.Cell> cells = result.listCells();
            List<Long> versionlist = new ArrayList<>();
            if (null != cells && !cells.isEmpty()) {
                for (org.apache.hadoop.hbase.Cell ce : cells) {
                    Map<String, Object> map = new HashMap<>();
                    String value = Bytes.toString(ce.getValueArray(),
                            ce.getValueOffset(),
                            ce.getValueLength());
                    String key = Bytes.toString(ce.getQualifierArray(),
                            ce.getQualifierOffset(),
                            ce.getQualifierLength());

                    Long timestamp = ce.getTimestamp();
                    map.put(key, value);
                    if (versionlist.contains(timestamp)) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> existing = (Map<String, Object>) entity.get(versionlist.indexOf(timestamp));
                        map.putAll(existing);
                        entity.set(versionlist.indexOf(timestamp), map);
                    } else {
                        entity.add(map);
                        versionlist.add(timestamp);
                    }
                }
                log.debug("traceId={},存在记录条数={}", traceId, entity.size());
            }
            businessResult.setData(entity);
            businessResult.setCode("200");
            businessResult.setMessage("成功");
        } catch (Exception ex) {
            String mgs = ex.getMessage();
            log.error("查询异常{}", mgs);
            businessResult.setMessage(mgs);
            businessResult.setCode("500");
            businessResult.setData(new ArrayList<>());
        }
        return businessResult;
    }

}
