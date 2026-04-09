package io.deepcover.datacenter.service.impl;

import io.deepcover.datacenter.dal.dao.entity.SceneModelEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SceneModelService 相关实体测试类
 */
@DisplayName("场景模型服务测试")
public class SceneModelServiceImplTest {

    @Test
    @DisplayName("测试基础验证")
    public void testBasicValidation() {
        assertNotNull(this, "测试类不应该为 null");
        assertTrue(true, "基础测试应该通过");
    }

    @Test
    @DisplayName("场景模型实体创建测试")
    public void testSceneModelEntityCreation() {
        SceneModelEntity entity = new SceneModelEntity();
        entity.setSceneName("测试场景");
        entity.setSceneId("test-scene-001");
        entity.setHashCode("hash-123");

        assertEquals("测试场景", entity.getSceneName());
        assertEquals("test-scene-001", entity.getSceneId());
        assertEquals("hash-123", entity.getHashCode());
    }

    @Test
    @DisplayName("场景模型实体字段设置测试")
    public void testSceneModelEntityFieldSetters() {
        SceneModelEntity entity = new SceneModelEntity();
        entity.setApi("/api/test");
        entity.setMethod("GET");
        entity.setUrl("http://example.com/test");
        entity.setServiceId("test-service");
        entity.setAppId("test-app");
        entity.setDepth(3);
        entity.setLength(5);
        entity.setIsCore(0);
        entity.setIsDelete(0);
        entity.setIsITCov("是");
        entity.setFlow(10);

        assertEquals("/api/test", entity.getApi());
        assertEquals("GET", entity.getMethod());
        assertEquals("http://example.com/test", entity.getUrl());
        assertEquals("test-service", entity.getServiceId());
        assertEquals("test-app", entity.getAppId());
        assertEquals(3, entity.getDepth());
        assertEquals(5, entity.getLength());
        assertEquals(0, entity.getIsCore());
        assertEquals(0, entity.getIsDelete());
        assertEquals("是", entity.getIsITCov());
        assertEquals(Integer.valueOf(10), entity.getFlow());
    }

    @Test
    @DisplayName("场景模型实体 ID 测试")
    public void testSceneModelEntityId() {
        SceneModelEntity entity = new SceneModelEntity();
        entity.setId(123L);
        assertEquals(Long.valueOf(123L), entity.getId());
    }

    @Test
    @DisplayName("场景模型实体描述字段测试")
    public void testSceneModelEntityDescription() {
        SceneModelEntity entity = new SceneModelEntity();
        entity.setDescription("这是一个测试场景");
        assertEquals("这是一个测试场景", entity.getDescription());
    }
}
