package org.hzero.generator.export.helper.entity;

import java.util.List;

/**
 * <p>
 * Data Group ： 数据分组，一组数据可以包含多张表，一组数据将被写入再同一张
 * 建议将多张有关联的表放在统一分组中
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 10:49
 */
public class DataGroup {
    private String sheetName;
    private List<Data> dataList;

    public String getSheetName() {
        return sheetName;
    }

    public DataGroup setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public DataGroup setDataList(List<Data> dataList) {
        this.dataList = dataList;
        return this;
    }
}
