package org.hzero.generator.service.impl;

import org.hzero.generator.scan.domain.Menu;
import org.hzero.generator.scan.domain.Prompt;
import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.UiPermissionApi;
import org.hzero.generator.scan.domain.repository.UiPermissionCompRepository;
import org.hzero.generator.scan.domain.vo.RouteComponentFileVO;
import org.hzero.generator.scan.domain.vo.ServiceRouteVO;
import org.hzero.generator.scan.infra.constant.Constants;
import org.hzero.generator.scan.mapper.UiScanMapper;
import org.hzero.generator.scan.provider.UiApiScannerProvider;
import org.hzero.generator.scan.provider.UiButtonApiScannerProvider;
import org.hzero.generator.scan.provider.UiFileScannerProvider;
import org.hzero.generator.service.IUiScanService;
import org.hzero.generator.util.SwitchDatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author fanghan.liu 2020/02/11 19:10
 */
@Service
public class UiScanServiceImpl implements IUiScanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiScanServiceImpl.class);

    /**
     * 路由配置文件名匹配前缀
     */
    @Value("${route.config.fileNamePrefix:routers}")
    private String routeConfigFileNamePrefix;
    /**
     * UI权限组件属性名
     */
    @Value("${ui.permission.ComponentAttr:permissionList}")
    private String uiPermissionComponentAttr;
    /**
     * 路由配置文件名匹配前缀
     */
    @Value("${route.config.fileNamePrefix:.*?Service.js}")
    private String uiButtonApiFileRegex;

    @Value("${route.config.serviceConfigFile:}")
    private String serviceConfigFile;

    @Value("${scan.tool.temporary:hzero_generator}")
    private String generator;

    @Value("${scan.tool.permission:hzero_platform}")
    private String platform;

    @Value("${scan.tool.service:hzero_admin}")
    private String admin;

    @Autowired
    private UiApiScannerProvider uiApiScannerProvider;

    @Autowired
    private UiButtonApiScannerProvider uiButtonApiScannerProvider;

    @Autowired
    private UiFileScannerProvider uiFileScannerProvider;

    @Autowired
    private UiScanMapper uiScanMapper;

    @Autowired
    private UiPermissionCompRepository uiPermissionCompRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    SwitchDatabaseUtils databaseUtils;

    @Override
    public boolean scanButton(String resourceDirPath, List<ServiceRouteVO> routes, Integer version) {
        // 获取资源目录对象
        File resourceDir = uiFileScannerProvider.fetchResourceDir(resourceDirPath);
        if (resourceDir == null) {
            return false;
        }
        // 获取路由配置文件列表 获取routers开头 .js结尾的文件
        Collection<File> routeConfigFiles = uiFileScannerProvider.fetchRouterFiles(resourceDir, this.routeConfigFileNamePrefix);
        if (CollectionUtils.isEmpty(routeConfigFiles)) {
            LOGGER.info("there is no route config files with name prefix {}, exit.", this.routeConfigFileNamePrefix);
            return false;
        }
        List<String> uiRoutes = new ArrayList<>();
        routes.forEach(route -> getRouterList(route, uiRoutes));
        // 路由对应的UI文件
        Map<String, Collection<RouteComponentFileVO>> routerUiFiles = new HashMap<>(16);
        // 路由对应的model文件，用于获取hzero lov
        Map<String, Collection<RouteComponentFileVO>> routeModelFiles = new HashMap<>(16);
        // 获取路由对应的UI文件以及Model文件
        uiFileScannerProvider.fetchRoutesAndModelFiles(routeConfigFiles, routerUiFiles, routeModelFiles, uiRoutes, version);

        // 扫描权限按钮
        List<UiComponent> comps = uiButtonApiScannerProvider.scanButtonComp(routerUiFiles, uiPermissionComponentAttr);
        uiPermissionCompRepository.rebuildComponent(dataSource, comps, Constants.CompType.BUTTON);
        return true;
    }

    @Override
    public boolean scanLov(String resourceDirPath, List<ServiceRouteVO> routes, Integer version) {
        // 获取资源目录对象
        File resourceDir = uiFileScannerProvider.fetchResourceDir(resourceDirPath);
        if (resourceDir == null) {
            return false;
        }
        // 获取路由配置文件列表 获取routers开头 .js结尾的文件
        Collection<File> routeConfigFiles = uiFileScannerProvider.fetchRouterFiles(resourceDir, this.routeConfigFileNamePrefix);
        if (CollectionUtils.isEmpty(routeConfigFiles)) {
            LOGGER.info("there is no route config files with name prefix {}, exit.", this.routeConfigFileNamePrefix);
            return false;
        }
        List<String> uiRoutes = new ArrayList<>();
        routes.forEach(route -> getRouterList(route, uiRoutes));
        // 路由对应的UI文件
        Map<String, Collection<RouteComponentFileVO>> routerUiFiles = new HashMap<>(16);
        // 路由对应的model文件，用于获取hzero lov
        Map<String, Collection<RouteComponentFileVO>> routeModelFiles = new HashMap<>(16);
        // 获取路由对应的UI文件以及Model文件
        uiFileScannerProvider.fetchRoutesAndModelFiles(routeConfigFiles, routerUiFiles, routeModelFiles, uiRoutes, version);
        // 获取路由对应的DS文件
        Map<String, Collection<RouteComponentFileVO>> routeDsFiles = uiFileScannerProvider.getRouteDsFile(routerUiFiles);

        // 扫描LOV
        List<UiComponent> comps = uiButtonApiScannerProvider.scanLovComp(routerUiFiles, routeModelFiles, routeDsFiles);

        // 打印临时扫描结果
        printParseResult(comps);
        uiPermissionCompRepository.rebuildComponent(dataSource, comps, Constants.CompType.LOV);
        return true;
    }

    @Override
    public boolean scanPrompt(String resourceDirPath, List<ServiceRouteVO> routes, Integer version) {
        // 获取资源目录对象
        File resourceDir = uiFileScannerProvider.fetchResourceDir(resourceDirPath);
        if (resourceDir == null) {
            return false;
        }
        // 获取路由配置文件列表 获取routers开头 .js结尾的文件
        Collection<File> routeConfigFiles = uiFileScannerProvider.fetchRouterFiles(resourceDir, this.routeConfigFileNamePrefix);
        if (CollectionUtils.isEmpty(routeConfigFiles)) {
            LOGGER.info("there is no route config files with name prefix {}, exit.", this.routeConfigFileNamePrefix);
            return false;
        }
        List<String> uiRoutes = new ArrayList<>();
        routes.forEach(route -> getRouterList(route, uiRoutes));
        // 路由对应的UI文件
        Map<String, Collection<RouteComponentFileVO>> routerUiFiles = new HashMap<>(16);
        // 路由对应的model文件
        Map<String, Collection<RouteComponentFileVO>> routeModelFiles = new HashMap<>(16);
        // 获取路由对应的UI文件以及Model文件
        uiFileScannerProvider.fetchRoutesAndModelFiles(routeConfigFiles, routerUiFiles, routeModelFiles, uiRoutes, version);
        // 获取路由对应的DS文件
        Map<String, Collection<RouteComponentFileVO>> routeDsFiles = uiFileScannerProvider.getRouteDsFile(routerUiFiles);

        // 扫描多语言
        List<UiComponent> comps = uiButtonApiScannerProvider.scanTlComp(routerUiFiles, routeDsFiles);

        // 打印临时扫描结果
        printParseResult(comps);
        uiPermissionCompRepository.rebuildComponent(dataSource, comps, Constants.CompType.PROMPT);
        return true;
    }

    @Override
    public boolean scanApi(String resourceDirPath, List<ServiceRouteVO> routes, Integer version) {
        // 获取资源目录对象
        File resourceDir = uiFileScannerProvider.fetchResourceDir(resourceDirPath);
        if (resourceDir == null) {
            return false;
        }
        // 获取路由配置文件列表 获取routers开头 .js结尾的文件
        Collection<File> routeConfigFiles = uiFileScannerProvider.fetchRouterFiles(resourceDir, this.routeConfigFileNamePrefix);
        if (CollectionUtils.isEmpty(routeConfigFiles)) {
            LOGGER.info("there is no route config files with name prefix {}, exit.", this.routeConfigFileNamePrefix);
            return false;
        }
        List<String> uiRoutes = new ArrayList<>();
        routes.forEach(route -> getRouterList(route, uiRoutes));
        // 路由对应的UI文件
        Map<String, Collection<RouteComponentFileVO>> routerUiFiles = new HashMap<>(16);
        // 路由对应的model文件
        Map<String, Collection<RouteComponentFileVO>> routeModelFiles = new HashMap<>(16);
        // 获取路由对应的UI文件以及Model文件
        uiFileScannerProvider.fetchRoutesAndModelFiles(routeConfigFiles, routerUiFiles, routeModelFiles, uiRoutes, version);
        // 获取路由对应的DS文件
        Map<String, Collection<RouteComponentFileVO>> routeDsFiles = uiFileScannerProvider.getRouteDsFile(routerUiFiles);
        // 获取Models文件对应的Services.js文件
        Map<String, Collection<RouteComponentFileVO>> routeServiceFiles = uiFileScannerProvider.getRouteServiceFile(routeModelFiles);
        // 开始解析api
        List<UiPermissionApi> uiPermissionApiList = new ArrayList<>();
        routeServiceFiles.forEach((pageRoute, serviceFiles) -> serviceFiles.forEach(file -> uiPermissionApiList
                .addAll(uiApiScannerProvider.scanApi(file.getComponentFile(), pageRoute))));
        routeDsFiles.forEach((pageRoute, dsFiles) -> dsFiles.forEach(file -> uiPermissionApiList
                .addAll(uiApiScannerProvider.scanApi(file.getComponentFile(), pageRoute))));

        // 加载服务配置文件
        String serviceConfig = uiFileScannerProvider.loadServiceConfig(serviceConfigFile, resourceDirPath);
        // 解析API的服务名
        uiApiScannerProvider.parseApiService(uiPermissionApiList, serviceConfig);
        uiPermissionCompRepository.rebuildApi(dataSource, uiPermissionApiList);
        return true;
    }

    @Override
    public List<ServiceRouteVO> getRoutersDetail(String level) {
        databaseUtils.switchDatabase(platform);
        List<ServiceRouteVO> result = new ArrayList<>();
        List<Menu> menus = uiScanMapper.listMenu(level);
        menus.forEach(menu -> result.add(menuToServiceRouteVO(menu)));
        return result;
    }

    @Override
    public List<String> listButton(List<ServiceRouteVO> routes) {
        if(CollectionUtils.isEmpty(routes)){
            return Collections.emptyList();
        }
        databaseUtils.switchDatabase(generator);
        List<String> uiRoutes = new ArrayList<>();
        routes.forEach(item -> getRouterList(item, uiRoutes));
        return uiRoutes;
    }

    @Override
    public List<UiComponent> listLov(List<ServiceRouteVO> routes) {
        if(CollectionUtils.isEmpty(routes)){
            return Collections.emptyList();
        }
        databaseUtils.switchDatabase(generator);
        List<String> uiRoutes = new ArrayList<>();
        routes.forEach(item -> getRouterList(item, uiRoutes));
        return uiScanMapper.listLovByRoutes(uiRoutes);
    }

    @Override
    public List<UiComponent> listPrompt(List<ServiceRouteVO> routes) {
        if(CollectionUtils.isEmpty(routes)){
            return Collections.emptyList();
        }
        databaseUtils.switchDatabase(generator);
        List<String> uiRoutes = new ArrayList<>();
        routes.forEach(item -> getRouterList(item, uiRoutes));
        return uiScanMapper.listPromptByRoutes(uiRoutes);
    }

    @Override
    public List<UiComponent> listAllPrompt() {
        databaseUtils.switchDatabase(generator);
        return uiScanMapper.selectAllUiPrompt();
    }

    @Override
    public List<UiComponent> listNewPrompt(List<UiComponent> uiComponents) {
        databaseUtils.switchDatabase(platform);
        List<UiComponent> result = new ArrayList<>();
        Map<String, List<UiComponent>> uiPromptMap = uiComponents.stream().collect(Collectors.groupingBy(UiComponent::getPromptKey));
        uiPromptMap.forEach((promptKey, uiPrompts) -> {
            List<Prompt> prompts = uiScanMapper.selectPromptByKey(promptKey);
            if(CollectionUtils.isEmpty(prompts)){
                result.addAll(uiPrompts);
            }else {
                List<UiComponent> newPrompt = uiPrompts.stream()
                        .filter(uiPrompt -> prompts.stream()
                                .noneMatch(prompt -> prompt.getPromptCode().equals(uiPrompt.getPromptCode())))
                        .collect(Collectors.toList());
                result.addAll(newPrompt);
            }
        });
        return result;
    }

    private void printParseResult(List<UiComponent> comps) {
        // 临时结果输出
        LOGGER.info("==============================ui permission result ({})==============================", comps.size());
        Map<String, List<UiComponent>> rootPageRouteStatistics = comps.stream().collect(Collectors.groupingBy(UiComponent::getUiRoute));
        rootPageRouteStatistics.forEach((k, v) -> {
            LOGGER.info("====route [{}], size [{}]====", k, v.size());
            v.forEach(item -> LOGGER.info(item.toString()));
        });
    }

    private void getRouterList(ServiceRouteVO serviceRouteVO, List<String> uiRoutes) {
        List<ServiceRouteVO> children = serviceRouteVO.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            if (!StringUtils.isEmpty(serviceRouteVO.getField())) {
                uiRoutes.add(serviceRouteVO.getField());
            }
        } else {
            children.forEach(item -> getRouterList(item, uiRoutes));
        }
    }

    private ServiceRouteVO menuToServiceRouteVO(Menu menu) {
        ServiceRouteVO vo = new ServiceRouteVO(menu.getId().toString(), menu.getName(), menu.getRoute(), null);
        List<ServiceRouteVO> childrenMenus = new ArrayList<>();
        if (!CollectionUtils.isEmpty(menu.getChildrenMenu())) {
            menu.getChildrenMenu().forEach(children -> {
                childrenMenus.add(menuToServiceRouteVO(children));
            });
            vo.setChildren(childrenMenus);
        }
        return vo;
    }
}
