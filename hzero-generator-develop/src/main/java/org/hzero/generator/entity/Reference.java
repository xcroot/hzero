package org.hzero.generator.entity;

import org.hzero.generator.export.helper.entity.ReferenceColumn;

/**
 * 描述:
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/29 15:45
 */
public class Reference {
    private String field;
    private String sheetName;
    private String tableName;
    private String columnName;
    
    public ReferenceColumn getReference() {
    	return new ReferenceColumn(this.sheetName,this.tableName, this.columnName);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
