package org.hzero.generator.execute;

import java.sql.SQLException;
import java.util.List;

/**
 * @author liguo.wang
 */
public abstract class AbstractExecutor {

    public String dbUrl;
    public String username;
    public String password;

    public AbstractExecutor(String dbUrl, String username, String password) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * 执行
     * @param contents 内容
     * @return 成功失败
     * @throws SQLException e
     */
    public abstract boolean execute(List<String> contents) throws SQLException;
}
