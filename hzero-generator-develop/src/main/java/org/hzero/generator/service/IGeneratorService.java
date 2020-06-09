package org.hzero.generator.service;

import java.util.List;
import java.util.Map;

import org.hzero.generator.dto.GeneratorEntity;

/**
 * 代码生成器服务接口类 description
 *
 * @author xianzhi.chen@hand-china.com 2018年6月19日下午3:21:43
 */
public interface IGeneratorService {

    /**
     * 
     * 分页查询表
     * 
     * @param map
     * @return
     */
    List<Map<String, Object>> queryList(Map<String, Object> map);

    /**
     * 
     * 查询表总数
     * 
     * @param map
     * @return
     */
    int queryTotal(Map<String, Object> map);

    /**
     * 
     * 查询表信息
     * 
     * @param tableName
     * @return
     */
    Map<String, String> queryTable(String tableName);

    /**
     * 
     * 查询表字段
     * 
     * @param tableName
     * @return
     */
    List<Map<String, String>> queryColumns(String tableName);

    /**
     * 
     * 查询表索引
     * 
     * @param tableName
     * @return
     */
    List<Map<String, String>> queryIndexs(String tableName);

    /**
     * DDD模型代码生成
     * 
     * @param info
     * @return
     */
    byte[] generatorCodeByDDD(GeneratorEntity info);

    /**
     * MVC模型代码生成
     * 
     * @param info
     * @return
     */
    byte[] generatorCodeByMVC(GeneratorEntity info);

    /**
     * Lquibase脚本生成
     * 
     * @param info
     * @return
     */
    byte[] generatorDBScript(GeneratorEntity info);

    /**
     * 执行SQL语句 description
     * 
     * @param sql
     */
    void executeDDL(String sql);

}
