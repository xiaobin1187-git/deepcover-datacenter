package io.deepcover.datacenter.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BusinessResultTest {

    @Test
    public void testDefaultConstructor() {
        BusinessResult<Object> result = new BusinessResult<>();
        assertFalse(result.isSuccess());
        assertNull(result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void testSuccessConstructor() {
        BusinessResult<Object> result = new BusinessResult<>(true);
        assertTrue(result.isSuccess());
        assertEquals("0", result.getCode());
    }

    @Test
    public void testFailureConstructor() {
        BusinessResult<Object> result = new BusinessResult<>(false);
        assertFalse(result.isSuccess());
        assertNull(result.getCode());
    }

    @Test
    public void testStaticSuccessFactory() {
        BusinessResult<String> result = BusinessResult.success("test data");
        assertTrue(result.isSuccess());
        assertEquals("test data", result.getData());
        assertEquals("0", result.getCode());
    }

    @Test
    public void testStaticFailFactory() {
        BusinessResult<Object> result = BusinessResult.fail("500", "system error");
        assertFalse(result.isSuccess());
        assertEquals("500", result.getCode());
        assertEquals("system error", result.getMessage());
    }

    @Test
    public void testSettersAndGetters() {
        BusinessResult<String> result = new BusinessResult<>();
        result.setSuccess(true);
        result.setCode("200");
        result.setMessage("ok");
        result.setData("payload");
        assertTrue(result.isSuccess());
        assertEquals("200", result.getCode());
        assertEquals("ok", result.getMessage());
        assertEquals("payload", result.getData());
    }

    @Test
    public void testSuccessWithNullData() {
        BusinessResult<Object> result = BusinessResult.success(null);
        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    public void testFailWithNullMessage() {
        BusinessResult<Object> result = BusinessResult.fail("400", null);
        assertFalse(result.isSuccess());
        assertEquals("400", result.getCode());
        assertNull(result.getMessage());
    }
}
