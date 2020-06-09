package org.hzero.generator.scan.infra.resolver.impl;

import org.hzero.generator.scan.domain.JsVariable;
import org.hzero.generator.scan.infra.resolver.JsVariableResolver;
import org.hzero.generator.scan.infra.util.RegexUtil;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author jianbo.li
 * @date 2019/12/26 17:10
 */
public class DefaultJsVariableResolverImpl implements JsVariableResolver {

    public static final Pattern VARIABLE_REGEX = Pattern.compile("((var)|(let)|(const))+?\\s+?(.*?=.*?);", Pattern.DOTALL);

    @Override
    public List<JsVariable> resolverVariable(String content) {
        List<String> variableKvs = RegexUtil.getAllMatcher(content, 5, VARIABLE_REGEX);
        if (CollectionUtils.isEmpty(variableKvs)) {
            return Collections.emptyList();
        }
        return variableKvs.stream().map(kv -> {
            String paramName = kv.substring(0, kv.indexOf("="));
            String paramValue = kv.substring(kv.indexOf("=") + 1, kv.length());
            return new JsVariable(paramName.trim(), paramValue.trim());
        }).collect(Collectors.toList());
    }

}
