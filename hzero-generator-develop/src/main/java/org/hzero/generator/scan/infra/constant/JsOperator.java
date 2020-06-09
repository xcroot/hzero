package org.hzero.generator.scan.infra.constant;

import org.apache.commons.lang3.StringUtils;

/**
 * JS运算符，有其他的运算符请在这里添加
 *
 * @author jianbo.li
 * @date 2019/12/25 11:03
 */
public enum JsOperator {
    // 基本运算符
    ADD("+"),
    DEL("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    SELF_ADD("++"),
    SEL_DEL("--"),
    // 比较符
    GREATER(">"),
    LESS("<"),
    EQUAL("="),
    QUESTION("?"),
    ELSE(":"),
    // 逻辑运算符
    OR("||"),
    AND("&&"),
    NOT("!"),
    // 语句结束符
    STATEMENT(";");
    private String operator;

    JsOperator(String operator) {
        this.operator = operator;
    }


    public String getOperator() {
        return operator;
    }

    /**
     * 写成字符串
     *
     * @param divider 每个枚举之间所用分割符
     * @return
     */
    public static String writeAsString(String divider) {
        return writeAsString(divider,null,null);
    }

    /**
     * 写成字符串
     *
     * @param divider 每个枚举之间所用分割符
     * @param enumPrefix 为其值添加前坠
     * @param enumSuffix 为其值添加后坠
     * @return
     */
    public static String writeAsString(String divider, String enumPrefix, String enumSuffix) {
        JsOperator[] jsOperators = JsOperator.values();

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < jsOperators.length; i++) {
            if (StringUtils.isNotEmpty(enumPrefix)) {
                stringBuilder.append(enumPrefix);
            }
            stringBuilder.append(jsOperators[i].getOperator());
            if (StringUtils.isNotEmpty(enumSuffix)) {
                stringBuilder.append(enumSuffix);
            }
            if (StringUtils.isNotEmpty(divider) && i != jsOperators.length - 1) {
                stringBuilder.append(divider);
            }
        }

        return stringBuilder.toString();
    }
}
