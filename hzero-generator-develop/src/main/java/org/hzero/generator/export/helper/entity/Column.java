package org.hzero.generator.export.helper.entity;

import java.util.List;
import org.hzero.generator.export.helper.supporter.CellData;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 数据列对应的表列
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 11:02
 */
public class Column {
    private final String SPACE = " ";
    private String columnName;
    private String pkName;
    private boolean autoGenerate = false;
    private boolean id = false;
    private boolean unique = false;
    private boolean cited = false;
    private boolean strickout = false;
    private List<String> lang;
    private String type;
    private ReferenceColumn reference;

    private boolean download;
    private String bucket;

    public boolean isFormula() {
        return reference != null;
    }

    public boolean isMultiLang() {
        return !CollectionUtils.isEmpty(lang);
    }

    public CellData.CellStyle getCellStyle() {
        if (autoGenerate) {
            return CellData.CellStyle.ORANGE;
        }
        if (id) {
            return CellData.CellStyle.ORANGE;
        }
        if (unique) {
            return CellData.CellStyle.BLUE;
        }
        if (strickout) {
            return CellData.CellStyle.STRICKOUT;
        }
        if (isFormula()) {
            return CellData.CellStyle.GREEN;
        }
        return null;
    }

    /**
     * 是否是固定值
     * @return boolean
     */
    public boolean isConstant(){
        return org.apache.commons.lang3.StringUtils.contains(columnName, SPACE);
    }
    public String getColumnNameText() {
        StringBuilder sb = new StringBuilder();
        if (autoGenerate) {
            sb.append("*");
        } else if (id) {
            sb.append("*");
        } else if (unique) {
            sb.append("#");
        }
        if(org.apache.commons.lang3.StringUtils.contains(columnName, SPACE)) {
            sb.append(org.apache.commons.lang3.StringUtils.split(columnName, SPACE)[1]);
        } else {
            sb.append(columnName);
        }
        if (StringUtils.hasText(type)) {
            sb.append("(").append(type).append(")");
        }
        return sb.toString();
    }

    public String getColumnName() {
        if (org.apache.commons.lang3.StringUtils.contains(columnName, SPACE)){
            return org.apache.commons.lang3.StringUtils.split(columnName, SPACE)[0];
        }
        return columnName;
    }

    public Column setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public Column setAutoGenerate(boolean autoGenerate) {
        this.autoGenerate = autoGenerate;
        return this;
    }

    public Column autoGenerate(boolean autoGenerate) {
        this.autoGenerate = autoGenerate;
        return this;
    }

    public boolean isId() {
        return id;
    }

    public Column setId(boolean id) {
        this.id = id;
        return this;
    }

    public Column id(boolean id) {
        this.id = id;
        return this;
    }

    public String getPkName() {
        return pkName;
    }

    public Column setPkName(String pkName) {
        this.pkName = pkName;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public Column setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public List<String> getLang() {
        return lang;
    }

    public Column setLang(List<String> lang) {
        this.lang = lang;
        return this;
    }

    public ReferenceColumn getReference() {
        return reference;
    }

    public Column setReference(ReferenceColumn reference) {
        this.reference = reference;
        return this;
    }

    public boolean isCited() {
        return cited;
    }

    public Column setCited(boolean cited) {
        this.cited = cited;
        return this;
    }

    public boolean isRef() {
        return reference != null;
    }

    public String getType() {
        return type;
    }

    public Column setType(String type) {
        this.type = type;
        return this;
    }

    public boolean getStrickout() {
        return strickout;
    }

    public void setStrickout(boolean strickout) {
        this.strickout = strickout;
    }

    public boolean isDownload() {
        return download;
    }

    public Column setDownload(boolean download) {
        this.download = download;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public Column setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }
}
