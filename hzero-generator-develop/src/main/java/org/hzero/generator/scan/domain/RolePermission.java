package org.hzero.generator.scan.domain;

import org.hzero.generator.scan.infra.constant.RolePermissionType;

import java.util.Set;

/**
 * 角色权限集关系，permission_id 用来表示权限集，Choerodon表示的是权限，注意区分。
 */
public class RolePermission {

    public static final String FIELD_ROLE_ID = "roleId";
    public static final String FIELD_CREATE_FLAG = "createFlag";
    public static final String FIELD_INHERIT_FLAG = "inheritFlag";

    /**
     * 默认为权限集类型
     */
    public static final RolePermissionType DEFAULT_TYPE = RolePermissionType.PS;

    public RolePermission() {}

    /**
     *
     * @param roleId 角色ID
     * @param permissionSetId 权限集ID
     * @param inheritFlag 继承标识
     * @param createFlag 创建标识
     */
    public RolePermission(Long roleId, Long permissionSetId, String inheritFlag, String createFlag, String type) {
        this.roleId = roleId;
        this.permissionSetId = permissionSetId;
        this.inheritFlag = inheritFlag;
        this.createFlag = createFlag;
        this.type = type;
    }

    //
    // getter/setter
    // ------------------------------------------------------------------------------

    private Long id;
    private Long roleId;
    /**
     * 该权限ID表示权限集ID
     */
    private Long permissionSetId;
    private String type;
    private String createFlag;
    private String inheritFlag;



    public Long getId() {
        return id;
    }

    public RolePermission setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getRoleId() {
        return roleId;
    }

    public RolePermission setRoleId(Long roleId) {
        this.roleId = roleId;
        return this;
    }

    public Long getPermissionSetId() {
        return permissionSetId;
    }

    public RolePermission setPermissionSetId(Long permissionSetId) {
        this.permissionSetId = permissionSetId;
        return this;
    }

    public String getType() {
        return type;
    }

    public RolePermission setType(String type) {
        this.type = type;
        return this;
    }

    public String getCreateFlag() {
        return createFlag;
    }

    public RolePermission setCreateFlag(String createFlag) {
        this.createFlag = createFlag;
        return this;
    }

    public String getInheritFlag() {
        return inheritFlag;
    }

    public RolePermission setInheritFlag(String inheritFlag) {
        this.inheritFlag = inheritFlag;
        return this;
    }

}
