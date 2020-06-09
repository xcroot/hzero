package org.hzero.generator.entity;

import java.util.List;

/**
 * 描述:
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/29 15:43
 */
public class Excel {
    List<Sheet> sheetList;
    private String name;
    private String description;
    private String fileName;
    private String schema;

    public List<Sheet> getSheetList() {
        return sheetList;
    }

    public void setSheetList(List<Sheet> sheetList) {
        this.sheetList = sheetList;
    }

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
