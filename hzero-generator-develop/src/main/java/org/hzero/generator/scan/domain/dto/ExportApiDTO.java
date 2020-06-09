package org.hzero.generator.scan.domain.dto;

/**
 * description
 *
 * @author fanghan.liu 2020/02/14 14:46
 */
public class ExportApiDTO {

    /**
     * 主键
     */
    private Long uiApiId;
    /**
     * 页面路由
     */
    private String uiRoute;
    /**
     * 按钮id
     */
    private Long uiCompId;
    /**
     * 按钮编码
     */
    private String compCode;
    /**
     * API路径
     */
    private String path;
    /**
     * HTTP方法
     */
    private String method;
    /**
     * API层级
     */
    private String apiLevel;
    /**
     * API所属服务名
     */
    private String serviceName;

    public ExportApiDTO() {
    }

    @Override
    public String toString() {
        return "ExportApiDTO{" +
                "uiApiId=" + uiApiId +
                ", uiRoute='" + uiRoute + '\'' +
                ", uiCompId=" + uiCompId +
                ", compCode='" + compCode + '\'' +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", apiLevel='" + apiLevel + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }

    public Long getUiApiId() {
        return uiApiId;
    }

    public ExportApiDTO setUiApiId(Long uiApiId) {
        this.uiApiId = uiApiId;
        return this;
    }

    public String getUiRoute() {
        return uiRoute;
    }

    public ExportApiDTO setUiRoute(String uiRoute) {
        this.uiRoute = uiRoute;
        return this;
    }

    public Long getUiCompId() {
        return uiCompId;
    }

    public ExportApiDTO setUiCompId(Long uiCompId) {
        this.uiCompId = uiCompId;
        return this;
    }

    public String getCompCode() {
        return compCode;
    }

    public ExportApiDTO setCompCode(String compCode) {
        this.compCode = compCode;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ExportApiDTO setPath(String path) {
        this.path = path;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public ExportApiDTO setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public ExportApiDTO setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ExportApiDTO setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }
}
