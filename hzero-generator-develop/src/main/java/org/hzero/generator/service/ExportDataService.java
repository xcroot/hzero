package org.hzero.generator.service;

import java.util.List;
import org.hzero.generator.dto.ServiceDTO;

/**
 * 生成初始化数据Excel服务接口
 *
 * @author wanshun.zhang@hand-china.com
 */
public interface ExportDataService {

    /**
     * 获取服务列表数据
     * @param env 环境
     * @param version 版本
     * @return 列表
     */
    List<ServiceDTO> getExportServices(String env, String version);

    /**
     * 导出服务数据
     * @param env 环境
     * @param serviceList 需要导出的服务列表
     */
    void exportInitData(String env, List<ServiceDTO> serviceList);
    /**
     * 导出服务数据-虚拟菜单根目录
     * @param env 环境
     * @param serviceList 需要导出的服务列表
     */
    void virtualExportInitData(String env, List<ServiceDTO> serviceList);

    /**
     * 导出服务数据
     * @param env 环境
     * @param dir 数据
     * @param serviceList 需要导出的服务列表
     */
    void diffExportInitData(String env, String dir, List<ServiceDTO> serviceList);
}
