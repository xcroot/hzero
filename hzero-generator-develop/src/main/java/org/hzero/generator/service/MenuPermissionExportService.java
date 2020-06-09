package org.hzero.generator.service;

import java.util.List;

/**
 * @Description 菜单权限导出
 * @Date 2020-02-17 14:20
 * @Author wanshun.zhang@hand-china.com
 */
public interface MenuPermissionExportService {
    /**
     * 导出菜单权限
     *
     * @param routes 导出参数
     */
    void exportMenuPermission(List<String> routes,String version);

}
