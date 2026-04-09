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

import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间工具
 *
 * @Author: huangtai
 * @Date: 2022/07/31 下午2:50
 */

public class DateTimeUtil {

    /**
     * 给定日期，返回日期中的年月日
     *
     * @return
     */
    public static Date getYMDByDate(String date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date newDate = dateFormat.parse(date);
            return newDate;

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getYMDByDate(int date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = dateFormat.format(getThisDayStart(date));
        return dateStr;
    }

    /**
     * 获取给定时间间的天数
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getDays(Date startTime, Date endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(dateFormat.format(startTime));
            Date end = dateFormat.parse(dateFormat.format(endTime));

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            throw new RuntimeException("日期解析异常", e);
        }

        return days;
    }

    /**
     * 获取那天的数据
     *
     * @param day
     * @return
     */
    public static Date getThisDayStart(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    /**
     * 获取给定时间间的天数,并加上引号，为cql查询使用
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getDaysArrayString(Date startTime, Date endTime) {
        List<String> date = DateTimeUtil.getDays(startTime, endTime);

        StringBuilder sb = new StringBuilder("[");
        for (String str : date) {
            sb.append("'").append(str).append("',");
        }
        String dates = StringUtils.removeEnd(sb.toString(), ",") + "]";

        return dates;
    }

}
