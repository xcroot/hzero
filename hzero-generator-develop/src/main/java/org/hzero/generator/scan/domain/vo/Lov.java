package org.hzero.generator.scan.domain.vo;

/**
 * Lov
 *
 * @author bojiangzhou 2019/01/29
 */
public class Lov {

    private Long lovId;
    private String lovCode;
    private String lovTypeCode;
    private String lovName;
    private Long tenantId;

    public Long getLovId() {
        return lovId;
    }

    public void setLovId(Long lovId) {
        this.lovId = lovId;
    }

    public String getLovCode() {
        return lovCode;
    }

    public void setLovCode(String lovCode) {
        this.lovCode = lovCode;
    }

    public String getLovTypeCode() {
        return lovTypeCode;
    }

    public void setLovTypeCode(String lovTypeCode) {
        this.lovTypeCode = lovTypeCode;
    }

    public String getLovName() {
        return lovName;
    }

    public void setLovName(String lovName) {
        this.lovName = lovName;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
