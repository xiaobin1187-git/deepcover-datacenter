package io.deepcover.datacenter.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResultEnumTest {

    @Test
    public void testSuccessCode() {
        assertEquals("0", ResultEnum.SUCCESS.getCode());
        assertEquals("success", ResultEnum.SUCCESS.getMsg());
    }

    @Test
    public void testSystemErrorCode() {
        assertEquals("500", ResultEnum.SYSTEM_ERROR.getCode());
        assertEquals("system error", ResultEnum.SYSTEM_ERROR.getMsg());
    }

    @Test
    public void testParamErrorCode() {
        assertEquals("400", ResultEnum.PARAM_ERROR.getCode());
    }

    @Test
    public void testNotFoundCode() {
        assertEquals("404", ResultEnum.NOT_FOUND.getCode());
    }

    @Test
    public void testErrCodeEqualsCode() {
        for (ResultEnum e : ResultEnum.values()) {
            assertEquals(e.getCode(), e.getErrCode());
        }
    }

    @Test
    public void testAllValuesExist() {
        assertEquals(4, ResultEnum.values().length);
    }
}
