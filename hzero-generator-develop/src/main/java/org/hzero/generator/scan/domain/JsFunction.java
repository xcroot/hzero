package org.hzero.generator.scan.domain;

/**
 * @author jianbo.li
 * @date 2019/12/23 11:29
 */
public class JsFunction {
    /**
     * 函数名称
     */
    private String functionName;

    /**
     * 参数列表
     */
    private String[] paramList;

    /**
     * 函数实体
     */
    private String functionEntity;


    public JsFunction(String functionName,String functionEntity){
        this.functionName = functionName;
        this.functionEntity = functionEntity;
    }


    public String getFunctionName() {
        return functionName.trim();
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionEntity() {
        return functionEntity;
    }

    public void setFunctionEntity(String functionEntity) {
        this.functionEntity = functionEntity;
    }

    public String[] getParamList() {
        return paramList;
    }

    public void setParamList(String[] paramList) {
        this.paramList = paramList;
    }
}
