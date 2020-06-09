package org.hzero.generator.scan.infra.util;

/**
 * @author jianbo.li
 * @date 2020/1/2 12:39
 */
public class JsUtil {
    /**
     * es6 变量引用
     *
     * @param param
     * @return
     */
    public static String setPlaceHolder(String param) {
        return "${" + param + "}";
    }
}
