package org.hzero.generator.scan.provider.impl;

import org.apache.commons.io.FileUtils;
import org.hzero.generator.scan.domain.JsFunction;
import org.hzero.generator.scan.domain.JsVariable;
import org.hzero.generator.scan.domain.UiApi;
import org.hzero.generator.scan.domain.UiPermissionApi;
import org.hzero.generator.scan.infra.constant.Constants;
import org.hzero.generator.scan.infra.util.FileUtil;
import org.hzero.generator.scan.infra.util.JsUtil;
import org.hzero.generator.scan.infra.util.RegexUtil;
import org.hzero.generator.scan.provider.UiApiScannerProvider;
import org.hzero.generator.scan.provider.config.ApiProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author fanghan.liu 2020/01/13 10:15
 */
@Component
public class UiApiScannerProviderImpl extends ApiProviderConfig implements UiApiScannerProvider {

    private static final Pattern ANNOTATION_PERMISSION_CODE_REGEX = Pattern.compile("@param\\s*?\\{Array}\\s*?params\\.permissionList\\s*?=\\s*?\\[\\s*'(.*?)'\\s*]");

    private static final Pattern PATH_VAR_HOLD_REGEX = Pattern.compile("\\$\\{(.*?)}");

    private static final Pattern API_PREFIX = Pattern.compile("(\\{[A-Z_]*?})(\\{[A-Z_]*?})");

    private static final Pattern FILE_REF = Pattern.compile("import\\s*\\{\\s*[A-Z_,\\s]*}\\s*from\\s*'@(.*?)';");

    private static final Pattern FILE_CONSTANT = Pattern.compile("const([A-Z_]*?)=['\"`]?(.*?)['\"`]?;");

    private static final String STORES = "stores";

