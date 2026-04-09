package io.deepcover.datacenter.service.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Neo4jUtil unit tests using Mockito.
 * Tests parameterized query handling and resource management.
 */
public class Neo4jUtilTest {

    private Neo4jUtil neo4jUtil;
    private Driver mockDriver;
    private Session mockSession;

    @BeforeEach
    public void setUp() throws Exception {
        neo4jUtil = new Neo4jUtil();
        mockDriver = mock(Driver.class);
        mockSession = mock(Session.class);

        // Inject mock driver via reflection
        java.lang.reflect.Field driverField = Neo4jUtil.class.getDeclaredField("driver");
        driverField.setAccessible(true);
        driverField.set(neo4jUtil, mockDriver);

        java.lang.reflect.Field nodeSizeField = Neo4jUtil.class.getDeclaredField("nodeSize");
        nodeSizeField.setAccessible(true);
        nodeSizeField.set(neo4jUtil, 100);

        when(mockDriver.session(any(SessionConfig.class))).thenReturn(mockSession);
    }

    @Test
    public void testExecuteSqlWithParamsReturnsEmpty() {
        Result mockResult = mock(Result.class);
        when(mockResult.list()).thenReturn(Collections.emptyList());
        when(mockSession.run(anyString(), any(Map.class))).thenReturn(mockResult);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "test");
        List<Map<String, Object>> result = neo4jUtil.executeSql(
                "MATCH (n:Scene) WHERE n.name = $name RETURN n", params, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testExecuteSqlWithParamsReturnsData() {
        Result mockResult = mock(Result.class);
        Value mockValue = mock(Value.class);
        when(mockValue.hasType(any())).thenReturn(false);
        when(mockValue.asObject()).thenReturn("testValue");

        Record mockRecord = mock(Record.class);
        when(mockRecord.keys()).thenReturn(Collections.singletonList("name"));
        when(mockRecord.get("name")).thenReturn(mockValue);
        when(mockResult.list()).thenReturn(Collections.singletonList(mockRecord));
        when(mockSession.run(anyString(), any(Map.class))).thenReturn(mockResult);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "test");
        List<Map<String, Object>> result = neo4jUtil.executeSql(
                "MATCH (n) WHERE n.name = $name RETURN n.name as name", params, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testValue", result.get(0).get("name"));
    }

    @Test
    public void testExecuteSqlSessionClosed() {
        Result mockResult = mock(Result.class);
        when(mockResult.list()).thenReturn(Collections.emptyList());
        when(mockSession.run(anyString(), any(Map.class))).thenReturn(mockResult);

        neo4jUtil.executeSql("RETURN 1", new HashMap<>(), true);

        verify(mockSession).close();
    }

    @Test
    public void testExecuteSqlV2ConvertsNumericParams() {
        Result mockResult = mock(Result.class);
        when(mockResult.list()).thenReturn(Collections.emptyList());
        when(mockSession.run(anyString(), any(Map.class))).thenReturn(mockResult);

        Map<String, String> params = new HashMap<>();
        params.put("count", "42");
        params.put("price", "3.14");
        params.put("name", "hello");

        neo4jUtil.executeSqlV2("RETURN 1", params, true);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockSession).run(eq("RETURN 1"), captor.capture());

        Map<String, Object> converted = captor.getValue();
        assertEquals(42L, converted.get("count"));
        assertEquals(3.14, converted.get("price"));
        assertEquals("hello", converted.get("name"));
    }

    @Test
    public void testExecuteSqlV2NullParams() {
        Result mockResult = mock(Result.class);
        when(mockResult.list()).thenReturn(Collections.emptyList());
        when(mockSession.run(anyString(), any(Map.class))).thenReturn(mockResult);

        neo4jUtil.executeSqlV2("RETURN 1", null, true);

        verify(mockSession).run(eq("RETURN 1"), any(Map.class));
    }

    @Test
    public void testQueryExecutesSql() {
        Result mockResult = mock(Result.class);
        when(mockSession.run(anyString(), any(Map.class))).thenReturn(mockResult);

        neo4jUtil.query("CREATE (n:Test {name: $name})", Collections.singletonMap("name", "test"));

        verify(mockSession).run(eq("CREATE (n:Test {name: $name})"), any(Map.class));
        verify(mockSession).close();
    }

    @Test
    public void testExecuteSqlV2HandlesNumberFormatException() {
        Result mockResult = mock(Result.class);
        when(mockResult.list()).thenReturn(Collections.emptyList());
        when(mockSession.run(anyString(), any(Map.class))).thenReturn(mockResult);

        Map<String, String> params = new HashMap<>();
        params.put("value", "not-a-number.abc");

        neo4jUtil.executeSqlV2("RETURN $value", params, true);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockSession).run(eq("RETURN $value"), captor.capture());

        assertEquals("not-a-number.abc", captor.getValue().get("value"));
    }
}
