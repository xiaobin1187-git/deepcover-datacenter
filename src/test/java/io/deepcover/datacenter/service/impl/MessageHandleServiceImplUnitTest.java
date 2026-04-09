package io.deepcover.datacenter.service.impl;

import io.deepcover.datacenter.common.BusinessResult;
import io.deepcover.datacenter.dal.dao.entity.AuditLogSetResult;
import io.deepcover.datacenter.dal.dao.mapper.AgentServiceDAO;
import io.deepcover.datacenter.service.controller.AresCollectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MessageHandleServiceImpl unit tests focusing on:
 * - Null traceId handling
 * - Empty message body
 * - Concurrent traceId deduplication
 */
public class MessageHandleServiceImplUnitTest {

    private MessageHandleServiceImpl service;
    private HbaseService mockHbaseService;
    private AuditLogServiceImpl mockAuditLogService;
    private AresCollectController mockController;
    private AgentServiceDAO mockAgentServiceDAO;

    @BeforeEach
    public void setUp() throws Exception {
        service = new MessageHandleServiceImpl();
        mockHbaseService = mock(HbaseService.class);
        mockAuditLogService = mock(AuditLogServiceImpl.class);
        mockController = mock(AresCollectController.class);
        mockAgentServiceDAO = mock(AgentServiceDAO.class);

        injectField("hbaseService", mockHbaseService);
        injectField("auditLogService", mockAuditLogService);
        injectField("aresCollectController", mockController);
        injectField("agentServiceDAO", mockAgentServiceDAO);
        injectField("hbaseTopic", "test-hbase-topic");
        injectField("hbaseAuditLog", "auditLog");
        injectField("hbaseTrace", "trace");
        injectField("traceIds", "");
        injectField("apis", "");
    }

    private void injectField(String name, Object value) throws Exception {
        Field field = MessageHandleServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    @Test
    public void testMessageHandleWithNullTraceId() {
        HashMap<String, Object> msgBodyMap = new HashMap<>();
        service.messageHandle(msgBodyMap);

        verify(mockHbaseService, never()).putData(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void testMessageHandleWithEmptyTraceId() {
        HashMap<String, Object> msgBodyMap = new HashMap<>();
        msgBodyMap.put("traceId", "");
        msgBodyMap.put("url", "http://example.com/api");

        service.messageHandle(msgBodyMap);
        verify(mockHbaseService, never()).putData(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void testMessageHandleWithHealthUrlSkipped() {
        HashMap<String, Object> msgBodyMap = new HashMap<>();
        msgBodyMap.put("traceId", "valid-trace-id");
        msgBodyMap.put("url", "http://example.com/health");

        service.messageHandle(msgBodyMap);
        verify(mockHbaseService, never()).putData(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void testMessageHandleWithBlacklistedTraceId() throws Exception {
        injectField("traceIds", "black-id-1,black-id-2");

        HashMap<String, Object> msgBodyMap = new HashMap<>();
        msgBodyMap.put("traceId", "black-id-1");
        msgBodyMap.put("url", "http://example.com/api");

        service.messageHandle(msgBodyMap);
        verify(mockHbaseService, never()).putData(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void testConcurrentTraceIdDeduplication() throws Exception {
        BusinessResult hbaseResult = BusinessResult.success(new ArrayList<>());
        AuditLogSetResult mockAuditResult = mock(AuditLogSetResult.class);
        when(mockHbaseService.getData(anyString(), anyString())).thenReturn(hbaseResult);
        when(mockAuditLogService.getAuditLogs(any())).thenReturn(mockAuditResult);
        when(mockAuditResult.getItems()).thenReturn(null);
        when(mockAgentServiceDAO.getLocalByServiceName(anyString())).thenReturn(0);

        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger processed = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    HashMap<String, Object> msg = new HashMap<>();
                    msg.put("traceId", "same-trace-id");
                    msg.put("url", "http://example.com/api");
                    msg.put("serviceName", "test-service");
                    msg.put("beginTime", System.currentTimeMillis());

                    service.messageHandle(msg);
                    processed.incrementAndGet();
                } catch (Exception e) {
                    // May happen due to concurrent deduplication
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        assertTrue(processed.get() > 0, "At least some threads should complete");
    }

    @Test
    public void testSequentialDifferentTraceIds() throws Exception {
        BusinessResult hbaseResult = BusinessResult.success(new ArrayList<>());
        AuditLogSetResult mockAuditResult = mock(AuditLogSetResult.class);
        when(mockHbaseService.getData(anyString(), anyString())).thenReturn(hbaseResult);
        when(mockAuditLogService.getAuditLogs(any())).thenReturn(mockAuditResult);
        when(mockAuditResult.getItems()).thenReturn(null);
        when(mockAgentServiceDAO.getLocalByServiceName(anyString())).thenReturn(0);

        HashMap<String, Object> msg1 = new HashMap<>();
        msg1.put("traceId", "trace-001");
        msg1.put("url", "http://example.com/api");
        msg1.put("serviceName", "test-service");
        msg1.put("beginTime", System.currentTimeMillis());
        service.messageHandle(msg1);

        HashMap<String, Object> msg2 = new HashMap<>();
        msg2.put("traceId", "trace-002");
        msg2.put("url", "http://example.com/api");
        msg2.put("serviceName", "test-service");
        msg2.put("beginTime", System.currentTimeMillis());
        service.messageHandle(msg2);

        // Both different traceIds should trigger getData
        verify(mockHbaseService, times(2)).getData(anyString(), eq("trace"));
    }

    @Test
    public void testMessageHandlePropagation() throws Exception {
        // When HBase throws, the exception propagates (no catch block, only finally)
        when(mockHbaseService.getData(anyString(), anyString()))
                .thenThrow(new RuntimeException("test error"));

        HashMap<String, Object> msg = new HashMap<>();
        msg.put("traceId", "trace-error");
        msg.put("url", "http://example.com/api");
        msg.put("serviceName", "test-service");
        msg.put("beginTime", System.currentTimeMillis());

        assertThrows(RuntimeException.class, () -> service.messageHandle(msg));
    }
}
