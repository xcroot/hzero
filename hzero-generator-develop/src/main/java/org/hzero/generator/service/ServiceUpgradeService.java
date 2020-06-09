package org.hzero.generator.service;

import java.util.List;

/**
 * @author liguo.wang
 */
public interface ServiceUpgradeService {
    /**
     * 查询升级列表
     * @return List<String>
     */
    List<String> listServiceUpgrade();

    /**
     * 服务升级步骤处理
     * @param version 版本
     */
    void serviceUpgrade(String version);

    /**
     * 数据修复
     * @param version 版本
     */
    void dataUpdate(String version);
}
