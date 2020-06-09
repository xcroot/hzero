package org.hzero.generator.scan.domain.vo;

/**
 * 页面组件
 *
 * @author fanghan.liu 2020/01/10 15:14
 */
public class UiComponentVO {

    /**
     * 主键
     */
    private Long uiCompId;
    /**
     * 页面路由
     */
    private String uiRoute;
    /**
     * 组件编码（permissionCode）
     */
    private String compCode;
    /**
     * 描述
     */
    private String description;
    /**
     * 组件类型 button/lov/prompt
     */
    private String compType;

    public UiComponentVO() {
    }

    public UiComponentVO(String uiRoute, String compCode, String description, String compType) {
        this.uiRoute = uiRoute;
        this.compCode = compCode;
        this.description = description;
        this.compType = compType;
    }

    @Override
    public String toString() {
        return "UiComponentVO{" +
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

    public UiComponentVO setUiCompId(Long uiCompId) {
        this.uiCompId = uiCompId;
        return this;
    }

    public String getUiRoute() {
        return uiRoute;
    }

    public UiComponentVO setUiRoute(String uiRoute) {
        this.uiRoute = uiRoute;
        return this;
    }

    public String getCompCode() {
        return compCode;
    }

    public UiComponentVO setCompCode(String compCode) {
        this.compCode = compCode;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UiComponentVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCompType() {
        return compType;
    }

    public UiComponentVO setCompType(String compType) {
        this.compType = compType;
        return this;
    }

}