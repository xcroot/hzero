package org.hzero.generator.scan.infra.parser.impl;

import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.scan.domain.JsFunction;
import org.hzero.generator.scan.domain.JsVariable;
import org.hzero.generator.scan.domain.UiApi;
import org.hzero.generator.scan.infra.constant.Constants;
import org.hzero.generator.scan.infra.parser.JsRequestApiParser;
import org.hzero.generator.scan.infra.util.JsUtil;
import org.hzero.generator.scan.infra.util.RegexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 针对请求，目前仅支持如下JS写法，对于匿函数是不支持的
 * request(getUrl())
 * request(getUrl()+"/url-path")
 * request(`${param}`+"/url-path")
 * request(tenant?`{$param}`+"/url-path":`${siteUrl}`)
 * <p>
 * 局限：
 * 1，不支持复杂的三元表达式，也就是三元表达式的嵌套
 *
 * @author jianbo.li
 * @date 2019/12/23 15:37
 */
@Service
public class DefaultJsRequestApiParserImpl implements JsRequestApiParser {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultJsRequestApiParserImpl.class);

    /**
     * URL PATH查找
     */
    private static final Pattern REQUEST_FIRST_PARAM_REGEX = Pattern.compile("request\\s*?\\(.*?\\);", Pattern.DOTALL);

    /**
     * URL METHOD查找
     */
    private static final Pattern REQUEST_METHOD_REGEX = Pattern.compile("\\{.*?method\\s*:\\s*(.*?),", Pattern.DOTALL);


    /**
     * 默认方法
     */
    private static final String DEFAULT_METHOD = "get";

    /**
     * 函数返回值正则表达式
     */
    private static final Pattern FUNCTION_RETURN_REGEX = Pattern.compile("[\\s;]+?return\\s*(.*?);", Pattern.DOTALL);

    private static final Pattern VARIABLE_REF = Pattern.compile("\\$\\{(.*?)}");

    private static final Pattern TERNARY_EXPRESSION_TENANT_ROLE = Pattern.compile("(.*?)\\$\\{\\s*?isTenantRoleLevel\\(\\)\\s*\\?\\s*[`|'\"](.*?)[`|'\"]\\s*:\\s*[`|'\"](.*?)[`|'\"]\\s*}(.*)", Pattern.DOTALL);

    private static final Pattern TERNARY_EXPRESSION_SITE_FLAG = Pattern.compile("(.*?)\\$\\{\\s*?isSiteFlag\\s*\\?\\s*[`'\"](.*?)[`'\"]\\s*:\\s*[`'\"](.*?)[`'\"]\\s*}(.*)", Pattern.DOTALL);

    private static final Pattern TERNARY_EXPRESSION_ORGANIZATION_ID = Pattern.compile("(.*?)\\$\\{\\s*?organizationId === 0\\s*\\?\\s*[`'\"](.*?)[`'\"]\\s*:\\s*[`'\"](.*?)[`'\"]\\s*}(.*)", Pattern.DOTALL);

    private static final Pattern TERNARY_EXPRESSION_PARAMS_ORGANIZATION_ID = Pattern.compile("(.*?)\\$\\{\\s*?params\\.organizationId !== undefined\\s*\\?\\s*[`'\"](.*?)[`'\"]\\s*:\\s*[`'\"](.*?)[`'\"]\\s*}(.*)", Pattern.DOTALL);

    private static final Pattern TERNARY_EXPRESSION = Pattern.compile(".*\\s*\\?\\s*(['\"`].*?['\"`])\\s*:\\s*(['\"`].*?['\"`])", Pattern.DOTALL);

    private static final Pattern URL = Pattern.compile("url=(.+?);|url:(.+?),", Pattern.DOTALL);

    private static final Pattern METHOD = Pattern.compile("method:['\"`](.*?)['\"`],", Pattern.DOTALL);

    @Override
    public List<UiApi> parseJsApi(JsFunction jsFunction, List<JsFunction> jsFunctionList, List<JsVariable> jsVariableList, String content) {
        String function = RegexUtil.getFirstMatcher(jsFunction.getFunctionEntity(), 0, REQUEST_FIRST_PARAM_REGEX);
        if (StringUtils.isEmpty(function)) {
            return Collections.emptyList();
        }
        // 获取request中的内容
        List<String> params = foundFunctionParamList(function);
        String requestFirstParam = params.get(0);

        if (StringUtils.isEmpty(requestFirstParam)) {
            return Collections.emptyList();
        }
        String method = DEFAULT_METHOD;
        // 获取METHOD
        if (params.size() > 1) {
            method = RegexUtil.getFirstMatcher(params.get(1), 1, REQUEST_METHOD_REGEX);
            if (!StringUtils.isEmpty(method)) {
                method = method.replaceAll("[\"'`]", "").trim().toLowerCase();
            }
        }
        // 解析路径
        List<String> paths = parseStatement(requestFirstParam, jsFunction, jsVariableList, jsFunctionList);
        String finalMethod = StringUtils.isEmpty(method) ? DEFAULT_METHOD : method.toLowerCase();
        return paths.stream().map(item -> new UiApi(item.contains("?") ? item.substring(0, item.indexOf("?")) : item, finalMethod)).collect(Collectors.toList());
    }

    @Override
    public List<UiApi> parseTransportApi(JsFunction jsFunction, List<JsFunction> jsFunctionList, List<JsVariable> jsVariableList, String content) {
        Matcher urlMatcher = URL.matcher(jsFunction.getFunctionEntity());
        String url = "";
        if (urlMatcher.find()) {
            url = urlMatcher.group(1) != null ? urlMatcher.group(1) : urlMatcher.group(2);
        }
        if (StringUtils.isEmpty(url)) {
            return Collections.emptyList();
        }
        String method = RegexUtil.getFirstMatcher(jsFunction.getFunctionEntity(), 1, METHOD);
        method = method == null ? DEFAULT_METHOD : method;
        List<String> paths = parseStatement(url, jsFunction, jsVariableList, jsFunctionList);
        String finalMethod = method.toLowerCase();
        return paths.stream().map(item -> new UiApi(item.contains("?") ? item.substring(0, item.indexOf("?")) : item, finalMethod)).collect(Collectors.toList());
    }

    /**
     * 解析js语句
     *
     * @param jsStatement    js语句
     * @param jsFunction     js语句所属函数
     * @param jsVariableList js变量集
     * @param jsFunctionList js函数集
     * @return
     */
    private List<String> parseStatement(String jsStatement,
                                        JsFunction jsFunction,
                                        List<JsVariable> jsVariableList,
                                        List<JsFunction> jsFunctionList) {
        if (jsStatement.contains(Constants.DOUBLE_VERTICAL_LINE)) {
            String[] variables = jsStatement.split(Constants.DOUBLE_VERTICAL_LINE);
            if (!isConstants(variables[0]) && !isConstants(variables[1])) {
                return Arrays.asList(variables);
            } else {
                jsStatement = isConstants(variables[0]) ? variables[0] : variables[1];
            }
        }
        // 判断路径是否包含三元表达式，是则切割值 返回 ：左右的值
        List<String> jsVariableValues = extractTernaryExpression(jsStatement);

        List<String> result = new ArrayList<>();
        for (String statement : jsVariableValues) {
            String[] periods = statement.split("\\+");

            StringBuilder statementValueBuilder = new StringBuilder();
            StringBuilder statementValueSiteBuilder = new StringBuilder();
            for (String period : periods) {
                // 过滤掉\\n
                period = filterSpecialChar(period);
                // 字符串值 去掉` ' "
                period = period.replaceAll("[`\"\']", "");
                // js 变量 判断period的值 是否等于变量或者${变量}
                if (variableStatement(period, Constants.VariableScope.P, jsFunction.getFunctionName(), jsVariableList)) {
                    JsVariable refVariable = getVariable(period, Constants.VariableScope.P, jsFunction.getFunctionName(), jsVariableList);
                    Assert.isTrue(refVariable != null, String.format("variable[%s] not found", period));
                    List<String> variableValues = parseStatement(refVariable.getValue(),
                            jsFunction, jsVariableList, jsFunctionList);
                    // Parse Ref Variable
                    setUpPeriod(statementValueBuilder, statementValueSiteBuilder, variableValues);
                } else if (functionStatement(period, jsFunctionList)) {
                    // js 函数 判断是否包含函数调用
                    JsFunction invokeFunction = getFunction(period, jsFunctionList);
                    Assert.isTrue(invokeFunction != null, String.format("function[%s] not found", period));
                    List<String> functionReturn = parseFunctionReturn(period, invokeFunction,
                            foundFunctionParamList(period), jsFunctionList, jsVariableList);
                    setUpPeriod(statementValueBuilder, statementValueSiteBuilder, functionReturn);
                } else {
                    // 无法处理的类型
                    statementValueBuilder.append(period);
                    statementValueSiteBuilder.append(period);
                }
            }

            result.add(statementValueBuilder.toString());
            if (!statementValueBuilder.toString().equals(statementValueSiteBuilder.toString())) {
                result.add(statementValueSiteBuilder.toString());
            }
        }

        /*
         * 解析路径，我们将字符串值存在的es6语法中对参数变量的引用进行解析
         * eg, const pathPrefix = 'v1/test';const url = '${pathPrefix}/info' ----> url = v1/test/info;
         */
        List<String> paths = new ArrayList<>();
        result.forEach(path -> {
            // 切割方式
            String pathSite = path;
            List<String> refVariables = RegexUtil.getAllMatcher(path, 1, VARIABLE_REF);
            for (String refVar : refVariables) {
                JsVariable jsVariable = getVariable(refVar, Constants.VariableScope.P, jsFunction.getFunctionName(), jsVariableList);
                if (jsVariable == null) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("no ref variable found , it may be normal,path = {},var = {}", path, refVar);
                    }
                    continue;
                }
                List<String> refVarValue = parseStatement(jsVariable.getValue(), jsFunction, jsVariableList, jsFunctionList);
                if (refVarValue.size() < 2) {
                    path = path.replace(JsUtil.setPlaceHolder(refVar), refVarValue.get(0));
                    pathSite = pathSite.replace(JsUtil.setPlaceHolder(refVar), refVarValue.get(0));
                } else {
                    path = path.replace(JsUtil.setPlaceHolder(refVar), refVarValue.get(0));
                    pathSite = pathSite.replace(JsUtil.setPlaceHolder(refVar), refVarValue.get(1));
                }
            }
            paths.add(path);
            if (!path.equals(pathSite)) {
                paths.add(pathSite);
            }
        });
        LOGGER.info("paths: {}", paths);
        Assert.isTrue(paths.size() < 5, String.format("too many return statement with statement[%s]", jsStatement));
        return paths;
    }

    /**
     * js 函数解析return, 我们认为最多返回两个值，复杂函数无法解析
     *
     * @param period         原语句
     * @param jsFunction     js函数
     * @param params         调用参数
     * @param jsFunctionList js函数列表
     * @return
     */
    private List<String> parseFunctionReturn(String period,
                                             JsFunction jsFunction,
                                             List<String> params,
                                             List<JsFunction> jsFunctionList,
                                             List<JsVariable> jsVariableList) {
        List<String> returnStatements = RegexUtil.getAllMatcher(jsFunction.getFunctionEntity(), 1, FUNCTION_RETURN_REGEX);
        LOGGER.info("returnStatements: {}", returnStatements);
        LOGGER.info("jsFunction: {}", jsFunction.getFunctionEntity());
        if (CollectionUtils.isEmpty(returnStatements)) {
            return Collections.emptyList();
        }

        Assert.isTrue(returnStatements.size() < 3, String.format("found too many return in function[%s]", jsFunction.getFunctionEntity()));

        // 返回
        List<String> returnValues = new ArrayList<>();

        // 单一返回语句
        for (String returnStatement : returnStatements) {
            List<String> statements = parseStatement(returnStatement, jsFunction, jsVariableList, jsFunctionList);
            Pattern pattern = Pattern.compile("(.*?)\\$\\{" + jsFunction.getFunctionName() + "\\((.*?)\\)}(.*)");
            Matcher url = pattern.matcher(period);
            if (url.find()) {
                String prefix = url.group(1);
                String suffix = url.group(3);
                statements.forEach(statement ->
                        returnValues.add((StringUtils.isEmpty(prefix) ? StringUtils.EMPTY : prefix)
                                .concat(statement).concat(StringUtils.isEmpty(suffix) ? StringUtils.EMPTY : suffix)));
            } else {
                returnValues.addAll(statements);
            }
        }
        List<String> result = new ArrayList<>();
        // 处理函数对参数的引用
        returnValues.forEach(item -> {
            String siteItem = item;
            if (jsFunction.getParamList() != null) {
                for (int i = 0; i < jsFunction.getParamList().length; i++) {
                    if (item.contains(JsUtil.setPlaceHolder(jsFunction.getParamList()[i])) && i < params.size()) {
                        List<String> holdValues = parseStatement(params.get(i), jsFunction, jsVariableList, jsFunctionList);
                        if (holdValues.size() >= 2) {
                            item = item.replace(JsUtil.setPlaceHolder(jsFunction.getParamList()[i]), holdValues.get(0));
                            siteItem = siteItem.replace(JsUtil.setPlaceHolder(jsFunction.getParamList()[i]), holdValues.get(1));
                        } else {
                            item = item.replace(JsUtil.setPlaceHolder(jsFunction.getParamList()[i]), holdValues.get(0));
                            siteItem = siteItem.replace(JsUtil.setPlaceHolder(jsFunction.getParamList()[i]), holdValues.get(0));
                        }
                    }
                }
            }
            result.add(item);
            if (!item.equals(siteItem)) {
                result.add(siteItem);
            }
        });

        Assert.isTrue(result.size() < 3, String.format("too many return found in function[%s]", jsFunction.getFunctionEntity()));
        return result;
    }


    /**
     * <p>
     * 提取三元表达式？ 后面的部分
     * 只是支持简单三元表达式，复杂三元表达式目前不支持
     * 我们认为一条JS语句中至多两条字符串语句
     * return judge? "aaa" : "bbb";
     *
     * @return 解析结果
     */
    private static List<String> extractTernaryExpression(String statement) {
        List<String> result = new ArrayList<>();
        // 解析`${HZERO_FILE}/v1/${isTenantRoleLevel() ? `${params.organizationId}/` : ''}files/multipart`格式
        Matcher tenantRole = TERNARY_EXPRESSION_TENANT_ROLE.matcher(statement);
        if (tenantRole.find()) {
            result.add(tenantRole.group(1) + tenantRole.group(2) + tenantRole.group(4));
            result.add(tenantRole.group(1) + tenantRole.group(3) + tenantRole.group(4));
            return result;
        }
        Matcher siteFlag = TERNARY_EXPRESSION_SITE_FLAG.matcher(statement);
        if (siteFlag.find()) {
            result.add(siteFlag.group(1) + siteFlag.group(2) + siteFlag.group(4));
            result.add(siteFlag.group(1) + siteFlag.group(3) + siteFlag.group(4));
            return result;
        }
        Matcher organizationId = TERNARY_EXPRESSION_ORGANIZATION_ID.matcher(statement);
        if (organizationId.find()) {
            result.add(organizationId.group(1) + organizationId.group(2) + organizationId.group(4));
            result.add(organizationId.group(1) + organizationId.group(3) + organizationId.group(4));
            return result;
        }
        Matcher params = TERNARY_EXPRESSION_PARAMS_ORGANIZATION_ID.matcher(statement);
        if (params.find()) {
            result.add(params.group(1) + params.group(2) + params.group(4));
            result.add(params.group(1) + params.group(3) + params.group(4));
            return result;
        }
        Matcher specialMatcher = TERNARY_EXPRESSION.matcher(statement);
        if (specialMatcher.find()) {
            // 解析普通三目运算
            result.add(specialMatcher.group(1));
            result.add(specialMatcher.group(2));
            return result;
        }
        // 普通的字符串返回
        result.add(statement);
        return result;
    }

    private String filterSpecialChar(String str) {
        return str.replaceAll("\\\\n", "")
                .trim();
    }

    /**
     * 安装短句值
     *
     * @param stringBuilder
     * @param stringSiteBuilder
     * @param periodValues
     */
    private void setUpPeriod(StringBuilder stringBuilder, StringBuilder stringSiteBuilder, List<String> periodValues) {
        LOGGER.info("periodValues:{}", periodValues);
        if (periodValues.size() > 1) {
            stringBuilder.append(periodValues.get(0));
            stringSiteBuilder.append(periodValues.get(1));
        } else {
            stringBuilder.append(periodValues.get(0));
            stringSiteBuilder.append(periodValues.get(0));
        }
    }

    /**
     * 判断当前一条JS语句是不是存在函数调用
     *
     * @param statement      js语句
     * @param jsFunctionList 当前文件js函数列表
     * @return
     */
    private boolean functionStatement(String statement,
                                      List<JsFunction> jsFunctionList) {
        return getFunction(statement, jsFunctionList) != null;
    }

    /**
     * 字符串语句
     *
     * @param statement js语句
     * @return
     */
    private boolean stringStatement(String statement) {
        return statement.matches("(^`.*`$)|(^\".*\"$)|(^\'.*\'$)");
    }


    /**
     * 寻找函数参数列表 return request()括号中的内容
     *
     * @param function
     * @return
     */
    private static List<String> foundFunctionParamList(String function) {
        int left = 0;
        int paramStartPos = -1;
        List<Integer> paramEndPosList = new ArrayList<>();
        char[] chars = function.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '(' || chars[i] == '{') {
                if (paramStartPos < 0) {
                    paramStartPos = i;
                } else {
                    left++;
                }
            }
            if (chars[i] == ')' || chars[i] == '}') {
                if (left > 0) {
                    left--;
                } else {
                    paramEndPosList.add(i);
                }
            }
            if (chars[i] == ',' && left == 0) {
                paramEndPosList.add(i);
            }
        }

        paramEndPosList.add(paramStartPos);
        if (paramEndPosList.size() >= 2) {
            paramEndPosList.sort(Comparator.comparing(Integer::intValue));
            List<String> paramList = new ArrayList<>();
            for (int i = 0; i < paramEndPosList.size() - 1; i++) {
                paramList.add(function.substring(paramEndPosList.get(i) + 1, paramEndPosList.get(i + 1)).trim());
            }
            return paramList;
        }

        return Collections.emptyList();
    }

    /**
     * 判断当前一条JS语句是不是存在函数调用
     *
     * @param statement      js语句
     * @param jsFunctionList 当前文件js函数列表
     * @return
     */
    private JsFunction getFunction(String statement,
                                   List<JsFunction> jsFunctionList) {
        for (JsFunction jsFunction : jsFunctionList) {
            String pattern = ".*?" + jsFunction.getFunctionName() + "\\s*\\(.*?\\).*?";
            if (statement.matches(pattern)) {
                return jsFunction;
            }
        }
        return null;
    }

    private boolean variableStatement(String statement,
                                      String scope,
                                      String scopeValue,
                                      List<JsVariable> jsVariableList) {
        return getVariable(statement, scope, scopeValue, jsVariableList) != null;
    }

    /**
     * TODO 排序 优先取函数内变量
     * <p>
     * 当前js语句是否存在对当前文件变量的引用
     *
     * @param statement
     * @param jsVariableList
     * @return
     */
    private JsVariable getVariable(String statement,
                                   String scope,
                                   String scopeValue,
                                   List<JsVariable> jsVariableList) {
        for (JsVariable jsVariable : jsVariableList) {
            // 首先作用范围必须一致
            boolean fitScope = (Constants.VariableScope.A.equals(scope) && Constants.VariableScope.A.equals(jsVariable.getScope())) ||
                    (Constants.VariableScope.P.equals(scope) && (Constants.VariableScope.A.equals(jsVariable.getScope()) || Objects.equals(scopeValue, jsVariable.getScopeValue())));

            if (fitScope) {

                // 先判断是否存在于es6写法中,`${var}`
                if (statement.equals(JsUtil.setPlaceHolder(jsVariable.getName())) || statement.equals(jsVariable.getName())) {
                    return jsVariable;
                }
            }
        }
        return null;
    }

    /**
     * 常规常量判断 只要有小写字母返回fasle
     *
     * @param variable
     * @return
     */
    private boolean isConstants(String variable) {
        for (int i = 0; i < variable.length(); i++) {
            if (Character.isLowerCase(variable.indexOf(i))) {
                return false;
            }
        }
        return true;
    }

}
