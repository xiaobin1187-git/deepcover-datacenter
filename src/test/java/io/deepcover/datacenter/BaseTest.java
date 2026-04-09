package io.deepcover.datacenter;

import io.deepcover.datacenter.common.BusinessResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础测试类
 * 所有测试类都应该继承此类
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseTest {

    /**
     * 等待指定毫秒数（用于异步测试）
     * @param millis 毫秒数
     */
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    /**
     * 验证 BusinessResult 是否成功
     * @param result BusinessResult 对象
     */
    protected void assertBusinessResultSuccess(BusinessResult result) {
        assertNotNull(result, "BusinessResult should not be null");
        assertTrue(result.isSuccess(), "BusinessResult should be success");
        assertNull(result.getCode(), "Code should be null for success result");
    }

    /**
     * 验证 BusinessResult 是否失败
     * @param result BusinessResult 对象
     */
    protected void assertBusinessResultFailure(BusinessResult result) {
        assertNotNull(result, "BusinessResult should not be null");
        assertFalse(result.isSuccess(), "BusinessResult should be failure");
        assertNotNull(result.getCode(), "Code should not be null for failure result");
    }
}
