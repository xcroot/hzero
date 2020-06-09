package org.hzero.generator.scan.domain;

/**
 * 增加了继承角色ID、父级三维角色定位字段。
 *
 * @author bojiangzhou 2018/07/04
 */
public class Role{

    public static final String FIELD_ID = "id";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_LEVEL_PATH = "levelPath";
    public static final String FIELD_INHERIT_LEVEL_PATH = "inheritLevelPath";
    public static final String FIELD_IS_ENABLED = "isEnabled";
    public static final String FIELD_ROLE_ID = "roleId";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_PARENT_ROLE_ID = "parentRoleId";
    public static final String FIELD_INHERIT_ROLE_ID = "inheritRoleId";
    public static final String FIELD_PARENT_ROLE_ASSIGN_LEVEL = "parentRoleAssignLevel";
    public static final String FIELD_PARENT_ROLE_ASSIGN_LEVEL_VALUE = "parentRoleAssignLevelValue";
    public static final String FIELD_BUILD_IN = "isBuiltIn";
    public static final String FIELD_CREATED_BY_TENANT_ID = "createdByTenantId";

    public static final Long ROOT_ID = 0L;
    public static final String DEFAULT_SUPPER_ROLE_LEVEL_PATH_PREFIX = "0.organization.0";

    public static final Long SITE_ADMIN_ID = 1L;
    public static final Long TENANT_ADMIN_ID = 2L;

    public static final String ZH_CN_DEFAULT_ROLE_NAME = "管理员角色";
    public static final String EN_DEFAULT_ROLE_NAME = "Admin Role";
    public static final String ZH_CN_TPL_ROLE_NAME_SUFFIX = "模板";
    public static final String EN_TPL_ROLE_NAME_SUFFIX = "template";

    //
    // getter/setter
    // ------------------------------------------------------------------------------

    private Long id;
    private String name;
    private String code;
    private String description;
    private String level;
    private Boolean isEnabled;
    private Boolean isModified;
    private Boolean isEnableForbidden;
    private Boolean isBuiltIn;
    private Boolean isAssignable;
    private Long objectVersionNumber;

    private Long tenantId;
    private Long inheritRoleId;
    private Long parentRoleId;
    private String parentRoleAssignLevel;
    private Long parentRoleAssignLevelValue;
    /**
     * 父子层级关系
     */
    private String levelPath;
    /**
     * 继承层级关系
     */
    private String inheritLevelPath;
    /**
     * 创建者租户ID
     */
    private Long createdByTenantId;

    public Long getId() {
        return id;
    }

    public Role setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Role setName(String name) {
        this.name = name;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Role setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Role setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public Role setLevel(String level) {
        this.level = level;
        return this;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public Role setEnabled(Boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public Boolean getModified() {
        return isModified;
    }

    public Role setModified(Boolean modified) {
        isModified = modified;
        return this;
    }

    public Boolean getEnableForbidden() {
        return isEnableForbidden;
    }

    public Role setEnableForbidden(Boolean enableForbidden) {
        isEnableForbidden = enableForbidden;
        return this;
    }

    public Boolean getBuiltIn() {
        return isBuiltIn;
    }

    public Role setBuiltIn(Boolean builtIn) {
        isBuiltIn = builtIn;
        return this;
    }

    public Boolean getAssignable() {
        return isAssignable;
    }

    public Role setAssignable(Boolean assignable) {
        isAssignable = assignable;
        return this;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public Role setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
        return this;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Role setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public Long getInheritRoleId() {
        return inheritRoleId;
    }

    public Role setInheritRoleId(Long inheritRoleId) {
        this.inheritRoleId = inheritRoleId;
        return this;
    }

    public Long getParentRoleId() {
        return parentRoleId;
    }

    public Role setParentRoleId(Long parentRoleId) {
        this.parentRoleId = parentRoleId;
        return this;
    }

    public String getParentRoleAssignLevel() {
        return parentRoleAssignLevel;
    }

    public Role setParentRoleAssignLevel(String parentRoleAssignLevel) {
        this.parentRoleAssignLevel = parentRoleAssignLevel;
        return this;
    }

    public Long getParentRoleAssignLevelValue() {
        return parentRoleAssignLevelValue;
    }

    public Role setParentRoleAssignLevelValue(Long parentRoleAssignLevelValue) {
        this.parentRoleAssignLevelValue = parentRoleAssignLevelValue;
        return this;
    }

    public String getLevelPath() {
        return levelPath;
    }

    public Role setLevelPath(String levelPath) {
        this.levelPath = levelPath;
        return this;
    }

    public String getInheritLevelPath() {
        return inheritLevelPath;
    }

    public Role setInheritLevelPath(String inheritLevelPath) {
        this.inheritLevelPath = inheritLevelPath;
        return this;
    }

    public Long getCreatedByTenantId() {
        return createdByTenantId;
    }

    public Role setCreatedByTenantId(Long createdByTenantId) {
        this.createdByTenantId = createdByTenantId;
        return this;
    }
}
