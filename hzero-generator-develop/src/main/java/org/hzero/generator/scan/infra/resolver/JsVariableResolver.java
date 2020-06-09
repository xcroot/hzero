package org.hzero.generator.scan.infra.resolver;


import org.hzero.generator.scan.domain.JsVariable;

import java.util.List;

/**
 *
 * 解析js中存在的变量
 *
 * @author jianbo.li
 * @date 2019/12/24 11:33
 */
public interface JsVariableResolver extends Resolver {

    /**
     * 解析指定字符串中存在的js变量
     * @param content
     * @return
     */
    List<JsVariable> resolverVariable(String content);

}
