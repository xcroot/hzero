package org.hzero.generator.scan.domain;

/**
 * @author wuguokai
 * @author allen modified 2018/06/29
 */
public class MenuPermission {
    public static final String FIELD_MENU_ID = "menuId";
    private Long id;
    private Long menuId;
    private String permissionCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
}
