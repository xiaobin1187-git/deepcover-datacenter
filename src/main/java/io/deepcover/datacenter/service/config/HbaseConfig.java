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
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(HbaseProperties.class)
public class HbaseConfig implements DisposableBean {

    private final HbaseProperties props;

    private ExecutorService hbaseClientAresThreadPool =
            new ThreadPoolExecutor(
                    20,
                    64,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(2048),
                    new ThreadFactoryBuilder().setNameFormat("HbaseConnection-AresThreadPool-%d").build(),
                    new ThreadPoolExecutor.AbortPolicy());

    public HbaseConfig(HbaseProperties props) {
        this.props = props;
    }

    @Override
    public void destroy() {
        hbaseClientAresThreadPool.shutdownNow();
    }

    @Bean
    public org.apache.hadoop.conf.Configuration configuration() {
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        Map<String, String> config = props.getConfig();
        config.forEach(conf::set);

        //连接池大小[hbase.client.ipc.pool.size] 1 => 5 /n
        //RPC重试系数调低 100 => 20
        conf.set("hbase.client.pause", "20");
        conf.set("hbase.client.ipc.pool.size", "5");
        return conf;
    }

    @Bean
    public Connection getConnection() throws IOException {
        return ConnectionFactory.createConnection(configuration(), hbaseClientAresThreadPool);
    }

    @Bean
    public HBaseAdmin hBaseAdmin() throws IOException {
        return (HBaseAdmin) getConnection().getAdmin();
    }
}
