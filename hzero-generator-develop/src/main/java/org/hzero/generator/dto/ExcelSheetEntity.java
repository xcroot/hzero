package org.hzero.generator.dto;

import java.util.List;
import java.util.Map;

/**
 * 
 * Excel的Sheet页
 * 
 * @author xianzhi.chen@hand-china.com 2018年7月20日下午2:24:13
 */
public class ExcelSheetEntity {

    private String sheetName;
    private String tableName;
    private List<Map<String, String>> sheetDatas;

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

    public List<Map<String, String>> getSheetDatas() {
        return sheetDatas;
    }

    public void setSheetDatas(List<Map<String, String>> sheetDatas) {
        this.sheetDatas = sheetDatas;
    }

}
