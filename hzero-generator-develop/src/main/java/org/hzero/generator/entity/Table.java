package org.hzero.generator.entity;

import java.util.List;

/**
 * 描述:
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/29 15:44
 */
public class Table {
    private String name;
    private String description;
    private String sql;
    private String id;
    private String cited;
    private String unique;
    private List<Type> types;
    private List<Lang> langs;
    private List<Reference> references;

    private String download;
    private String bucket;

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

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCited() {
        return cited;
    }

    public void setCited(String cited) {
        this.cited = cited;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }

    public List<Lang> getLangs() {
        return langs;
    }

    public void setLangs(List<Lang> langs) {
        this.langs = langs;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public String getDownload() {
        return download;
    }

    public Table setDownload(String download) {
        this.download = download;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public Table setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }
}
