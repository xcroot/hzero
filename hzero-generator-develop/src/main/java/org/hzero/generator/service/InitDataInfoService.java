package org.hzero.generator.service;

import java.util.List;
import org.hzero.generator.entity.Service;

/**
 * 描述:
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/12/04 17:58
 */
public interface InitDataInfoService {

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
     * 开发环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportDevData(List<Service> serviceList);

    /**
     * 测试环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportTstData(List<Service> serviceList);

    /**
     * UAT环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportUatData(List<Service> serviceList);

    /**
     * 生产环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportPrdData(List<Service> serviceList);
    /**
     * 开发环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportVirtualDevData(List<Service> serviceList);

    /**
     * 测试环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportVirtualTstData(List<Service> serviceList);

    /**
     * UAT环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportVirtualUatData(List<Service> serviceList);

    /**
     * 生产环境
     *
     * @param serviceList 需要导出的列表
     */
    void exportVirtualPrdData(List<Service> serviceList);
    /**
     * 开发环境
     *
     * @param serviceList 需要导出的列表
     */
    void diffExportDevData(List<Service> serviceList,String dir);

    /**
     * 测试环境
     *
     * @param serviceList 需要导出的列表
     */
    void diffExportTstData(List<Service> serviceList,String dir);

    /**
     * UAT环境
     *
     * @param serviceList 需要导出的列表
     */
    void diffExportUatData(List<Service> serviceList,String dir);

    /**
     * 生产环境
     *
     * @param serviceList 需要导出的列表
     */
    void diffExportPrdData(List<Service> serviceList,String dir);

    /**
     * 开发环境
     *
     */
    List<String> selectDevDatabase();
    /**
     * 测试环境
     *
     */
    List<String> selectTstDatabase();
    /**
     * 演示环境
     *
     */
    List<String> selectUatDatabase();
    /**
     * 生产环境
     *
     */
    List<String> selectPrdDatabase();

    /**
     * 开发环境
     *
     * @param schema 数据库名称
     */
    void createDevDatabase(String database, String schema);

    /**
     * 测试环境
     *
     * @param schema 数据库名称
     */
    void createTstDatabase(String database, String schema);

    /**
     * 演示环境
     *
     * @param schema 数据库名称
     */
    void createUatDatabase(String database, String schema);

    /**
     * 生产环境
     *
     * @param schema 数据库名称
     */
    void createPrdDatabase(String database, String schema);
}
