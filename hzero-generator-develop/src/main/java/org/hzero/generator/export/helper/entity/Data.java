package org.hzero.generator.export.helper.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 表数据，一个Data对应一张表
 * </p>
 * @author wanshun.zhang 2019/11/28
 * @author qingsheng.chen 2018/11/24 星期六 10:55
 */
public class Data {
    private static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String serviceName;
    private String schemaName;
    private String tableName;
    private LocalDate creationDate;
    private String author;
    private String description;
    private List<Column> columnList;
    private String where;

    public String getTableName() {
        return tableName;
    }

    public Data setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public LocalDate getCreationDate() {
        if (creationDate == null){
            creationDate = LocalDate.now();
        }
        return creationDate;
    }

    public String getCreationDateText(){
        return DEFAULT_FORMAT.format(getCreationDate());
    }

    public Data setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Data setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Data setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public Data setColumnList(List<Column> columnList) {
        this.columnList = columnList;
        return this;
    }

    public String getWhere() {
        return where;
    }

    public Data setWhere(String where) {
        this.where = where;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Data setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }
}
