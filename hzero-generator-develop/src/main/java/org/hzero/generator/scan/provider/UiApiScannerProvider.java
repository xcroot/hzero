package org.hzero.generator.scan.provider;


import org.hzero.generator.scan.domain.UiPermissionApi;

import java.io.File;
import java.util.List;

/**
 * 页面API扫描
 *
 * @author fanghan.liu 2020/01/13 10:12
 */
public interface UiApiScannerProvider {

    /**
     * 扫描API
     *
     * @param apiFile   api所属文件
     * @param pageRoute 路由
     * @return api列表
     */
    List<UiPermissionApi> scanApi(File apiFile, String pageRoute);

    /**
     * 扫描出services包下的api
     *
     * @param content   文件内容
     * @param pageRoute 路由
     * @return api列表
     */
    List<UiPermissionApi> scanServicesFileApi(File apiFile, String content, String pageRoute);

    /**
     * 扫描出stores包下的api
     *
     * @param content   文件内容
     * @param pageRoute 路由
     * @return api列表
     */
    List<UiPermissionApi> scanStoresFileApi(File apiFile, String content, String pageRoute);

    /**
     * 解析api服务名
     *
     * @param uiPermissionApiList 权限api列表
     * @param serviceConfig       服务配置
     */
    void parseApiService(List<UiPermissionApi> uiPermissionApiList, String serviceConfig);
}
