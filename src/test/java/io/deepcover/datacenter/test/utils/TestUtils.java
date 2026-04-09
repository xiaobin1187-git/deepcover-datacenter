package io.deepcover.datacenter.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试工具类
 * 提供测试中常用的辅助方法
 */
@Component
public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * 创建测试用的 Map 参数
     * @param keyValues 键值对（交替参数）
     * @return Map 对象
     */
    public static Map<String, Object> createParams(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even number");
        }

        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            params.put((String) keyValues[i], keyValues[i + 1]);
        }
        return params;
    }

    /**
     * 对象转 JSON 字符串
     * @param obj 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * JSON 字符串转对象
     * @param json JSON 字符串
     * @param clazz 目标类
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * 创建随机字符串
     * @param prefix 前缀
     * @return 随机字符串
     */
    public static String randomString(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }

    /**
     * 创建随机 ID
     * @return 随机 long 类型 ID
     */
    public static long randomId() {
        return System.currentTimeMillis();
    }
}
