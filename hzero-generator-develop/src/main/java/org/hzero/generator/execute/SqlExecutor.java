package org.hzero.generator.execute;

import org.hzero.generator.util.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * @author wlg
 */
public class SqlExecutor extends AbstractExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutor.class);

    private String dbType;

    public SqlExecutor(String dbUrl, String username, String password, String dbType) {
        super(dbUrl, username, password);
        this.dbType = dbType;
    }

    @Override
    public boolean execute(List<String> contents) throws SQLException {

        Connection conn = JDBCUtils.getConnection(dbType, dbUrl, username, password);
        Statement stmt;
        try {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            for (String sql : contents) {
                stmt.addBatch(sql);
            }
            int[] rows = stmt.executeBatch();
            logger.info("Row count:" + Arrays.toString(rows));
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
        } finally {
            conn.close();
        }
        return false;
    }
}
