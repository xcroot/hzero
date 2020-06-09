package org.hzero.generator.scan.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.vo.RouteComponentFileVO;
import org.hzero.generator.scan.infra.builder.ObjectMapperBuilder;
import org.hzero.generator.scan.infra.enums.CliVersion;
import org.hzero.generator.scan.infra.util.FileUtil;
import org.hzero.generator.scan.provider.UiButtonApiScannerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jianbo.li
 * @date 2019/12/13 10:14
 */
@Component
public class UiButtonApiScannerProviderImpl implements UiButtonApiScannerProvider {

    private static final Pattern HZERO_LOV_VIEW_CODE_LABEL = Pattern.compile("<Lov(.*?)code=['|\"]([A-z._]*?)['|\"](.+?)/>");

    private static final Pattern HZERO_LOV_VIEW_CODE_LABEL_OBJECT = Pattern.compile("<Lov(.*?)code=\\{(.*?)['|\"]([A-z._]*?)['|\"]:['|\"]([A-z._]*?)['|\"]}");

    private static final Pattern HZERO_LOV_CODE_SINGLE = Pattern.compile("call\\(query[a-zA-Z0-9]*IdpValue,['|\"]([A-Z._]+)['|\"]");

    private static final Pattern HZERO_LOV_CODE_OBJECT = Pattern.compile("call\\(query[a-zA-Z0-9]*IdpValue,\\{(.+?)}");

    private static final Pattern HZERO_LOV_CODE_OBJECT_CODES = Pattern.compile("(.+?):['|\"](.+?)['|\"]");

    private static final Pattern HZERO_LOV_CODE_VARIABLE = Pattern.compile("call\\(query[a-zA-Z]*IdpValue,([a-zA-Z0-9]+)\\)");

    private static final Pattern HZERO_LOV_CODE_METHOD_SINGLE = Pattern.compile("query[a-zA-Z]*IdpValue\\(['|\"]([A-Z._]+)['|\"]\\)");

    private static final Pattern HZERO_LOV_CODE_PAYLOAD = Pattern.compile("(payload:)?\\{(.*?)lovCode:([^}]*?)['|\"]([A-Z._]*?)['|\"][:]?(['|\"]([A-Z._]*?)['|\"])?");

    private static final Pattern HZERO_LOV_CODE_PAYLOAD_VARIABLE = Pattern.compile("payload:\\{(lovCode[s]?)[:]?([A-z]*?)[,'\"]}");

    private static final Pattern C7N_LOV_CODE = Pattern.compile("lookupCode:['|\"]([A-Z._]*?)['|\"]");

    private static final Pattern MUTL_LANGUANE = Pattern.compile("intl\\.get\\(['|\"]([A-z.]*?)['|\"]\\)\\.d\\(['|\"](.*?)['|\"]\\)");

    private static final String LOV = "lov";

    private static final String LOV_CODES = "lovCodes";

    private static final String PROMPT = "prompt";

