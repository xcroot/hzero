package org.hzero.generator.export.helper.exception;

/**
 * <p>
 * Liquibase Helper Exception
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 9:51
 */
public class LiquibaseHelperException extends RuntimeException {
    public LiquibaseHelperException(String message) {
        super(message);
    }

    public LiquibaseHelperException(String message, Throwable cause) {
        super(message, cause);
    }
}
