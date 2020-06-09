package org.hzero.generator.entity;

import java.util.List;

/**
 * 描述:
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/29 15:43
 */

public class Sheet {
    private String name;
    private String description;
    private String version;
    private List<Table> tableList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Table> getTableList() {
        return tableList;
    }

    public void setTableList(List<Table> tableList) {
        this.tableList = tableList;
    }
}
