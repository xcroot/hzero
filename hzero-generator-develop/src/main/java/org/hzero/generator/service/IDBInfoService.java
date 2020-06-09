package org.hzero.generator.service;

import java.util.List;
import java.util.Map;

/**
 * 
 * 数据库对比服务接口
 * 
 * @author xianzhi.chen@hand-china.com 2018年9月17日上午11:45:22
 */
public interface IDBInfoService {

    /**
     * 开发环境
     */
    String ENV_DEV = "dev";
    /**
     * 测试环境
     */
    String ENV_TST = "tst";
    /**
     * UAT环境
     */
    String ENV_UAT = "uat";
    /**
     * 生产环境
     */
    String ENV_PRD = "prd";

    /**
     * 
     * 查询数据库(DEV)
     * 
     * @return
     */
    List<String> selectDevDatabase();

    /**
     * 
     * 查询表(DEV)
     * 
     * @param dbname
     * @return
     */
    List<String> selectDevDatabaseTable(String dbname);

    /**
     * 
     * 查询字段(DEV)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectDevDatabaseColumn(String dbname);

    /**
     * 
     * 查询索引(DEV)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectDevDatabaseIndex(String dbname);

    /**
     * 
     * 更新数据库(DEV)
     * 
     * @param sql
     */
    void updateDevDatabase(String sql);

    /**
     * 
     * 查询数据库(TST)
     * 
     * @return
     */
    List<String> selectTstDatabase();

    /**
     * 
     * 查询表(TST)
     * 
     * @param dbname
     * @return
     */
    List<String> selectTstDatabaseTable(String dbname);

    /**
     * 
     * 查询字段(TST)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectTstDatabaseColumn(String dbname);

    /**
     * 
     * 查询索引(TST)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectTstDatabaseIndex(String dbname);

    /**
     * 
     * 更新数据库(TST)
     * 
     * @param sql
     */
    void updateTstDatabase(String sql);

    /**
     * 
     * 查询数据库(UAT)
     * 
     * @return
     */
    List<String> selectUatDatabase();

    /**
     * 
     * 查询表(UAT)
     * 
     * @param dbname
     * @return
     */
    List<String> selectUatDatabaseTable(String dbname);

    /**
     * 
     * 查询字段(UAT)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectUatDatabaseColumn(String dbname);

    /**
     * 
     * 查询索引(UAT)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectUatDatabaseIndex(String dbname);

    /**
     * 
     * 更新数据库(UAT)
     * 
     * @param sql
     */
    void updateUatDatabase(String sql);

    /**
     * 
     * 查询数据库(PRD)
     * 
     * @return
     */
    List<String> selectPrdDatabase();

    /**
     * 
     * 查询表(PRD)
     * 
     * @param dbname
     * @return
     */
    List<String> selectPrdDatabaseTable(String dbname);

    /**
     * 
     * 查询字段(PRD)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectPrdDatabaseColumn(String dbname);

    /**
     * 
     * 查询索引(PRD)
     * 
     * @param dbname
     * @return
     */
    List<Map<String, String>> selectPrdDatabaseIndex(String dbname);

    /**
     * 
     * 更新数据库(PRD)
     * 
     * @param sql
     */
    void updatePrdDatabase(String sql);

}
