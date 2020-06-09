package org.hzero.generator.scan.infra.builder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 构造器
 *
 * @author allen.liu
 * @date 2019/7/29
 */
public class ObjectMapperBuilder {
    private static ObjectMapper objectMapper;

    /**
     * 构造Object Mapper
     *
     * @return
     */
    public static synchronized ObjectMapper buildObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            // 允许属性名没有引号
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, Boolean.TRUE);
            // 允许数组或对象最后一个元素后的逗号
            objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, Boolean.TRUE);
            // 允许行内注释
            objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, Boolean.TRUE);
            // 允许单引号作为界定符
            objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, Boolean.TRUE);
        }

        return objectMapper;
    }
}
