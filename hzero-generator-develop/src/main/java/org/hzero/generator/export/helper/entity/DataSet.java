package org.hzero.generator.export.helper.entity;

import java.util.Map;
import java.util.Set;

/**
 * @Description 数据集
 * @Date 2020/2/11 15:35
 * @Author wanshun.zhang@hand-china.com
 */
public class DataSet {

    private String sheetName;
    private String tableName;
    private Data data;
    private Set<Map<String, Object>> dataSet;

    public String getSheetName() {
        return sheetName;
    }

    public DataSet setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public DataSet setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public Data getData() {
        return data;
    }

    public DataSet setData(Data data) {
        this.data = data;
        return this;
    }

    public Set<Map<String, Object>> getDataSet() {
        return dataSet;
    }

    public DataSet setDataSet(Set<Map<String, Object>> dataSet) {
        this.dataSet = dataSet;
        return this;
    }
}
