package org.hzero.generator.scan.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jianbo.li
 * @date 2019/12/13 9:58
 */
public class UiPermissionApi {

    /**
     * ui权限代码
     */
    private String permissionCode;

    /**
     * 权限代码关联的apis
     */
    private List<UiApi> apis = new ArrayList<>();

    private String pageRoute;

    public UiPermissionApi(String permissionCode){
        this.permissionCode = permissionCode;
    }

    public UiPermissionApi(String permissionCode,List<UiApi> jsRequestApiList){
        this.permissionCode = permissionCode;
        this.apis = jsRequestApiList;
    }

    public UiPermissionApi(String permissionCode, String pageRoute) {
        this.permissionCode = permissionCode;
        this.pageRoute = pageRoute;
    }

    public UiPermissionApi(String permissionCode, List<UiApi> apis, String pageRoute) {
        this.permissionCode = permissionCode;
        this.apis = apis;
        this.pageRoute = pageRoute;
    }

    public UiPermissionApi() {
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public List<UiApi> getApis() {
        return apis;
    }

    public void setApis(List<UiApi> apis) {
        this.apis = apis;
    }

    public String getPageRoute() {
        return pageRoute;
    }

    public UiPermissionApi setPageRoute(String pageRoute) {
        this.pageRoute = pageRoute;
        return this;
    }
}
