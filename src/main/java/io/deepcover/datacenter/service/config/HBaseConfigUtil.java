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

package io.deepcover.datacenter.service.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: hbase 客户端操作（单例模式）
 * @author: wuchen
 * @time: 2023/5/19 10:37
 */
@Log4j2
@EnableConfigurationProperties(HbaseProperties.class)
@Deprecated
public class HBaseConfigUtil {

    private static HbaseProperties props;
//    private final HbaseProperties props;
    private Connection connection;
    private static Configuration configuration;

    private ExecutorService hbaseClientAresThreadPool =
            new ThreadPoolExecutor(
                    20,
                    64,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(2048),
                    new ThreadFactoryBuilder().setNameFormat("HbaseConnection-AresThreadPool-%d").build(),
                    new ThreadPoolExecutor.AbortPolicy());

    /**
     * 私有构造器
     * 初始化 HBase Connection
     * @param props
     */
    private HBaseConfigUtil(HbaseProperties props) {
        this.props = props;
        configuration = initHBaseEnv();
        try {
            connection = ConnectionFactory.createConnection(configuration, hbaseClientAresThreadPool);
        } catch (IOException e) {
            log.error("HBase Client connect abnormal: ", e);
        }
    }

    /**
     * 静态内部类
     */
    private static class InstanceHolder {
        // 不会在外部类初始化时就直接加载，只有当调用了getInstance方法时才会静态加载，线程安全。
        private static final HBaseConfigUtil instance = new HBaseConfigUtil(props);
    }

    /**
     * 单例模式，获取HBase实例
     */
    @Bean
    public static HBaseConfigUtil getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 初始化 HBase 配置
     */
    @Bean
    public static Configuration initHBaseEnv() {

        try {
            configuration = HBaseConfiguration.create();
            Map<String, String> config = props.getConfig();
            config.forEach(configuration::set);
            //configuration.set("hbase.zookeeper.quorum", "");
            //configuration.set("hbase.zookeeper.property.clientPort", );
            //configuration.set("zookeeper.znode.parent", "/hbase");
            //配置连接池大小
            configuration.set("hbase.client.pause", "20");
            configuration.set("hbase.client.ipc.pool.size", "5");
        } catch (Exception e) {
            log.error("HBase Client Configuration Initialization exception: ", e);
        }
        return configuration;
    }

    /**
     * 获取namespace中所有的表名
     *
     * @param namespace
     */
    public List<String> listTables(String namespace) throws IOException {
        List<String> tableNameList = new ArrayList<>();
        // 获取namespace中所有的表名
        TableName[] tbs = connection.getAdmin().listTableNamesByNamespace(namespace);

        for (TableName tableName : tbs) {
            tableNameList.add(tableName.toString());
        }
        return tableNameList;
    }

    public Table getTable(TableName tableName) throws IOException {
        return connection.getTable(tableName);
    }

}
