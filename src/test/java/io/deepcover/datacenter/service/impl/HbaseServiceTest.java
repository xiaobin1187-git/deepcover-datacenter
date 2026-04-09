package io.deepcover.datacenter.service.impl;

import io.deepcover.datacenter.common.BusinessResult;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HbaseService unit tests focusing on resource management and error handling.
 * After C-2 fix: uses try-with-resources, no more NPE in finally block.
 */
public class HbaseServiceTest {

    private HbaseService hbaseService;
    private Connection mockConnection;
    private Table mockTable;

    @BeforeEach
    public void setUp() throws Exception {
        hbaseService = new HbaseService();
        mockConnection = mock(Connection.class);
        mockTable = mock(Table.class);

        java.lang.reflect.Field connField = HbaseService.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(hbaseService, mockConnection);

        when(mockConnection.getTable(any(TableName.class))).thenReturn(mockTable);
    }

    @Test
    public void testPutDataSuccess() {
        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", 123);

        BusinessResult result = hbaseService.putData("testTable", "cf", "row1", data);

        assertNotNull(result);
        assertEquals("200", result.getCode());
        assertEquals("成功", result.getMessage());
    }

    @Test
    public void testPutDataClosesTable() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");

        hbaseService.putData("testTable", "cf", "row1", data);

        verify(mockTable).close();
    }

    @Test
    public void testPutDataConnectionFailure() throws IOException {
        // After C-2 fix: try-with-resources handles table cleanup gracefully
        when(mockConnection.getTable(any(TableName.class)))
                .thenThrow(new RuntimeException("connection failed"));

        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");

        BusinessResult result = hbaseService.putData("testTable", "cf", "row1", data);
        assertEquals("500", result.getCode());
    }

    @Test
    public void testGetDataSuccess() throws IOException {
        org.apache.hadoop.hbase.client.Result mockHbaseResult = mock(org.apache.hadoop.hbase.client.Result.class);
        when(mockHbaseResult.listCells()).thenReturn(null);
        when(mockTable.get(any(org.apache.hadoop.hbase.client.Get.class))).thenReturn(mockHbaseResult);

        BusinessResult result = hbaseService.getData("trace123", "traceTable");

        assertNotNull(result);
        assertEquals("200", result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    public void testGetDataClosesTable() throws IOException {
        org.apache.hadoop.hbase.client.Result mockHbaseResult = mock(org.apache.hadoop.hbase.client.Result.class);
        when(mockHbaseResult.listCells()).thenReturn(null);
        when(mockTable.get(any(org.apache.hadoop.hbase.client.Get.class))).thenReturn(mockHbaseResult);

        hbaseService.getData("trace123", "traceTable");

        verify(mockTable).close();
    }

    @Test
    public void testGetDataConnectionFailure() throws IOException {
        // After C-2 fix: gracefully returns error result
        when(mockConnection.getTable(any(TableName.class)))
                .thenThrow(new RuntimeException("connection failed"));

        BusinessResult result = hbaseService.getData("trace123", "traceTable");
        assertEquals("500", result.getCode());
    }

    @Test
    public void testPutDataSkipsNullValues() {
        // After C-2 fix: null values are skipped instead of causing NPE
        Map<String, Object> data = new HashMap<>();
        data.put("nullKey", null);
        data.put("validKey", "validValue");

        BusinessResult result = hbaseService.putData("testTable", "cf", "row1", data);
        assertEquals("200", result.getCode());
    }
}
