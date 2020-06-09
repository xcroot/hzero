package org.hzero.generator.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 代码生成器
 * 
 * @name GeneratorMapper
 * @description
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:18:59
 * @version
 */
@Mapper
public interface GeneratorMapper {

    List<Map<String, Object>> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    Map<String, String> queryTable(String tableName);

    List<Map<String, String>> queryColumns(String tableName);

    List<Map<String, String>> queryIndexs(String tableName);

    void executeDDL(@Param("sql") String sql);
}
