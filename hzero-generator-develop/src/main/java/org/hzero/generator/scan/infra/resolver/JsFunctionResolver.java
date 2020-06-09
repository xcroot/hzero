package org.hzero.generator.scan.infra.resolver;


import org.hzero.generator.scan.domain.JsFunction;

import java.util.List;

/**
 * @author jianbo.li
 * @date 2019/12/23 11:08
 */
public interface JsFunctionResolver extends Resolver {

    /**
     * 将js文件内容解析为函数集合
     * @param jsFileContent js 文件内容
     * @return 返回提取的函数集合
     */
    List<JsFunction> resolveFunction(String jsFileContent);

    /**
     * 解析c7n transport中的函数
     * @param jsFileContent js 文件内容
     * @return 返回提取的函数集合
     */
    List<JsFunction> resolveTransport(String jsFileContent);
}
