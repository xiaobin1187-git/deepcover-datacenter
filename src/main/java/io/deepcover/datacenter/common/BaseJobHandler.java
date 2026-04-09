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
package io.deepcover.datacenter.common;

/**
 * Base job handler for scheduled tasks.
 * Replaces internal schedulerT IJobHandler.
 */
public abstract class BaseJobHandler {

    public abstract void execute() throws Exception;

    public ReturnT<String> doExecute() {
        try {
            execute();
            return new ReturnT<>("success");
        } catch (Exception e) {
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }
    }

    public static class ReturnT<T> {
        public static final int FAIL_CODE = 500;
        @SuppressWarnings("rawtypes")
        public static final ReturnT SUCCESS = new ReturnT<>("success");
        private int code;
        private String msg;
        private T content;

        public ReturnT(T content) {
            this.code = 200;
            this.content = content;
        }

        public ReturnT(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() { return code; }
        public String getMsg() { return msg; }
        public T getContent() { return content; }
    }
}
