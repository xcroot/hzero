package org.hzero.generator.export.helper.enums;

/**
 * <p>
 * Liquibase Helper Mode
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 9:42
 */
public enum LiquibaseEngineMode {
    /**
     * 追加到已存在文件中
     */
    APPEND,
    /**
     * 创建新的文件
     */
    CREATE,
    /**
     * 覆盖已存在文件
     */
    OVERRIDE
}
