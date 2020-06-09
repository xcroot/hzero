package org.hzero.generator.scan.domain.repository;


import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.UiPermissionApi;

import javax.sql.DataSource;
import java.util.List;

/**
 * UI权限组件资源库
 *
 * @author allen.liu
 * @date 2019/7/30
 */
public interface UiPermissionCompRepository {

    /**
     * 重建组件
     *
     * @param dataSource   数据源
     * @param uiComponents 组件列表
     * @param compType     组件类型
     */
    void rebuildComponent(DataSource dataSource, List<UiComponent> uiComponents, String compType);

    /**
     * 重建API
     *
     * @param dataSource       数据源
     * @param uiPermissionApis api列表
     */
    void rebuildApi(DataSource dataSource, List<UiPermissionApi> uiPermissionApis);
}
