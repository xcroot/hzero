package org.hzero.generator.scan.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * 页面组件
 *
 * @author fanghan.liu 2020/01/10 15:14
 */
public class UiComponent {

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
    /**
     * 所属页面
     */
    private String pageBelonged;

    // -----------------

    /**
     * @return 多语言key
     */
    public String getPromptKey() {
        return StringUtils.substring(compCode, 0, StringUtils.ordinalIndexOf(compCode, ".", 2));
    }

    /**
     * @return 多语言code
     */
    public String getPromptCode() {
        return StringUtils.substring(compCode, StringUtils.ordinalIndexOf(compCode, ".", 2) + 1);
    }

    // -----------------

    public UiComponent() {
    }

    public UiComponent(String uiRoute, String compCode, String description, String compType) {
        this.uiRoute = uiRoute;
        this.compCode = compCode;
        this.description = description;
        this.compType = compType;
    }

    public Long getUiCompId() {
        return uiCompId;
    }

    public UiComponent setUiCompId(Long uiCompId) {
        this.uiCompId = uiCompId;
        return this;
    }

    public String getUiRoute() {
        return uiRoute;
    }

    public UiComponent setUiRoute(String uiRoute) {
        this.uiRoute = uiRoute;
        return this;
    }

    public String getCompCode() {
        return compCode;
    }

    public UiComponent setCompCode(String compCode) {
        this.compCode = compCode;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UiComponent setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCompType() {
        return compType;
    }

    public UiComponent setCompType(String compType) {
        this.compType = compType;
        return this;
    }

    public String getPageBelonged() {
        return pageBelonged;
    }

    public UiComponent setPageBelonged(String pageBelonged) {
        this.pageBelonged = pageBelonged;
        return this;
    }

    @Override
    public String toString() {
        return "UiComponent{" +
                "uiCompId=" + uiCompId +
                ", uiRoute='" + uiRoute + '\'' +
                ", compCode='" + compCode + '\'' +
                ", description='" + description + '\'' +
                ", compType='" + compType + '\'' +
                ", pageBelonged='" + pageBelonged + '\'' +
                '}';
    }
}