package org.hzero.generator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 获取JDBC连接
 *
 * @author xiaoyu.zhao@hand-china.com 2019/10/18 15:01
 */
public class JDBCUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCUtils.class);

    /**
     * 数据库JDBC驱动
     */
    private static final String DRIVER_CLASS_MYSQL = "com.mysql.jdbc.Driver";
    private static final String DRIVER_CLASS_SQLSERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DRIVER_CLASS_ORACLE = "oracle.jdbc.driver.OracleDriver";

    /**
     * 获取数据库JDBC连接
     *
     * @param dbType 数据库类型
     * @param dbUrl 数据库连接URL
     * @param username 用户名
     * @param password 密码
     * @return JDBC连接
     */
    public static Connection getConnection(String dbType, String dbUrl, String username, String password) {
        switch(dbType) {
            case "MySql":
            case "TiDB":
                return getJDBCConnection(dbUrl, username, password, DRIVER_CLASS_MYSQL);
            case "SqlServer":
                return getJDBCConnection(dbUrl, username, password, DRIVER_CLASS_ORACLE);
            case "Oracle":
                return getJDBCConnection(dbUrl, username, password, DRIVER_CLASS_SQLSERVER);
            default:
                return getJDBCConnection(dbUrl, username, password, DRIVER_CLASS_MYSQL);
        }
    }

    /**
     * 获取JDBC连接
     *
     * @param dbUrl 数据库连接URL
     * @param username 用户名
     * @param password 密码
     * @param driverClass 数据库驱动
     * @return 数据库连接
     */
    private static Connection getJDBCConnection(String dbUrl, String username, String password, String driverClass) {
        Connection connection = null;
        try{
            Class.forName(driverClass);
            connection = DriverManager.getConnection(dbUrl, username, password);
        } catch (ClassNotFoundException e) {
            LOGGER.error(">>>>>>>>>>>>>>>>>driver class not found, the driver class is : {}, the exception is : {}",
                    driverClass, e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            LOGGER.error(">>>>>>>>>>>>>>>>>driverManager get connection failed, the dbUrl is : {}, username is : {}, password is : {}, the exception is : {}",
                dbUrl, username, password, e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}
