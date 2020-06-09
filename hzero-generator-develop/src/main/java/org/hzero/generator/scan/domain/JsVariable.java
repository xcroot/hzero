package org.hzero.generator.scan.domain;

import org.hzero.generator.scan.infra.constant.Constants;

/**
 * @author jianbo.li
 * @date 2019/12/24 11:38
 */
public class JsVariable {

    public JsVariable(String name,String value){
        this.name = name;
        this.value =value;
    }

    /**
     * 参数名称
     */
    private String name;
    /**
     * 参数值
     */
    private String value;

    /**
     * 作用范围： A,P
     */
    private String scope = Constants.VariableScope.A;

    /**
     * 范围值： 可能是函数名称，maybe null
     */
    private String scopeValue;

    public String getName() {
        return name.trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "JsVariable{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue) {
        this.scopeValue = scopeValue;
    }
}
