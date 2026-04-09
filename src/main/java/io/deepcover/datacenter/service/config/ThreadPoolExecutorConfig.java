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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jiuxi
 * @version 1.0
 * @date 2021/8/12 15:11
 */
@Configuration
@EnableAsync
public class ThreadPoolExecutorConfig {
    private static int CORE_POOL_SIZE = 5;
    private static int MAX_POOL_SIZE = 5;

    @Bean(name = {"threadPoolTaskExecutor"})
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        return initThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 10000, 30000, "ARES-Datacenter-ThreadPoolExecutor-");
    }

    /**
     * 构建 线程池
     *
     * @param corePoolSize
     * @param maxPoolSize
     * @param queueCapacity
     * @param keepAliveSeconds
     * @param threadNamePrefix
     * @return
     */
    public static ThreadPoolTaskExecutor initThreadPoolExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds, String threadNamePrefix) {
        ThreadPoolTaskExecutor poolTaskExecutor = new ThreadPoolTaskExecutor();
        //线程池维护线程的最少数量
        poolTaskExecutor.setCorePoolSize(corePoolSize);
        //线程池维护线程的最大数量
        poolTaskExecutor.setMaxPoolSize(maxPoolSize);
        //线程池所使用的缓冲队列
        poolTaskExecutor.setQueueCapacity(queueCapacity);
        //线程池维护线程所允许的空闲时间
        poolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        poolTaskExecutor.setThreadNamePrefix(threadNamePrefix);
        poolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return poolTaskExecutor;
    }

}
