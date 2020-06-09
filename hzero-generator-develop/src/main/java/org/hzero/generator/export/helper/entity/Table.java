package org.hzero.generator.export.helper.entity;

/**
 * <p>
 *
 * </p>
 *
 * @author qingsheng.chen 2018/12/3 星期一 20:44
 */
public class Table {
    private String schemaName;
    private String tableName;

    public String getSchemaName() {
        return schemaName;
    }

    public Table setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public Table setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
}
