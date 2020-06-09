package org.hzero.generator.scan.infra.resolver.impl;


import org.hzero.generator.scan.domain.JsFunction;
import org.hzero.generator.scan.infra.constant.Constants;
import org.hzero.generator.scan.infra.resolver.JsFunctionResolver;
import org.hzero.generator.scan.infra.util.RegexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认js函数解析实现
 *
 * @author jianbo.li
 * @date 2019/12/23 11:14
 */
@Service
public class DefaultJSFunctionResolverImpl implements JsFunctionResolver {

    /**
     * 日志信息
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJSFunctionResolverImpl.class);

    /**
     * 一个标准带注释的JS函数
     */
    private static final String JS_STANDARD_FUNCTION_WITH_ANNOTATION = "/**/ function xx(){}";

    /**
     * 一个不带注释的标准js函数
     */
    private static final String JS_STANDARD_FUNCTION_WITHOUT_ANNOTATION = "function xx(){}";

    /**
     * 基于注释提取函数，最后一个函数肯定无法提取到,使用时建议提前做一个函数添加到文件末尾
     */
    private static final Pattern JS_FUNCTION_ENTITY_WITH_ANNOTATION = Pattern.compile("(/\\*.*?\\*/)+(.*?)((\\s)|(export)|(async)|(sync))*?function\\s*.*?\\(.*?\\)\\s*\\{.*?\\}(?=((/\\*.*?\\*/)|([^{}]))*?function\\s+.*?\\(.*?\\))",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

    /**
     * JS 函数标志表达式,不包含注释,最后一个函数提取不到，使用前需要手动补一个，否则最后一个函数无法提取到
     */
    private static final Pattern JS_FUNCTION_ENTITY_WITHOUT_ANNOTATION = Pattern.compile("(?<=[^@])function\\s*.*?\\(.*?\\)\\s*\\{.*?\\}(?=((/\\*.*?\\*/)|([^{}]))*?function\\s+.*?\\(.*?\\))",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

    /**
     * JS 函数参数提取
     */
    private static final Pattern JS_FUNCTION_PARAM = Pattern.compile("\\((.*?)\\)");

    /**
     * 起取js函数名称
     */
    private static final Pattern JS_FUNCTION_NAME = Pattern.compile("(/\\*.*?\\*/)*?[^@]*?function\\s*?(.*?)\\(.*?\\)(?=\\s*?\\{)", Pattern.DOTALL);

    /**
     * transport函数提取，最后一个函数无法提取，使用时需做一个函数放到提取出来的内容末尾
     */
    private static final Pattern TRANSPORT_FUNCTION = Pattern.compile("(/\\*\\*\\*[^=>]+?@param*?\\{Array}*?params\\.permissionList*?=*?\\[*'(.*?)'*]\\*/)?([a-z]*?):[a-zA-z,(){}=]*?=>\\{(.+?)},((?=(?:/\\*\\*\\*))|(?=(?:[a-z]*?:.+?=>)))");

    /**
     * transport标准函数
     */
    private static final String TRANSPORT_STANDARD_FUNCTION = "function:config=>{}";

    @Override
    public List<JsFunction> resolveFunction(String jsFileContent) {

        // 按照注释提取JS函数
        List<JsFunction> functionWithAnnotation = divisionContentByFunction(jsFileContent,
                JS_STANDARD_FUNCTION_WITH_ANNOTATION,
                JS_FUNCTION_ENTITY_WITH_ANNOTATION,
                true);

        // 提取未带注释的函数
        List<JsFunction> functionWithOutAnnotation = divisionContentByFunction(jsFileContent,
                JS_STANDARD_FUNCTION_WITHOUT_ANNOTATION,
                JS_FUNCTION_ENTITY_WITHOUT_ANNOTATION,
                false);

        // 函数合并
        for (JsFunction function : functionWithOutAnnotation) {
            if (functionWithAnnotation.stream().noneMatch(jsFunction ->
                    Objects.equals(function.getFunctionName(), jsFunction.getFunctionName()))) {
                functionWithAnnotation.add(function);
            }
        }

        // 提取函数中的参数
        for (JsFunction jsFunction : functionWithAnnotation) {
            String paramsString = RegexUtil.getFirstMatcher(jsFunction.getFunctionEntity(), 1, JS_FUNCTION_PARAM);
            if (!StringUtils.isEmpty(paramsString)) {
                jsFunction.setParamList(Arrays.stream(paramsString.split(",")).map(String::trim).toArray(String[]::new));
            }
        }

        return functionWithAnnotation;

    }

    /**
     * 按函数为单位切js
     *
     * @param content
     * @return
     */
    private List<JsFunction> divisionContentByFunction(String content, String contentSuffix, Pattern functionPattern, Boolean isComment) {

        /**
         * 添加辅助检索字段
         */
        String preHandleContent = content + contentSuffix;

        /**
         * 结果集
         */
        List<JsFunction> functionList = new ArrayList<>();

        /**
         * 正则提取
         */
        Matcher functionMatcher = functionPattern.matcher(preHandleContent);
        while (functionMatcher.find()) {
            String function = functionMatcher.group();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("======================== found  function ==============================");
                LOGGER.debug(function);
            }
            if (isComment) {
                if (!StringUtils.isEmpty(function) && StringUtils.isEmpty(functionMatcher.group(2))) {
                    String functionName = RegexUtil.getFirstMatcher(function, 2, JS_FUNCTION_NAME);
                    functionList.add(new JsFunction(functionName, function));
                }
            } else {
                if (!StringUtils.isEmpty(function)) {
                    String functionName = RegexUtil.getFirstMatcher(function, 2, JS_FUNCTION_NAME);
                    functionList.add(new JsFunction(functionName, function));
                }
            }
        }

        return functionList;
    }

    @Override
    public List<JsFunction> resolveTransport(String jsFileContent) {
        jsFileContent = StringUtils.trimAllWhitespace(jsFileContent);
        List<String> transportList = new ArrayList<>();
        listTransport(jsFileContent, transportList);
        if (CollectionUtils.isEmpty(transportList)) {
            return Collections.emptyList();
        }
        List<JsFunction> result = new ArrayList<>();
        transportList.forEach(functionList -> {
            Matcher functionMatcher = TRANSPORT_FUNCTION.matcher(functionList);
            while (functionMatcher.find()) {
                result.add(new JsFunction(functionMatcher.group(3), functionMatcher.group()));
            }
        });
        return result;
    }

    private void listTransport(String fileContent, List<String> transportList) {
        if (!fileContent.contains(Constants.TRANSPORT)) {
            return;
        }
        String transport = fileContent.substring(fileContent.indexOf(Constants.TRANSPORT));
        char[] chars = transport.toCharArray();
        int left = 0;
        int right = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '{') {
                left++;
            }
            if (chars[i] == '}') {
                right++;
            }
            if (left == right && left > 0) {
                transportList.add(transport.substring(0, i) + TRANSPORT_STANDARD_FUNCTION + "}");
                listTransport(transport.substring(i), transportList);
                return;
            }
        }
    }
}
