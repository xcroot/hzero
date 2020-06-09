package org.hzero.generator.scan.domain;

/**
 * 页面API
 *
 * @author fanghan.liu 2020/01/10 15:03
 */
public class UiApi {

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

    public UiApi() {
    }

    public UiApi(String path, String method) {
        this.path = path;
        this.method = method;
    }

    public Long getUiApiId() {
        return uiApiId;
    }

    public UiApi setUiApiId(Long uiApiId) {
        this.uiApiId = uiApiId;
        return this;
    }

    public String getUiRoute() {
        return uiRoute;
    }

    public UiApi setUiRoute(String uiRoute) {
        this.uiRoute = uiRoute;
        return this;
    }

    public Long getUiCompId() {
        return uiCompId;
    }

    public UiApi setUiCompId(Long uiCompId) {
        this.uiCompId = uiCompId;
        return this;
    }

    public String getPath() {
        return path;
    }

    public UiApi setPath(String path) {
        this.path = path;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public UiApi setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public UiApi setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public UiApi setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @Override
    public String toString() {
        return "UiApi{" +
                "uiApiId=" + uiApiId +
                ", uiRoute='" + uiRoute + '\'' +
                ", uiCompId=" + uiCompId +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", apiLevel='" + apiLevel + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}