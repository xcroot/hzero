package org.hzero.generator.scan.domain.dto;

/**
 * 导出组件DTO
 *
 * @author fanghan.liu 2020/02/14 17:14
 */
public class ExportComponentDTO {

    private Long uiCompId;
    private String uiRoute;
    private String compCode;
    private String description;
    private String compType;

    public ExportComponentDTO() {
    }

    @Override
    public String toString() {
        return "ExportComponentDTO{" +
                "uiCompId=" + uiCompId +
                ", uiRoute='" + uiRoute + '\'' +
                ", compCode='" + compCode + '\'' +
                ", description='" + description + '\'' +
                ", compType='" + compType + '\'' +
                '}';
    }

    public Long getUiCompId() {
        return uiCompId;
    }

    public ExportComponentDTO setUiCompId(Long uiCompId) {
        this.uiCompId = uiCompId;
        return this;
    }

    public String getUiRoute() {
        return uiRoute;
    }

    public ExportComponentDTO setUiRoute(String uiRoute) {
        this.uiRoute = uiRoute;
        return this;
    }

    public String getCompCode() {
        return compCode;
    }

    public ExportComponentDTO setCompCode(String compCode) {
        this.compCode = compCode;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExportComponentDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCompType() {
        return compType;
    }

    public ExportComponentDTO setCompType(String compType) {
        this.compType = compType;
        return this;
    }
}
