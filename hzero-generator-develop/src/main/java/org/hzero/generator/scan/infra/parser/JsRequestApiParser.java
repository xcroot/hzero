package org.hzero.generator.scan.infra.parser;


import org.hzero.generator.scan.domain.JsFunction;
import org.hzero.generator.scan.domain.JsVariable;
import org.hzero.generator.scan.domain.UiApi;

import java.util.List;

/**
 * @author jianbo.li
 * @date 2019/12/23 15:27
 */
public interface JsRequestApiParser extends Parser {
    /**
     * 解析函数引用了的API
     *
     * @param jsFunction       函数
     * @param jsFunctionList js函数列表
     * @param jsVariableList js变量列表
     * @param content        函数所处整个js文件内容
     * @return
     */
    List<UiApi> parseJsApi(JsFunction jsFunction, List<JsFunction> jsFunctionList, List<JsVariable> jsVariableList, String content);

    /**
     * 解析c7n函数引用了的API
     *
     * @param jsFunction       函数
     * @param jsFunctionList js函数列表
     * @param jsVariableList js变量列表
     * @param content        函数所处整个js文件内容
     * @return
     */
    List<UiApi> parseTransportApi(JsFunction jsFunction, List<JsFunction> jsFunctionList, List<JsVariable> jsVariableList, String content);
}