    /**
     * 日志信息
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UiButtonApiScannerProviderImpl.class);

    @Override
    public List<UiComponent> scanLovComp(Map<String, Collection<RouteComponentFileVO>> routerConfigFiles,
                                         Map<String, Collection<RouteComponentFileVO>> routeModelFiles,
                                         Map<String, Collection<RouteComponentFileVO>> routeDsFiles) {
        List<UiComponent> lovComponents = new ArrayList<>();
        routerConfigFiles.forEach((pageRoute, files) -> files.forEach(file -> {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("==================start analyze lov, path : routes/.. route : {}==================", pageRoute);
            }
            // 扫描HZERO UI的值集视图
            lovComponents.addAll(this.analyzeLovView(pageRoute, file.getComponentFile()));
            // 扫描HZERO UI的值集
            lovComponents.addAll(this.analyzeLov(pageRoute, file.getComponentFile()));
        }));
        routeModelFiles.forEach((pageRoute, files) -> files.forEach(file -> {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("==================start analyze lov, path : model/.. route : {}==================", pageRoute);
            }
            // 扫描HZERO UI的值集
            lovComponents.addAll(this.analyzeLov(pageRoute, file.getComponentFile()));
        }));
        routeDsFiles.forEach((pageRoute, files) -> files.forEach(file -> {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("==================start analyze lov, path : stores/.. route : {}==================", pageRoute);
            }
            // 扫描C7N的值集、视图
            lovComponents.addAll(this.analyzeLov(pageRoute, file.getComponentFile()));
            lovComponents.addAll(this.analyzeLovView(pageRoute, file.getComponentFile()));
        }));
        return lovComponents;
    }

    @Override
    public List<UiComponent> scanTlComp(Map<String, Collection<RouteComponentFileVO>> routeUiFiles, Map<String, Collection<RouteComponentFileVO>> routeDsFiles) {
        List<UiComponent> tlComponents = new ArrayList<>();
        routeUiFiles.forEach((pageRoute, files) -> files.forEach(file -> {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("==================start analyze tl, path routes/.. route : {}", pageRoute);
            }
            tlComponents.addAll(this.analyzeTl(pageRoute, file.getComponentFile()));
        }));
        routeDsFiles.forEach((pageRoute, files) -> files.forEach(file -> {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("==================start analyze tl, path stores/.. route : {}", pageRoute);
            }
            tlComponents.addAll(this.analyzeTl(pageRoute, file.getComponentFile()));
        }));
        return tlComponents;
    }

    private List<UiComponent> analyzeLovView(String pageRoute, File file) {
        String fileContent = FileUtil.fileToStringWithoutSpace(file);
        List<UiComponent> uiComponents = new ArrayList<>();
        // <Lov code='CODE' HZERO UI写法
        Matcher label = HZERO_LOV_VIEW_CODE_LABEL.matcher(fileContent);
        while (label.find()) {
            String code = label.group(2);
            if (!StringUtils.isEmpty(code)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
            }
        }
        // <Lov code={ xxx? 'CODE1':'CODE2'} 对象写法
        Matcher labelObject = HZERO_LOV_VIEW_CODE_LABEL_OBJECT.matcher(fileContent);
        while (labelObject.find()) {
            String code1 = labelObject.group(3);
            String code2 = labelObject.group(4);
            if (!StringUtils.isEmpty(code1)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code1));
            }
            if (!StringUtils.isEmpty(code2)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code2));
            }
        }
        // lovCode:'CODE' C7N写法
        Matcher payloadCode = HZERO_LOV_CODE_PAYLOAD.matcher(fileContent);
        while (payloadCode.find()) {
            String code1 = payloadCode.group(4);
            String code2 = payloadCode.group(6);
            if (!StringUtils.isEmpty(code2)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code1));
            }
            if (!StringUtils.isEmpty(code2)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code2));
            }
        }
        return uiComponents;
    }

    /**
     * 分析LOV 穷举方式
     *
     * @param pageRoute 路由
     * @param file      文件
     * @return lov组件
     */
    private List<UiComponent> analyzeLov(String pageRoute, File file) {
        String fileContent = FileUtil.fileToStringWithoutSpace(file);
        List<UiComponent> uiComponents = new ArrayList<>();
        // call(query... , "CODE") 单个值集
        Matcher single = HZERO_LOV_CODE_SINGLE.matcher(fileContent);
        while (single.find()) {
            String code = single.group(1);
            if (!StringUtils.isEmpty(code)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
            }
        }
        // call(query... , {code:"CODE",...}) 对象方式
        Matcher object = HZERO_LOV_CODE_OBJECT.matcher(fileContent);
        while (object.find()) {
            String codes = object.group(1);
            Matcher objectCodesMatcher = HZERO_LOV_CODE_OBJECT_CODES.matcher(codes);
            while (objectCodesMatcher.find()) {
                String code = objectCodesMatcher.group(2);
                if (!StringUtils.isEmpty(code)) {
                    uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
                }
            }
        }
        // call(query..., params) 变量方式
        Matcher variable = HZERO_LOV_CODE_VARIABLE.matcher(fileContent);
        while (variable.find()) {
            String codeName = variable.group(1);
            // const xxx = "CODE" 或者 const xxx = {xxx:"CODE", ...};
            Pattern constVariable = Pattern.compile("const" + codeName + "=(.+?);");
            Matcher codesMatcher = constVariable.matcher(fileContent);
            while (codesMatcher.find()) {
                String codes = codesMatcher.group(1);
                if (!StringUtils.isEmpty(codes)) {
                    codes = StringUtils.replace(codes, "'", "\"");
                    if (codes.startsWith("{")) {
                        // 对象方式
                        Matcher objectCodesMatcher = HZERO_LOV_CODE_OBJECT_CODES.matcher(codes);
                        while (objectCodesMatcher.find()) {
                            String code = objectCodesMatcher.group(2);
                            if (!StringUtils.isEmpty(code)) {
                                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
                            }
                        }
                    } else if (!StringUtils.isEmpty(codes)) {
                        // 单个code
                        uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(StringUtils.replace(codes, "\"", "")));
                    }
                }
            }
        }
        // query...("CODE") 直接调方法
        Matcher method = HZERO_LOV_CODE_METHOD_SINGLE.matcher(fileContent);
        while (method.find()) {
            String code = method.group(1);
            if (!StringUtils.isEmpty(code)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
            }
        }
        // payload:{...lovCode:'CODE' 写法 或者 xxx?'CODE1':'CODE2'
        Matcher payloadCode = HZERO_LOV_CODE_PAYLOAD.matcher(fileContent);
        while (payloadCode.find()) {
            String payload = payloadCode.group(1);
            String code1 = payloadCode.group(4);
            String code2 = payloadCode.group(6);
            if (StringUtils.isEmpty(payload)) {
                // 没payload，代表是DS文件，是c7n的视图
                if (!StringUtils.isEmpty(code1)) {
                    uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code1));
                }
                if (!StringUtils.isEmpty(code2)) {
                    uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code2));
                }
            } else {
                if (!StringUtils.isEmpty(code1)) {
                    uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code1));
                }
                if (!StringUtils.isEmpty(code2)) {
                    uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code2));
                }
            }
        }
        // payload:{ lovCode:xxx 变量
        Matcher payloadVariable = HZERO_LOV_CODE_PAYLOAD_VARIABLE.matcher(fileContent);
        while (payloadVariable.find()) {
            String lovCodes = payloadVariable.group(1);
            if (!StringUtils.isEmpty(lovCodes) && LOV_CODES.equals(lovCodes)) {
                // 这里特殊处理 payload:{lovCodes}，具体指还需要扫描const lovCodes
                Pattern constVariable = Pattern.compile("const" + LOV_CODES + "=(.+?);");
                Matcher codesMatcher = constVariable.matcher(fileContent);
                while (codesMatcher.find()) {
                    String codes = codesMatcher.group(1);
                    if (!StringUtils.isEmpty(codes)) {
                        Matcher objectCodesMatcher = HZERO_LOV_CODE_OBJECT_CODES.matcher(codes);
                        while (objectCodesMatcher.find()) {
                            String code = objectCodesMatcher.group(2);
                            if (!StringUtils.isEmpty(code)) {
                                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
                            }
                        }
                    }
                }
            } else {
                String lovCodeVariable = payloadVariable.group(2);
                if (!StringUtils.isEmpty(lovCodeVariable)) {
                    // 扫描const 变量 = 'CODE';
                    Pattern lovCode = Pattern.compile("const" + lovCodeVariable + "=['|\"]([A-z._]*?)['|\"]");
                    Matcher codeMatcher = lovCode.matcher(fileContent);
                    while (codeMatcher.find()) {
                        String code = codeMatcher.group(1);
                        if (!StringUtils.isEmpty(code)) {
                            uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
                        }
                    }
                }
            }
        }
        // lookupCode:'CODE' 写法
        Matcher lookupCode = C7N_LOV_CODE.matcher(fileContent);
        while (lookupCode.find()) {
            String code = lookupCode.group(1);
            if (!StringUtils.isEmpty(code)) {
                uiComponents.add(new UiComponent().setCompType(LOV).setUiRoute(pageRoute).setCompCode(code));
            }
        }
        return uiComponents;
    }

    private List<UiComponent> analyzeTl(String pageRoute, File file) {
        String fileContent = FileUtil.fileToStringWithoutSpace(file);
        List<UiComponent> uiComponents = new ArrayList<>();
        Matcher tlMatcher = MUTL_LANGUANE.matcher(fileContent);
        while (tlMatcher.find()) {
            String code = tlMatcher.group(1);
            String description = tlMatcher.group(2);
            if (!StringUtils.isEmpty(code)) {
                uiComponents.add(new UiComponent().setCompType(PROMPT).setCompCode(code).setUiRoute(pageRoute).setDescription(description));
            }
        }
        return uiComponents;
    }

    @Override
    public String buildRouteEntryPointComponentFileAbsolutePathWithoutExt(File routeConfigFile, String componentConfig, Integer version) {
        // 寻找入口文件
        CliVersion cliVersion = CliVersion.valueOf2(version);
        switch (cliVersion) {
            case ZERO:
                return routeConfigFile.getParentFile().getParent() + File.separator +
                        "src" + File.separator +
                        "routes" + File.separator +
                        componentConfig;
            case ONE:
                return routeConfigFile.getParentFile().getParent() + File.separator +
                        "routes" + File.separator +
                        componentConfig;
            default:
                return org.apache.commons.lang3.StringUtils.EMPTY;
        }

    }

    @Override
    public String getModelFilePath(File routeConfigFile, String modelFilePath, Integer version) {
        // 寻找入口文件
        CliVersion cliVersion = CliVersion.valueOf2(version);
        switch (cliVersion) {
            case ZERO:
                return routeConfigFile.getParentFile().getParent() + File.separator +
                        "src" + File.separator +
                        "models" + File.separator +
                        modelFilePath + ".js";
            case ONE:
                return routeConfigFile.getParentFile().getParent() + File.separator +
                        "models" + File.separator +
                        modelFilePath + ".js";
            default:
                return org.apache.commons.lang3.StringUtils.EMPTY;
        }
    }

    @Override
    public File findRouteEntryPointComponentFile(String entryPointAbsolutePathWithoutExt) {
        File routeEntryPointComponentDir = new File(entryPointAbsolutePathWithoutExt);
        if (routeEntryPointComponentDir.exists() && routeEntryPointComponentDir.isDirectory()) {
            // #1 默认路由组件绝度路径为一个目录, 则需要寻找其下的`index.js`
            Collection<File> fileCollection = FileUtils.listFiles(routeEntryPointComponentDir, FileFilterUtils.nameFileFilter("index.js"), null);
            if (!CollectionUtils.isEmpty(fileCollection)) {
                for (File entryPoint : fileCollection) {
                    LOGGER.info("route component entry point file is {}.", entryPoint.getAbsolutePath());
                    return entryPoint;
                }
            }
        }
        // #2 否则, 需要拼接`.js`到组件之后, 寻找同名`.js`文件
        File routeEntryPointFile = new File(entryPointAbsolutePathWithoutExt + ".js");
        if (routeEntryPointFile.exists() && routeEntryPointFile.isFile()) {
            LOGGER.info("route component entry point file is {}.", routeEntryPointFile.getAbsolutePath());
            return routeEntryPointFile;
        }
        // #3 如果均没有找到, 则抛出异常
        LOGGER.info("cannot find an entry point file for route component {}.", entryPointAbsolutePathWithoutExt);
        return null;
    }

    @Override
    public List<File> findRouteEntryPointComponentDependencies(File routeEntryPointFile) {
        List<File> routeEntryPointDependencies = new ArrayList<>(16);
        // 递归寻找依赖组件
        this.findRouteEntryPointComponentDependenciesRecursive(routeEntryPointFile, routeEntryPointDependencies);
        return routeEntryPointDependencies;
    }

    /**
     * 递归寻找入口文件的相对依赖
     *
     * @param routeEntryPointFile         路由入口文件
     * @param routeEntryPointDependencies 返回值: 路由入口文件依赖
     */
    private void findRouteEntryPointComponentDependenciesRecursive(File routeEntryPointFile,
                                                                   List<File> routeEntryPointDependencies) {
        Assert.notNull(routeEntryPointDependencies, "route entry point dependencies collection must be initialized first.");
        try {
            List<String> lines = FileUtils.readLines(routeEntryPointFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                // 获取导入组件的绝对路径
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("file line _______________{}", line);
                }
                String dependencyAbsolutePath = this.buildImportFileAbsolutePath(routeEntryPointFile, line);
                if (StringUtils.isEmpty(dependencyAbsolutePath)) {
                    continue;
                }

                // 处理自身
                File dependencyEntryPointFile = this.findRouteEntryPointComponentFile(dependencyAbsolutePath);
                if (dependencyEntryPointFile != null) {
                    // 如果找到入口文件, 则继续处理
                    if (routeEntryPointDependencies.stream()
                            .noneMatch(item -> item.getAbsolutePath().equals(dependencyEntryPointFile.getAbsolutePath()))) {
                        routeEntryPointDependencies.add(dependencyEntryPointFile);
                        LOGGER.info("===============size [{}]================", routeEntryPointDependencies.size());
                        this.findRouteEntryPointComponentDependenciesRecursive(dependencyEntryPointFile, routeEntryPointDependencies);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @Override
    public String buildImportFileAbsolutePath(File routeEntryPointFile,
                                              String importLine) {
        String lineTrimed = StringUtils.trimWhitespace(importLine);

        // 必须以`import`开始, 也即意味着处理导入组件行
        if (!lineTrimed.startsWith("import")) {
            return null;
        }

        // 寻找导入行单引号或双引号的部分, 识别依赖组件的相对路径
        Pattern p = Pattern.compile("[\"\'](.*?)[\"\']");
        Matcher m = p.matcher(lineTrimed);
        if (m.find()) {
            String dependencyLocation = StringUtils.replace(m.group(), "\"", "");
            dependencyLocation = StringUtils.replace(dependencyLocation, "\'", "");
            if (!dependencyLocation.startsWith(".") && !dependencyLocation.startsWith("@")) {
                // 如果不是相对路径, 则退出, 不作处理
                return null;
            }

            // 获取依赖组件的绝对路径
            File currentPath = routeEntryPointFile;
            StringBuilder subDirBuilder = new StringBuilder(File.separator);
            /*
             * WINDOW 文件分割符是\,这里调整一下
             */
            String[] dependencyLocationArray = dependencyLocation.split("/");
            for (String path : dependencyLocationArray) {
                if (".".equals(path)) {
                    currentPath = currentPath.getParentFile();
                } else if ("..".equals(path)) {
                    currentPath = currentPath.getParentFile().getParentFile();
                } else if ("@".equals(path)) {
                    currentPath = currentPath.getParentFile().getParentFile().getParentFile().getParentFile();
                } else {
                    subDirBuilder.append(path).append(File.separator);
                }
            }
            String importFileAbsolutePath = currentPath.getAbsolutePath() + StringUtils.trimTrailingCharacter(subDirBuilder.toString(), File.separatorChar);

            // 排除掉.css/.less/.sass扩展名的文件
            for (String exclude : EXCLUDE_COMPONENT_EXT_ARRAY) {
                if (importFileAbsolutePath.endsWith(exclude)) {
                    return null;
                }
            }

            // 去除后缀名, 为了后续寻找入口文件的逻辑统一, 此处返回值去除扩展名
            return importFileAbsolutePath.replace(".js", "");
        }

        return null;
    }

    @Override
    public List<UiComponent> scanButtonComp(Map<String, Collection<RouteComponentFileVO>> routeComponentFiles,
                                            String uiPermissionComponentAttr) {
        LOGGER.info("route component files size is {}.", routeComponentFiles.size());
        List<UiComponent> uiComponents = new ArrayList<>(64);
        if (CollectionUtils.isEmpty(routeComponentFiles)) {
            return uiComponents;
        }

        // 分析组件文件中的权限配置
        routeComponentFiles.forEach((rootPageRoute, routeComponentFileCollection) -> {
            routeComponentFileCollection.forEach(routeComponentFile -> {
                List<UiComponent> currentUiPermissionComps = this.analyzeRouteComponentUiPermissions(rootPageRoute, routeComponentFile, uiPermissionComponentAttr);
                if (!CollectionUtils.isEmpty(currentUiPermissionComps)) {
                    uiComponents.addAll(currentUiPermissionComps);
                }
            });
        });

        return uiComponents;
    }

    /**
     * 分析路由之后的组件信息, 获取UiPermission
     * <p>
     * 思路: 读取成为一个大的字符串, 然后去除所有空格, 之后通过正则表达式截取
     *
     * @param rootPageRoute             页面路由
     * @param routeComponentFile        路由组件文件
     * @param uiPermissionComponentAttr UI权限组件属性名
     * @return
     */
    private List<UiComponent> analyzeRouteComponentUiPermissions(String rootPageRoute,
                                                                 RouteComponentFileVO routeComponentFile,
                                                                 String uiPermissionComponentAttr) {
        String uiComponentFileContent;
        try {
            uiComponentFileContent = FileUtils.readFileToString(routeComponentFile.getComponentFile(), StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            LOGGER.error("read content of component file {} with exception.", routeComponentFile.getComponentFile().getAbsolutePath());
            LOGGER.error("read content of component file with exception: ", ioe);
            throw new IllegalStateException(ioe);
        }
        if (StringUtils.isEmpty(uiComponentFileContent)) {
            return Collections.emptyList();
        }

        List<UiComponent> uiComponents = new ArrayList<>(16);
        // 剔除所有空格, 便于正则表达式分析及处理
        uiComponentFileContent = StringUtils.trimAllWhitespace(uiComponentFileContent);
        // 方式一: 组件属性方式
        this.findRouteComponentUiPermissions(
                uiComponents,
                rootPageRoute,
                routeComponentFile.getPageRoute(),
                routeComponentFile.getComponentFile().getName(),
                uiComponentFileContent,
                uiPermissionComponentAttr,
                "={[",
                "]}"
        );

        // 方式二: 对象属性方式
        this.findRouteComponentUiPermissions(
                uiComponents,
                rootPageRoute,
                routeComponentFile.getPageRoute(),
                routeComponentFile.getComponentFile().getAbsolutePath(),
                uiComponentFileContent,
                uiPermissionComponentAttr,
                ":[",
                "]"
        );

        return uiComponents;
    }

    /**
     * 查找路由组件的UI权限VO
     *
     * @param resultUiComponents              结果返回列表
     * @param rootPageRoute                   页面路由
     * @param uiComponentFileAbsolutePath     UI组件所属全路径
     * @param uiComponentFileContent          UI组件文件字符串内容
     * @param uiPermissionComponentAttr       UI权限组件属性名
     * @param uiPermissionComponentAttrPrefix 前缀
     * @param uiPermissionComponentAttrSuffix 后缀
     */
    private void findRouteComponentUiPermissions(List<UiComponent> resultUiComponents,
                                                 String rootPageRoute,
                                                 String currentPageRoute,
                                                 String uiComponentFileAbsolutePath,
                                                 String uiComponentFileContent,
                                                 String uiPermissionComponentAttr,
                                                 String uiPermissionComponentAttrPrefix,
                                                 String uiPermissionComponentAttrSuffix) {
        Assert.notNull(resultUiComponents, "result collection must be initialize first.");
        String localPermissionComponentStart = uiPermissionComponentAttr + uiPermissionComponentAttrPrefix;
        String localPermissionComponentEnd = uiPermissionComponentAttrSuffix;
        // permissionList\\=\\{\\[.+?.\\]\\} 或者 permissionList\\:\\[.+?.\\]
        String componentAttrRegex =
                uiPermissionComponentAttr + this.buildRegexPatternEscapeChar(uiPermissionComponentAttrPrefix)
                        + ".+?." + this.buildRegexPatternEscapeChar(localPermissionComponentEnd);
        Pattern pattern = Pattern.compile(componentAttrRegex);
        Matcher matcher = pattern.matcher(uiComponentFileContent);
        while (matcher.find()) {
            String permissionJson = matcher.group();
            permissionJson = StringUtils.replace(permissionJson, localPermissionComponentStart, "[");
            permissionJson = StringUtils.replace(permissionJson, localPermissionComponentEnd, "]");
            // 这个方法能得到一个页面所有按钮的对象集合
            resultUiComponents.addAll(this.parseRouteComponentUiPermissions(rootPageRoute, currentPageRoute, permissionJson, uiComponentFileAbsolutePath));
        }
    }

    /**
     * 为界定符增加转义字符
     *
     * @param delimiter 界定符
     * @return
     */
    private String buildRegexPatternEscapeChar(String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < delimiter.length(); i++) {
            stringBuilder.append("\\");
            stringBuilder.append(delimiter.charAt(i));
        }

        return stringBuilder.toString();
    }

    /**
     * 解析路由之后的组件信息, 获取UiPermission
     *
     * @param rootPageRoute  页面路由
     * @param permissionJson 权限Json
     * @param pageBelonged   所属页面
     * @return
     */
    private List<UiComponent> parseRouteComponentUiPermissions(String rootPageRoute,
                                                               String currentPageRoute,
                                                               String permissionJson,
                                                               String pageBelonged) {
        List<UiComponent> uiComponents = new ArrayList<>(8);
        // 剔除特殊字符
        String effectivePermissionJson = permissionJson.replace(PERMISSION_ATTR_VALUE_INVALID_DELIMITER, PERMISSION_ATTR_VALUE_DELIMITER);

        // 转化为JSON处理
        ObjectMapper objectMapper = ObjectMapperBuilder.buildObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(effectivePermissionJson);
            if (!rootNode.isArray()) {
                LOGGER.info("invalid permission component {}.", effectivePermissionJson);
                throw new IllegalArgumentException("invalid permission component.");
            }
            System.out.println(currentPageRoute);
            rootNode.iterator().forEachRemaining(permissionNode -> {
                String permissionCode = permissionNode.get("code").textValue();
                String permissionName = permissionNode.get("meaning").textValue();
                String permissionType = permissionNode.get("type").textValue();

                // 处理permissionCode
                // 权限代码取自当前路由
                String pageRouteWithoutLeadingAndTrailingDelimiter = currentPageRoute;
                // 去除开头的 /
                pageRouteWithoutLeadingAndTrailingDelimiter = StringUtils.trimLeadingCharacter(pageRouteWithoutLeadingAndTrailingDelimiter, PAGE_ROUTE_DELIMITER);
                // 去除结尾的 /
                pageRouteWithoutLeadingAndTrailingDelimiter = StringUtils.trimTrailingCharacter(pageRouteWithoutLeadingAndTrailingDelimiter, PAGE_ROUTE_DELIMITER);
                // 这个方法做的事情是将按钮权限的code前缀"${this.props.match.path}", "${match.path}", "${path}"替换掉 变成pub/hpfm/code-rule-org/dist/:id.button.DetailCreate这种格式
                permissionCode = this.replacePermissionCodePageRouteVar(permissionCode, pageRouteWithoutLeadingAndTrailingDelimiter);
                // 然后将 / 替换成 . 将 ： 替换成 -
                permissionCode = permissionCode.replace(PAGE_ROUTE_DELIMITER, PERMISSION_CODE_DELIMITER);
                permissionCode = permissionCode.replace(":", "-"); // 统一替换路由参数
                UiComponent uiComponent = new UiComponent();
                uiComponent.setUiRoute(rootPageRoute).setCompCode(permissionCode).setDescription(permissionName).setCompType(permissionType).setPageBelonged(pageBelonged);
                uiComponents.add(uiComponent);
            });
        } catch (IOException ioe) {
            LOGGER.error("parse permission {} to json with exception.", effectivePermissionJson);
            LOGGER.error("parse permission to json with exception: ", ioe);
        }

        return uiComponents;
    }

    /**
     * 替换权限代码中的页面路由变量
     *
     * @param permissionCode                              权限代码
     * @param pageRouteWithoutLeadingAndTrailingDelimiter 替换之后的页面路由代码
     * @return 替换之后的权限代码
     */
    private String replacePermissionCodePageRouteVar(String permissionCode,
                                                     String pageRouteWithoutLeadingAndTrailingDelimiter) {
        Assert.hasText(permissionCode, "permission code cannot be null.");
        // 按钮权限的code写法 "${this.props.match.path}", "${match.path}", "${path}" 匹配成功后替换
        for (String pageRouteVar : PAGE_ROUTE_VAR_ARRAY) {
            if (permissionCode.contains(pageRouteVar)) {
                return StringUtils.replace(permissionCode, pageRouteVar, pageRouteWithoutLeadingAndTrailingDelimiter);
            }
        }

        return permissionCode;
    }

}
