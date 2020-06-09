package org.hzero.generator.dto;

import java.util.List;

/**
 * 
 * 索引信息实体类
 * @author xianzhi.chen@hand-china.com	2018年7月18日下午5:18:58
 */
public class IndexEntity {
    
    // 表名
    private String tableName;
    // 索引名称
    private String indexName;
    // 类型
    private String indexType;
    // 索引字段
    private String indexColumn;
    // 索引行
    private List<String> indexColumns;
    
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getIndexName() {
        return indexName;
    }
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    public String getIndexType() {
        return indexType;
    }
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }
    public String getIndexColumn() {
        return indexColumn;
    }
    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }
    public List<String> getIndexColumns() {
        return indexColumns;
    }
    public void setIndexColumns(List<String> indexColumns) {
        this.indexColumns = indexColumns;
    }
    
}