    /**
     * 日志信息
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UiApiScannerProviderImpl.class);

    @Override
    public List<UiPermissionApi> scanApi(File apiFile, String pageRoute) {
        String content;
        try {
            content = FileUtils.readFileToString(apiFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("================= begin handle button api file: {} ==================", apiFile.getAbsolutePath());
        }
        // 判断文件是否在stores包下
        String packageName = apiFile.getParentFile().getName();
        if (STORES.equals(packageName)) {
            return scanStoresFileApi(apiFile, content, pageRoute);
        }
        return scanServicesFileApi(apiFile, content, pageRoute);
    }

    @Override
    public List<UiPermissionApi> scanServicesFileApi(File apiFile, String content, String pageRoute) {
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        // 获取到所有函数
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("================ Begin Analysis Function =================");
        }
        List<JsFunction> jsFunctionList = getJsFunctionResolver().resolveFunction(content);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("=========== found function size :{}  ===========", jsFunctionList.size());
        }

        // 获取到所有变量
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("============== Begin Analysis Variable ==================");
        }
        List<JsVariable> jsVariableList = getAllVariable(jsFunctionList, content);
        jsVariableList.addAll(getRefVariable(apiFile, content));
        // 以函数为单位解析Api
        List<UiPermissionApi> uiPermissionApiList = new ArrayList<>();
        List<UiPermissionApi> uiPublicApiList = new ArrayList<>();
        for (JsFunction jsFunction : jsFunctionList) {
            List<UiApi> apiList = getJsRequestApiParser().parseJsApi(jsFunction, jsFunctionList, jsVariableList, content);
            if (CollectionUtils.isEmpty(apiList)) {
                continue;
            }
            parseApiPermissionCode(apiList, jsFunction, pageRoute, uiPermissionApiList, uiPublicApiList);
            parseApiLevel(jsFunction, apiList, pageRoute);
        }
        // code相等的变量进行合并
        List<UiPermissionApi> result = new ArrayList<>();
        result.addAll(mergePermission(uiPermissionApiList, pageRoute));
        result.addAll(uiPublicApiList);
        return result;
    }

    @Override
    public List<UiPermissionApi> scanStoresFileApi(File apiFile, String content, String pageRoute) {
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        // 获取到所有函数
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("================ Begin Analysis Function =================");
        }
        List<JsFunction> jsFunctionList = getJsFunctionResolver().resolveTransport(content);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("=========== found function size :{}  ===========", jsFunctionList.size());
        }
        List<JsVariable> jsVariableList = getAllVariable(jsFunctionList, content);
        jsVariableList.addAll(getRefVariable(apiFile, content));
        // 以函数为单位解析Api
        List<UiPermissionApi> uiPermissionApiList = new ArrayList<>();
        List<UiPermissionApi> uiPublicApiList = new ArrayList<>();
        for (JsFunction jsFunction : jsFunctionList) {
            List<UiApi> apiList = getJsRequestApiParser().parseTransportApi(jsFunction, jsFunctionList, jsVariableList, content);
            if (CollectionUtils.isEmpty(apiList)) {
                continue;
            }
            parseApiPermissionCode(apiList, jsFunction, pageRoute, uiPermissionApiList, uiPublicApiList);
            parseApiLevel(jsFunction, apiList, pageRoute);

        }
        // code相等的变量进行合并
        List<UiPermissionApi> result = new ArrayList<>();
        result.addAll(mergePermission(uiPermissionApiList, pageRoute));
        result.addAll(uiPublicApiList);
        return result;
    }

    @Override
    public void parseApiService(List<UiPermissionApi> uiPermissionApiList, String serviceConfig) {
        for (UiPermissionApi uiPermissionApi : uiPermissionApiList) {
            Iterator<UiApi> uiApiIterator = uiPermissionApi.getApis().iterator();
            while (uiApiIterator.hasNext()) {
                UiApi item = uiApiIterator.next();
                if (!StringUtils.isEmpty(item.getServiceName())) {
                    continue;
                }
                String path = item.getPath();
                if (!path.contains(Constants.PATH_DIVIDER)) {
                    LOGGER.error("unsupported syntax, api:{}", item);
                    continue;
                }
                if (path.startsWith(Constants.PATH_DIVIDER)) {
                    path = item.getPath().substring(1);
                }
                String serviceRef = path.substring(0, path.indexOf(Constants.PATH_DIVIDER));
                String serviceName = serviceRef;
                if (serviceRef.contains("{")) {
                    Matcher apiPrefixMatcher = API_PREFIX.matcher(serviceName);
                    if (apiPrefixMatcher.find()) {
                        serviceName = apiPrefixMatcher.group(2);
                    }
                    serviceName = serviceName.replace("{", org.apache.commons.lang3.StringUtils.EMPTY)
                            .replace("}", org.apache.commons.lang3.StringUtils.EMPTY)
                            .replace("$", org.apache.commons.lang3.StringUtils.EMPTY);
                }
                item.setPath(item.getPath().substring(item.getPath().indexOf(serviceRef) + serviceRef.length()));
                try {
                    serviceName = getServiceName(serviceName, serviceConfig);
                    if (StringUtils.isEmpty(serviceName)) {
                        uiApiIterator.remove();
                    }
                } catch (Exception e) {
                    uiApiIterator.remove();
                }
                item.setServiceName(serviceName);
            }
        }
    }

    private String getServiceName(String serviceRef, String serviceConfig) {
        Pattern pattern = Pattern.compile(serviceRef
                + "\\s*:\\s*\\{.*?init\\s*:\\s*\\(\\s*\\)\\s*=>\\s*\\{\\s*return\\s*.*?(/[a-z]*).*?}.*?}\\s*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(serviceConfig);
        if (matcher.find()) {
            String serviceName = matcher.group(1);
            if (serviceName.startsWith(Constants.PATH_DIVIDER)) {
                serviceName = serviceName.substring(1);
            }
            return serviceName;
        }
        pattern = Pattern.compile(serviceRef
                + "\\s*:\\s*['\"](/[a-z]*)['|\"]", Pattern.DOTALL);
        matcher = pattern.matcher(serviceConfig);
        if (matcher.find()) {
            String serviceName = matcher.group(1);
            if (serviceName.startsWith(Constants.PATH_DIVIDER)) {
                serviceName = serviceName.substring(1);
            }
            return serviceName;
        }
        return serviceRef;
    }

    /**
     * 获取页面所有变量
     *
     * @param jsFunctionList 函数列表
     * @param content        文件内天
     * @return 变量
     */
    private List<JsVariable> getAllVariable(List<JsFunction> jsFunctionList, String content) {
        String holdContent = content;
        List<JsVariable> jsVariableList = new ArrayList<>();
        for (JsFunction jsFunction : jsFunctionList) {
            List<JsVariable> functionVariable = getJsVariableResolver().resolverVariable(jsFunction.getFunctionEntity());
            functionVariable.forEach(jsVariable -> {
                jsVariable.setScope(Constants.VariableScope.P);
                jsVariable.setScopeValue(jsFunction.getFunctionName());
            });
            holdContent = holdContent.replace(jsFunction.getFunctionEntity(), "");
            jsVariableList.addAll(functionVariable);
        }
        jsVariableList.addAll(getJsVariableResolver().resolverVariable(holdContent));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("============= found variableList size : {} ==============", jsFunctionList.size());
        }
        return jsVariableList;
    }


    /**
     * 获取页面从其他页面引入的变量
     *
     * @param apiFile 页面所属文件
     * @param content 文件内容
     * @return 变量
     */
    private List<JsVariable> getRefVariable(File apiFile, String content) {
        List<File> refFiles = new ArrayList<>();
        Matcher refMatcher = FILE_REF.matcher(content);
        while (refMatcher.find()) {
            String path = refMatcher.group(1);
            if (!StringUtils.isEmpty(path)) {
                File file = new File(apiFile.getParentFile().getParentFile().getPath() + path + ".js");
                if (file.exists()) {
                    refFiles.add(file);
                }
            }
        }
        List<JsVariable> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(refFiles)) {
            refFiles.forEach(file -> {
                String refContent = FileUtil.fileToStringWithoutSpace(file);
                Matcher constantMatcher = FILE_CONSTANT.matcher(refContent);
                while (constantMatcher.find()) {
                    result.add(new JsVariable(constantMatcher.group(1), constantMatcher.group(2)));
                }
            });
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * 解析api权限
     */
    private void parseApiPermissionCode(List<UiApi> apiList, JsFunction jsFunction, String pageRoute, List<UiPermissionApi> uiPermissionApiList, List<UiPermissionApi> uiPublicApiList) {
        String permissionCodeList = RegexUtil.getFirstMatcher(jsFunction.getFunctionEntity(), 1, ANNOTATION_PERMISSION_CODE_REGEX);
        if (!StringUtils.isEmpty(permissionCodeList)) {
            String[] codes = permissionCodeList.split(Constants.COMMA);
            for (String code : codes) {
                code = code.replaceAll("'", "");
                UiPermissionApi uiPermissionApi = new UiPermissionApi(code, pageRoute);
                uiPermissionApi.setApis(apiList);
                uiPermissionApiList.add(uiPermissionApi);
            }
        } else {
            UiPermissionApi uiPermissionApi = new UiPermissionApi(null, pageRoute);
            uiPermissionApi.setApis(apiList);
            uiPublicApiList.add(uiPermissionApi);
        }
    }

    /**
     * 解析api层级
     *
     * @param jsFunction 函数
     * @param apiList    api列表
     * @param pageRoute  路由
     */
    private void parseApiLevel(JsFunction jsFunction, List<UiApi> apiList, String pageRoute) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("=====================Parse API ==========================");
            LOGGER.info(jsFunction.getFunctionEntity());
        }
        // 解析API层级
        for (UiApi uiApi : apiList) {
            uiApi.setUiRoute(pageRoute);
            String path = uiApi.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            /*
             * 租户层api标识:
             * iam/v1/{租户id}/...
             * 租户id means: tenantId ,organizationId,
             * 若不满足这种格式，我们默认平台级与租户级功用一个api
             */
            // 截取路径第二段参数
            path = path.substring(path.indexOf(Constants.PATH_DIVIDER) + 1);
            path = path.substring(path.indexOf(Constants.PATH_DIVIDER) + 1);
            if (path.startsWith("v1") || path.startsWith("v2")) {
                path = path.substring(path.indexOf(Constants.PATH_DIVIDER) + 1);
            }
            if (path.contains(Constants.PATH_DIVIDER)) {
                path = path.substring(0, path.indexOf(Constants.PATH_DIVIDER));
            }
            uiApi.setApiLevel(Constants.PathLevel.organization);
            if (Constants.TenantIdPathFlag.GET_CURRENT_ORGANIZATION_ID.equalsIgnoreCase(path.trim())) {
                uiApi.setPath(uiApi.getPath().replace(Constants.TenantIdPathFlag.GET_CURRENT_ORGANIZATION_ID,
                        Constants.TenantIdPathFlag.STARDARD_PATH_FLAG));
            } else if (Constants.TenantIdPathFlag.TENANT_ID.equalsIgnoreCase(path.trim())) {
                uiApi.setPath(uiApi.getPath().replace(Constants.TenantIdPathFlag.TENANT_ID,
                        Constants.TenantIdPathFlag.STARDARD_PATH_FLAG));
            } else if (Constants.TenantIdPathFlag.ORGANIZATION_ID.equalsIgnoreCase(path.trim())) {
                uiApi.setPath(uiApi.getPath().replace(Constants.TenantIdPathFlag.ORGANIZATION_ID,
                        Constants.TenantIdPathFlag.STARDARD_PATH_FLAG));
            } else if (Constants.TenantIdPathFlag.PARAMS_ORGANIZATION_ID.equalsIgnoreCase(path.trim())) {
                uiApi.setPath(uiApi.getPath().replace(Constants.TenantIdPathFlag.PARAMS_ORGANIZATION_ID,
                        Constants.TenantIdPathFlag.STARDARD_PATH_FLAG));
            } else if (Constants.TenantIdPathFlag.GET_CURRENT_ORGANIZATION_ID_SPECIAL.equalsIgnoreCase(path.trim())) {
                uiApi.setPath(uiApi.getPath().replace(Constants.TenantIdPathFlag.GET_CURRENT_ORGANIZATION_ID_SPECIAL,
                        Constants.TenantIdPathFlag.STARDARD_PATH_FLAG));
            } else if (Constants.TenantIdPathFlag.PARAMS_TENANT_ID.equalsIgnoreCase(path.trim())) {
                uiApi.setPath(uiApi.getPath().replace(Constants.TenantIdPathFlag.PARAMS_TENANT_ID,
                        Constants.TenantIdPathFlag.STARDARD_PATH_FLAG));
            } else {
                uiApi.setApiLevel(Constants.PathLevel.site);
            }
        }

        // 解析API中其他路径参数的引用去掉$号
        for (UiApi uiApi : apiList) {
            List<String> holdVarList = RegexUtil.getAllMatcher(uiApi.getPath(), 1, PATH_VAR_HOLD_REGEX);
            String path = uiApi.getPath();
            for (String holdVar : holdVarList) {
                if (holdVar.contains(".")) {
                    String value = holdVar.substring(holdVar.indexOf(".") + 1);
                    path = path.replace(JsUtil.setPlaceHolder(holdVar), "{" + value + "}");
                }
            }
            path = path.replaceAll("\\$", org.apache.commons.lang3.StringUtils.EMPTY);
            uiApi.setPath(path);
        }
    }

    /**
     * 合并相同的code
     */
    private List<UiPermissionApi> mergePermission(List<UiPermissionApi> uiPermissionApiList, String pageRoute) {
        Map<String, List<UiPermissionApi>> groupApi = uiPermissionApiList.stream().collect(Collectors.groupingBy(UiPermissionApi::getPermissionCode));
        List<UiPermissionApi> result = new ArrayList<>();
        for (String code : groupApi.keySet()) {
            List<UiApi> jsRequestApiList = new ArrayList<>();
            groupApi.get(code).forEach(item -> jsRequestApiList.addAll(item.getApis()));
            result.add(new UiPermissionApi(code, jsRequestApiList, pageRoute));
        }
        return result;
    }
}
