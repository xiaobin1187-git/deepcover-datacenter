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
 * Result enum for business result codes.
 */
public enum ResultEnum {

    SUCCESS("0", "success"),
    SYSTEM_ERROR("500", "system error"),
    PARAM_ERROR("400", "parameter error"),
    NOT_FOUND("404", "not found");

    private final String code;
    private final String errCode;
    private final String msg;

    ResultEnum(String code, String msg) {
        this.code = code;
        this.errCode = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getErrCode() {
        return errCode;
    }

    public String getMsg() {
        return msg;
    }
}
