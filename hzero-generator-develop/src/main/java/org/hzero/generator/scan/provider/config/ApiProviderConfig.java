package org.hzero.generator.scan.provider.config;


import org.hzero.generator.scan.infra.parser.JsRequestApiParser;
import org.hzero.generator.scan.infra.parser.impl.DefaultJsRequestApiParserImpl;
import org.hzero.generator.scan.infra.resolver.JsFunctionResolver;
import org.hzero.generator.scan.infra.resolver.JsVariableResolver;
import org.hzero.generator.scan.infra.resolver.impl.DefaultJSFunctionResolverImpl;
import org.hzero.generator.scan.infra.resolver.impl.DefaultJsVariableResolverImpl;

/**
 * @author jianbo.li
 * @date 2019/12/24 11:58
 */
public class ApiProviderConfig implements ProviderConfig{
    /**
     * 配置js函数解析器
     */
    private JsFunctionResolver jsFunctionResolver = new DefaultJSFunctionResolverImpl();
    /**
     * 配置js变量解析器
     */
    private JsVariableResolver jsVariableResolver = new DefaultJsVariableResolverImpl();
    /**
     * 配置js API分析器
     */
    private JsRequestApiParser jsRequestApiParser = new DefaultJsRequestApiParserImpl();

    public JsFunctionResolver getJsFunctionResolver() {
        return jsFunctionResolver;
    }

    public void setJsFunctionResolver(JsFunctionResolver jsFunctionResolver) {
        this.jsFunctionResolver = jsFunctionResolver;
    }

    public JsVariableResolver getJsVariableResolver() {
        return jsVariableResolver;
    }

    public void setJsVariableResolver(JsVariableResolver jsVariableResolver) {
        this.jsVariableResolver = jsVariableResolver;
    }

    public JsRequestApiParser getJsRequestApiParser() {
        return jsRequestApiParser;
    }

    public void setJsRequestApiParser(JsRequestApiParser jsRequestApiParser) {
        this.jsRequestApiParser = jsRequestApiParser;
    }
}
