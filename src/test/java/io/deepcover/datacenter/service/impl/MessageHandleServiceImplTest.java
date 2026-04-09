package io.deepcover.datacenter.service.impl;

import io.deepcover.datacenter.dal.dao.entity.CodeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageHandleServiceImpl 相关实体测试类
 */
@DisplayName("消息处理服务测试")
public class MessageHandleServiceImplTest {

    @Test
    @DisplayName("CodeEntity 基本属性设置")
    public void testCodeEntityCreation() {
        CodeEntity entity = new CodeEntity();
        entity.setCodeInfo("{\"lines\":[1,2,3]}");
        assertEquals("{\"lines\":[1,2,3]}", entity.getCodeInfo());
    }

    @Test
    @DisplayName("CodeEntity codeInfo 为 null 时不抛异常")
    public void testCodeEntityNullCodeInfo() {
        CodeEntity entity = new CodeEntity();
        assertNull(entity.getCodeInfo());
    }

    @Test
    @DisplayName("CodeEntity 默认值验证")
    public void testCodeEntityDefaults() {
        CodeEntity entity = new CodeEntity();
        assertNotNull(entity);
        assertNull(entity.getCodeInfo());
    }
}
