package org.hzero.generator.scan.provider;

import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.vo.RouteComponentFileVO;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author jianbo.li
 * @date 2019/12/13 9:56
 */
public interface UiButtonApiScannerProvider {

    String[] PAGE_ROUTE_VAR_ARRAY = new String[]{"${this.props.match.path}", "${this.props.path}", "${match.path}", "${path}"};
    char PAGE_ROUTE_DELIMITER = '/';
    char PERMISSION_CODE_DELIMITER = '.';
    char PERMISSION_ATTR_VALUE_INVALID_DELIMITER = '`';
    char PERMISSION_ATTR_VALUE_DELIMITER = '"';
    String[] EXCLUDE_COMPONENT_EXT_ARRAY = new String[]{".css", ".less", ".sass"};

    /**
     * 扫描LOV组件
     *
     * @param routerConfigFiles 路由文件集合
     * @param routeModelFiles   路由文件对应的model集合
     * @param routeDsFiles      路由文件对应的DS集合
     * @return LOV组件
     */
    List<UiComponent> scanLovComp(Map<String, Collection<RouteComponentFileVO>> routerConfigFiles,
                                  Map<String, Collection<RouteComponentFileVO>> routeModelFiles,
                                  Map<String, Collection<RouteComponentFileVO>> routeDsFiles);

    /**
     * 扫描多语言
     *
     * @param routeUiFiles 路由对应的UI文件
     * @param routeDsFiles 路由对应的DS文件
     * @return 多语言组件
     */
    List<UiComponent> scanTlComp(Map<String, Collection<RouteComponentFileVO>> routeUiFiles,
                                 Map<String, Collection<RouteComponentFileVO>> routeDsFiles);

    /**
     * 构造路由配置的组件入口绝对路径, 不带扩展名
     *
     * @param routeConfigFile 路由配置文件, 用于定位相对位置
     * @param componentConfig 配置在路由配置文件中的组件字符串
     * @param version         cli版本
     * @return 组件全路径
     */
    String buildRouteEntryPointComponentFileAbsolutePathWithoutExt(File routeConfigFile,
                                                                   String componentConfig,
                                                                   Integer version);

    /**
     * 根据路由文件获取model文件，带扩展名
     *
     * @param routeConfigFile 路由文件
     * @param modelFilePath   路由文件单个json对象的单个model，如models:["xxx"]或者models:[()=>import('xxx')]
     * @param version         cli版本
     * @return model文件path
     */
    String getModelFilePath(File routeConfigFile, String modelFilePath, Integer version);

    /**
     * 获取路由组件入口文件
     *
     * @param entryPointAbsolutePathWithoutExt 入口文件绝对路径, 不带扩展名
     *                                         算法:
     *                                         #1 此路径为目录, 入口文件则为其下的index.js
     *                                         #2 此路径不为目录, 则需要加上.js扩展名看看是否能找到同名的入口文件
     * @return 路由组件入口文件
     */
    File findRouteEntryPointComponentFile(String entryPointAbsolutePathWithoutExt);

    /**
     * 递归寻找入口文件的相对依赖
     * <p>
     * 仅仅寻找相独路径依赖, 绝对依赖可能是标准组件, 目前无法扫描
     * #1 不带扩展名的相对依赖, 则可能是`.js`, 也可能是`相对依赖组件目录/index.js`
     * #2 带扩展名的相对依赖, 则扩展名必须是`.js`
     * 此处又是通过{@link #findRouteEntryPointComponentFile(String)}寻找依赖组件的入口文件
     *
     * @param routeEntryPointFile 路由入口文件
     */
    List<File> findRouteEntryPointComponentDependencies(File routeEntryPointFile);

    /**
     * 构造导入组件的绝对路径, 不带扩展名
     * <p>
     * 限制: 只能导入相对路径依赖, 我们认为这部分为客户化文件
     *
     * @param routeEntryPointFile 路由入口文件, 用于定位相对位置
     * @param importLine          导入语句行, 例如: `import EmailForm from './EmailForm';`
     * @return 导入组件全路径
     */
    String buildImportFileAbsolutePath(File routeEntryPointFile,
                                       String importLine);

    /**
     * 分析路由之下的组件权限信息, 获取UiPermission
     * <p>
     * 思路: 读取成为一个大的字符串, 然后去除所有空格, 之后通过正则表达式截取
     *
     * @param routeComponentFiles       路由及组件映射关系列表
     *                                  #Key 页面根路由
     *                                  #Value 页面跟路由对应的组件文件列表
     * @param uiPermissionComponentAttr UI权限组件属性名, 默认`permissionList`
     * @return UI权限组件列表
     */
    List<UiComponent> scanButtonComp(Map<String, Collection<RouteComponentFileVO>> routeComponentFiles,
                                     String uiPermissionComponentAttr);
}
