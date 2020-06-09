package org.hzero.generator.export.helper.entity;

/**
 * <p>
 * 关联列
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 11:13
 */
public class ReferenceColumn {
    private String sheetName;
    private String tableName;
    private String columnName;

    public ReferenceColumn(String sheetName, String tableName, String columnName) {
        this.sheetName = sheetName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public ReferenceColumn(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public ReferenceColumn(String columnName) {
        this.columnName = columnName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }
}
