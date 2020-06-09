package org.hzero.generator.scan.provider;

import org.hzero.generator.scan.domain.vo.RouteComponentFileVO;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 封装一些前端文件获取方法
 *
 * @author fanghan.liu 2020/01/17 10:09
 */
public interface UiFileScannerProvider {


    /**
     * 获取资源目录对象
     *
     * @param resourceDirPath 资源目录路径
     * @return 资源文件
     */
    File fetchResourceDir(String resourceDirPath);

    /**
     * 获取路由配置文件
     *
     * @param resourceDir               资源目录
     * @param routeConfigFileNamePrefix 路由配置文件名匹配前缀
     * @return 路由配置文件
     */
    Collection<File> fetchRouterFiles(File resourceDir, String routeConfigFileNamePrefix);

    /**
     * 获取路由对应的UI文件以及model文件
     *
     * @param routerConfigFiles 路由文件
     * @param routerUiFiles     输出UI文件<路由，文件>
     * @param routerModelFiles  输出model文件<路由，文件>
     * @param uiRoutes          选择的路由
     */
    void fetchRoutesAndModelFiles(Collection<File> routerConfigFiles,
                                  Map<String, Collection<RouteComponentFileVO>> routerUiFiles,
                                  Map<String, Collection<RouteComponentFileVO>> routerModelFiles,
                                  List<String> uiRoutes,
                                  Integer version);

    /**
     * 获取路由js对应的DS文件
     *
     * @param routerFiles 路由对应的routes文件夹下的文件
     * @return DS文件
     */
    Map<String, Collection<RouteComponentFileVO>> getRouteDsFile(Map<String, Collection<RouteComponentFileVO>> routerFiles);

    /**
     * 通过model文件获取对应的Service文件
     *
     * @param routeModelFiles 路由对应的model文件
     * @return Services文件
     */
    Map<String, Collection<RouteComponentFileVO>> getRouteServiceFile(Map<String, Collection<RouteComponentFileVO>> routeModelFiles);

    /**
     * 获取配置文件
     *
     * @param configFilePath  配置文件路径
     * @param resourceDirPath 前端资源路径
     * @return 文件内容
     */
    String loadServiceConfig(String configFilePath, String resourceDirPath);

}
