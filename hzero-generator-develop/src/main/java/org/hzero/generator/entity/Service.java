package org.hzero.generator.entity;

import java.util.List;

/**
 * 描述:xml服务对应实体类
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/29 11:26
 */
public class Service {

    private String name;
    private int order;
    private String description;
    private List<Excel> excelList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Excel> getExcelList() {
        return excelList;
    }

    public void setExcelList(List<Excel> excelList) {
        this.excelList = excelList;
    }
}




