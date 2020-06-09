package org.hzero.generator.entity;

/**
 * 描述:
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2020/01/10 13:35
 */
public class Config {
    private String name;
    private String merge;
    private String targetSchema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMerge() {
        return merge;
    }

    public void setMerge(String merge) {
        this.merge = merge;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }
}
