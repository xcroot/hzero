package org.hzero.generator.scan.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.hzero.generator.scan.domain.vo.RouteComponentFileVO;
import org.hzero.generator.scan.domain.vo.ServiceRouteVO;
import org.hzero.generator.scan.infra.builder.ObjectMapperBuilder;
import org.hzero.generator.scan.infra.constant.Constants;
import org.hzero.generator.scan.infra.util.FileUtil;
import org.hzero.generator.scan.provider.UiButtonApiScannerProvider;
import org.hzero.generator.scan.provider.UiFileScannerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description
 *
 * @author fanghan.liu 2020/01/17 10:14
 */
@Component
public class UiFileScannerProviderImpl implements UiFileScannerProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiFileScannerProviderImpl.class);

    private static final Pattern DS_FILE = Pattern.compile("import.*?from'.*?stores/(.*?)(\\.js)?';");

    private static final Pattern SERVICE_FILE = Pattern.compile("import\\{.+?}from['|\"](.+?)services/(.+?)['|\"]");

    private static final Pattern ROUTE = Pattern.compile("(\\(\\)\\s=>\\simport\\('.+?/[routesmdl]+/(.+?)'\\))");

    private UiButtonApiScannerProvider uiButtonApiScannerProvider;

    public UiFileScannerProviderImpl(UiButtonApiScannerProvider uiButtonApiScannerProvider) {
        this.uiButtonApiScannerProvider = uiButtonApiScannerProvider;
    }

    @Override
    public File fetchResourceDir(String resourceDirPath) {
        File resourceDir = new File(resourceDirPath);

        if (!resourceDir.exists() || !resourceDir.isDirectory()) {
            return null;
        }
        return resourceDir;
    }

    @Override
    public Collection<File> fetchRouterFiles(File resourceDir, String routeConfigFileNamePrefix) {
        Collection<File> routeConfigFiles = FileUtils.listFiles(resourceDir, FileFilterUtils.prefixFileFilter(routeConfigFileNamePrefix), FileFilterUtils.directoryFileFilter());

        Iterator<File> fileIterator = routeConfigFiles.iterator();
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            LOGGER.info("===================route config files===================");
            LOGGER.info("{}", file.getAbsolutePath());
            LOGGER.info("========================================================");
            if (!file.getAbsolutePath().endsWith(".js")) {
                fileIterator.remove();
            }
        }

        return routeConfigFiles;
    }

    @Override
    public void fetchRoutesAndModelFiles(Collection<File> routerConfigFiles,
                                         Map<String, Collection<RouteComponentFileVO>> routerUiFiles,
                                         Map<String, Collection<RouteComponentFileVO>> routerModelFiles,
                                         List<String> uiRoutes,
                                         Integer version) {
        routerConfigFiles.forEach(routeConfigFile -> {

            // 通过路由配置文件，找到路由对应的页面所在的js文件 <pageRoute,RouteConponentFileVO>
            Map<String, Collection<RouteComponentFileVO>> currentRouteComponentFiles
                    = this.analyzeRouterFile(routeConfigFile, routerModelFiles, uiRoutes, version);

            // 当前根路由下的组件映射信息列表
            if (CollectionUtils.isEmpty(currentRouteComponentFiles)) {
                LOGGER.info("there is no route component files with route {} and component file, exit current loop.",
                        routeConfigFile.getAbsolutePath());
            } else {
                routerUiFiles.putAll(currentRouteComponentFiles);
            }
        });
    }

    @Override
    public Map<String, Collection<RouteComponentFileVO>> getRouteDsFile(Map<String, Collection<RouteComponentFileVO>> routerFiles) {
        Map<String, Collection<RouteComponentFileVO>> dsFileMap = new HashMap<>(16);
        routerFiles.forEach((pageRoute, routeFiles) -> {
            List<RouteComponentFileVO> dsFiles = new ArrayList<>();
            routeFiles.forEach(fileVO -> {
                File file = fileVO.getComponentFile();
                String fileContent = FileUtil.fileToStringWithoutSpace(file);
                // 匹配引入的DS文件 由于扫路由的时候已经把routes下的DS文件扫出来了，所以这里只扫描src/stores下的ds文件
                Matcher dsFilePosition = DS_FILE.matcher(fileContent);
                while (dsFilePosition.find()) {
                    // 第一部分@开头，替换为src
                    String fileName = dsFilePosition.group(1);
                    String absolutePath = file.getAbsolutePath();
                    absolutePath = absolutePath.substring(0, absolutePath.indexOf("src"));
                    File dsFile = new File(absolutePath + "src" + File.separator + "stores" + File.separator + fileName + ".js");
                    if (dsFile.exists()) {
                        dsFiles.add(new RouteComponentFileVO(pageRoute, dsFile));
                    }
                }
            });
            dsFileMap.put(pageRoute, dsFiles);
        });
        return dsFileMap;
    }

    @Override
    public Map<String, Collection<RouteComponentFileVO>> getRouteServiceFile(Map<String, Collection<RouteComponentFileVO>> routeModelFiles) {
        Map<String, Collection<RouteComponentFileVO>> serviceFileMap = new HashMap<>(16);
        routeModelFiles.forEach((pageRoute, modelFiles) -> {
            List<RouteComponentFileVO> serviceFiles = new ArrayList<>();
            modelFiles.forEach(modelFileVO -> {
                File file = modelFileVO.getComponentFile();
                String fileContent = FileUtil.fileToStringWithoutSpace(file);
                Matcher serviceFilePosition = SERVICE_FILE.matcher(fileContent);
                while (serviceFilePosition.find()) {
                    String fileName = serviceFilePosition.group(2);
                    String absolutePath = file.getAbsolutePath();
                    absolutePath = absolutePath.substring(0, absolutePath.indexOf("src"));
                    File serviceFile = new File(absolutePath + "src" + File.separator + "services" + File.separator + fileName + ".js");
                    if (serviceFile.exists()) {
                        serviceFiles.add(new RouteComponentFileVO(pageRoute, serviceFile));
                    }
                }
            });
            serviceFileMap.put(pageRoute, serviceFiles);
        });
        return serviceFileMap;
    }

    /**
     * 分析路由组件文件
     *
     * @param routeConfigFile 路由配置文件
     * @return UI文件
     */
    private Map<String, Collection<RouteComponentFileVO>> analyzeRouterFile(File routeConfigFile,
                                                                            Map<String, Collection<RouteComponentFileVO>> routeModelFiles,
                                                                            List<String> uiRoutes,
                                                                            Integer version) {
        Map<String, Collection<RouteComponentFileVO>> routeComponentFiles = new HashMap<>(16);

        // 读取路由配置文件
        String routeConfig;
        try {
            routeConfig = FileUtils.readFileToString(routeConfigFile, StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            LOGGER.error("read route config file {} to string with exception.", routeConfigFile.getAbsolutePath());
            LOGGER.error("read route config file to string with exception: ", ioe);
            throw new IllegalStateException(ioe);
        }
        if (StringUtils.isEmpty(routeConfig)) {
            LOGGER.info("there is no route info in route config file {}.", routeConfigFile.getAbsolutePath());
            return routeComponentFiles;
        }

        // 处理路由配置
        routeConfig = StringUtils.trimWhitespace(routeConfig);
        // 获取路由JSON体
        // routeConfig = routeConfig.replaceAll("module.exports\\s=", "");
        routeConfig = StringUtils.trimTrailingCharacter(routeConfig, ';');
        routeConfig = StringUtils.trimWhitespace(routeConfig);
        routeConfig = getJsonContent(routeConfig);
        routeConfig = covertToStandardJson(routeConfig);
        // 转化为JSON处理
        ObjectMapper objectMapper = ObjectMapperBuilder.buildObjectMapper();
        try {
            LOGGER.debug(routeConfig);
            JsonNode rootNode = objectMapper.readTree(routeConfig);
            if (!rootNode.isArray()) {
                LOGGER.error("route info root node must be an array.");
                return null;
            }
            rootNode.iterator().forEachRemaining(currentNode -> {
                // 获取顶层父路由
                JsonNode rootPageRouteNode = currentNode.get("path");
                String rootPageRoute = rootPageRouteNode.textValue();

                if (rootPageRoute == null) {
                    throw new IllegalArgumentException("illegal route config format for node" + currentNode.toString());
                }
                // 顶层路由不在选择的路由中
                for (String uiRoute : uiRoutes) {
                    if (uiRoute.contains(rootPageRoute)) {
                        // 获取顶层路由与组件文件之间的映射关系
                        this.fetchRouteComponentFilesRecursive(currentNode, rootPageRoute, routeComponentFiles, routeConfigFile, routeModelFiles, version);
                    }
                }
            });
        } catch (IOException ioe) {
            LOGGER.error("parse route info(from route config file {}) to json with exception.", routeConfigFile.getAbsolutePath());
            LOGGER.error("parse route info to json with exception: ", ioe);
        }

        // 记录日志
        routeComponentFiles.forEach((key, value) -> {
            LOGGER.info("===================route {} with component files===================", key);
            value.forEach(item -> LOGGER.info("{}", item.getComponentFile().getName()));
            LOGGER.info("===========================================================");
        });

        return routeComponentFiles;
    }

    /**
     * 递归获取路由组件文件
     *
     * @param jsonNode            当前json节点
     * @param rootPageRoute       顶层路由
     * @param routeComponentFiles 路由组件文件列表 输出参数
     * @param routeConfigFile     路由配置文件
     */
    private void fetchRouteComponentFilesRecursive(JsonNode jsonNode,
                                                   String rootPageRoute,
                                                   Map<String, Collection<RouteComponentFileVO>> routeComponentFiles,
                                                   File routeConfigFile,
                                                   Map<String, Collection<RouteComponentFileVO>> routeModelFiles,
                                                   Integer version) {
        if (jsonNode == null || routeComponentFiles == null) {
            return;
        }

        // 如果是数组, 则直接继续递归
        if (jsonNode.isArray()) {
            jsonNode.iterator().forEachRemaining(item ->
                    this.fetchRouteComponentFilesRecursive(item, rootPageRoute, routeComponentFiles, routeConfigFile, routeModelFiles, version));
        }

        // 如果是对象, 则处理
        if (jsonNode.isObject()) {
            // 组件判断
            JsonNode componentJsonNode = jsonNode.get("component");
            if (componentJsonNode != null && componentJsonNode.isTextual()) {
                // 走到这里该页面一定是路由，我们按照路由进行解析
                JsonNode pageRouteNode = jsonNode.get("path");
                String pageRoute = pageRouteNode.textValue();
                if (pageRoute == null) {
                    throw new IllegalArgumentException("illegal route config format for " + jsonNode.toString());
                }

                String component = componentJsonNode.textValue();
                if (component == null) {
                    throw new IllegalArgumentException("illegal route config format for " + jsonNode.toString());
                }
                if (component.contains("/")) {
                    component = component.replace("/", File.separator);
                }
                List<RouteComponentFileVO> RouteComponentFileVOS = new ArrayList<>();
                JsonNode models = jsonNode.get("models");
                if (models != null) {
                    Iterator<JsonNode> iterator = models.iterator();
                    if (iterator.hasNext()) {
                        String modelPath = iterator.next().textValue();
                        // 获取路由对应的models文件
                        File modelFile = new File(this.uiButtonApiScannerProvider.getModelFilePath(routeConfigFile, modelPath, version));
                        if (modelFile.exists()) {
                            RouteComponentFileVOS.add(new RouteComponentFileVO(pageRoute, modelFile));
                        }
                    }
                    routeModelFiles.put(rootPageRoute, RouteComponentFileVOS);
                }
                // 解析路由文件

                String routeEntryPointComponentFileAbsolutePathWithoutExt =
                        this.uiButtonApiScannerProvider.buildRouteEntryPointComponentFileAbsolutePathWithoutExt(routeConfigFile, component, version);
                File routeEntryPointFile = this.uiButtonApiScannerProvider.findRouteEntryPointComponentFile(routeEntryPointComponentFileAbsolutePathWithoutExt);

                if (routeEntryPointFile != null) {
                    // 如果存在入口文件, 则继续执行
                    List<File> routeFileList = this.uiButtonApiScannerProvider.findRouteEntryPointComponentDependencies(routeEntryPointFile);
                    routeFileList.add(routeEntryPointFile);

                    // 构造路由及组件映射关系列表
                    if (!CollectionUtils.isEmpty(routeFileList)) {
                        Collection<RouteComponentFileVO> routeComponentFileVOs = new ArrayList<>();
                        routeFileList.forEach(item -> {
                            RouteComponentFileVO routeComponentFileVO = new RouteComponentFileVO(pageRoute, item);
                            routeComponentFileVOs.add(routeComponentFileVO);
                        });
                        // 合并
                        Collection<RouteComponentFileVO> hadExists = routeComponentFiles.getOrDefault(rootPageRoute, new ArrayList<>());
                        hadExists.addAll(routeComponentFileVOs);
                        routeComponentFiles.put(rootPageRoute, hadExists);
                    }
                }
            }

            // 子组件判断
            JsonNode componentArrayJsonNode = jsonNode.get("components");
            if (componentArrayJsonNode != null && componentArrayJsonNode.isArray()) {
                componentArrayJsonNode.iterator().forEachRemaining(item -> {
                    this.fetchRouteComponentFilesRecursive(item, rootPageRoute, routeComponentFiles, routeConfigFile, routeModelFiles, version);
                });
            }
        }
    }

    @Override
    public String loadServiceConfig(String configFilePath, String resourceDirPath) {
        if (StringUtils.isEmpty(configFilePath)) {
            Collection<File> routeConfigFiles = FileUtils.listFiles(new File(resourceDirPath), FileFilterUtils.prefixFileFilter("apiConfig.js"), FileFilterUtils.directoryFileFilter());
            if (CollectionUtils.isEmpty(routeConfigFiles)) {
                LOGGER.error("Could not find the file named apiConfig.js");
                return org.apache.commons.lang3.StringUtils.EMPTY;
            }
            configFilePath = routeConfigFiles.iterator().next().getPath();
        }
        File file = FileUtils.getFile(configFilePath);
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            LOGGER.error("error load file, path: {}", file.getPath());
        }
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }

    private ServiceRouteVO JsonNodeToRouteVO(JsonNode jsonNode) {
        ServiceRouteVO result = new ServiceRouteVO(UUID.randomUUID().toString(), jsonNode.get("path").textValue(), Constants.UI_ROUTE, new ArrayList<>());
        JsonNode childNode = jsonNode.get("components");
        if (childNode != null) {
            childNode.iterator().forEachRemaining(node -> result.getChildren().add(JsonNodeToRouteVO(node)));
        }
        return result;
    }

    private String covertToStandardJson(String config) {
        Matcher matcher = ROUTE.matcher(config);
        while (matcher.find()) {
            String value = matcher.group(1);
            String replace = matcher.group(2);
            config = config.replace(value, "'" + replace + "'");
        }
        return config;
    }

    private String getJsonContent(String fileContent) {
        String content = fileContent.substring(fileContent.indexOf("= ["));
        char[] chars = content.toCharArray();
        int left = 0;
        int right = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '[') {
                left++;
            }
            if (chars[i] == ']') {
                right++;
            }
            if (left == right && left > 0) {
                return content.substring(2, i + 1);
            }
        }
        return null;
    }

}
