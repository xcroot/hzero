package org.hzero.generator.scan.domain;

import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.scan.infra.constant.Constants;
import org.hzero.generator.scan.infra.constant.HiamMenuType;
import org.hzero.generator.scan.infra.constant.HiamResourceLevel;
import org.hzero.generator.util.ChineseUtils;
import org.springframework.util.Assert;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;


public class Menu {

    public static final String FIELD_ID = "id";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_QUICK_INDEX = "quickIndex";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_PARENT_ID = "parentId";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_SORT = "sort";
    public static final String FIELD_IS_DEFAULT = "isDefault";
    public static final String FIELD_ICON = "icon";
    public static final String FIELD_ROUTE = "route";
    public static final String FIELD_CUSTOM_FLAG = "customFlag";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_ENABLED_FLAG = "enabledFlag";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_LEVEL_PATH = "levelPath";
    public static final String FIELD_VIRTUAL_FLAG = "virtualFlag";
    public static final String FIELD_CONTROLLER_TYPE = "controllerType";
    public static final String FIELD_PERMISSION_TYPE = "permissionType";

    public static final String ROOT_LEVEL_PATH = "0";
    public static final Long ROOT_ID = 0L;
    public static final String PS_DEFAULT = ".ps.default";
    public static final String PS_DEFAULT_NAME = "hiam.info.menu.ps.defaultName";
    public static final ControllerType DEFAULT_CONTROLLER_TYPE = ControllerType.DISABLED;
    public static final String MENU_EXPORT_FILE_PREFIX = "menu_";
    public static final String MENU_EXPORT_FILE_TYPE = ".json";
    public static final String MENU_CODE_SPLIT = ".";

    private Long id;

    private String code;

    private String name;

    private Long parentId;

    private String type;

    private String route;

    private String level;

    private Integer sort;

    private Integer isDefault;
    private String icon;
    private Integer customFlag;
    private Long tenantId;
    private Integer enabledFlag;
    private String description;
    private String levelPath;
    private Integer virtualFlag;
    private String controllerType;
    private List<Menu> childrenMenu;
    @Pattern(regexp = "^[a-zA-Z0-9]{0,30}$", message = "error.menu.quickIndexIllegal", groups = Valid.class)
    private String quickIndex;

    private String permissionType;

    /**
     * 按钮控制类型
     */
    public enum ControllerType {
        HIDDEN("hidden"),
        DISABLED("disabled");

        private String type;

        ControllerType(String type) {
            this.type = type;
        }

        public String type() {
            return type;
        }
    }

    /**
     * 菜单创建初始化
     */
    public void initMenu() {
        Assert.notNull(tenantId, "menu's tenantId must not be null.");
        this.isDefault = Constants.Flag.YES;
        if (HiamMenuType.ROOT.value().equals(this.type)) {
            this.parentId = ROOT_ID;
        }
        // code 去除重复的点号
        this.code = StringUtils.replaceAll(this.code, "\\.{2,}", ".");
    }

    public void initQuickIndex() {
        // 初始化首字母
        if (StringUtils.isBlank(this.quickIndex)) {
            this.quickIndex = ChineseUtils.extractCapitalInitial(this.name);
        }
    }


    public void validate(Menu parentMenu) {
        // 验证菜单数据的有效性
        HiamMenuType.throwExceptionNotMatch(this.type);
        HiamResourceLevel.levelOf(this.level);
        if (!Menu.ROOT_ID.equals(this.parentId)) {
            if (!parentMenu.getLevel().equals(this.level)) {
                throw new RuntimeException("hiam.error.menu.levelNotEqualsParentLevel");
            }
        }
        if (StringUtils.equalsAny(this.type, HiamMenuType.MENU.value(), HiamMenuType.LINK.value(), HiamMenuType.INNER_LINK.value(), HiamMenuType.WINDOW.value())) {
            if (StringUtils.isBlank(this.route)) {
                throw new RuntimeException("hiam.error.menu.routeNotNull");
            }
        }
    }

    /**
     * 设置 levelPath，使用 code 作路径
     *
     * @param parentMenu not null
     */
    public void initLevelPath(Menu parentMenu) {
        if (Objects.equals(this.parentId, Menu.ROOT_ID)) {
            this.levelPath = this.code;
        } else {
            this.levelPath = parentMenu.getLevelPath() + Constants.PATH_SEPARATOR + this.code;
        }
    }

    /**
     * 调整父级菜单时重置编码
     */
    public void resetCode(String originParentCode, String nowParentCode) {
        if (this.code.startsWith(originParentCode)) {
            this.code = StringUtils.replaceFirst(this.code, originParentCode, nowParentCode);
        }
    }


    public Long getId() {
        return id;
    }

    public Menu setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Menu setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public Menu setName(String name) {
        this.name = name;
        return this;
    }


    public Long getParentId() {
        return parentId;
    }

    public Menu setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getType() {
        return type;
    }

    public Menu setType(String type) {
        this.type = type;
        return this;
    }


    public String getRoute() {
        return route;
    }

    public Menu setRoute(String route) {
        this.route = route;
        return this;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public Menu setPermissionType(String permissionType) {
        this.permissionType = permissionType;
        return this;
    }

    public List<Menu> getChildrenMenu() {
        return childrenMenu;
    }

    public Menu setChildrenMenu(List<Menu> childrenMenu) {
        this.childrenMenu = childrenMenu;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public Menu setLevel(String level) {
        this.level = level;
        return this;
    }

    public Integer getSort() {
        return sort;
    }

    public Menu setSort(Integer sort) {
        this.sort = sort;
        return this;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public Menu setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public Menu setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public Integer getCustomFlag() {
        return customFlag;
    }

    public Menu setCustomFlag(Integer customFlag) {
        this.customFlag = customFlag;
        return this;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Menu setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public Integer getEnabledFlag() {
        return enabledFlag;
    }

    public Menu setEnabledFlag(Integer enabledFlag) {
        this.enabledFlag = enabledFlag;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Menu setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLevelPath() {
        return levelPath;
    }

    public Menu setLevelPath(String levelPath) {
        this.levelPath = levelPath;
        return this;
    }

    public Integer getVirtualFlag() {
        return virtualFlag;
    }

    public Menu setVirtualFlag(Integer virtualFlag) {
        this.virtualFlag = virtualFlag;
        return this;
    }

    public String getControllerType() {
        return controllerType;
    }

    public Menu setControllerType(String controllerType) {
        this.controllerType = controllerType;
        return this;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", type='" + type + '\'' +
                ", route='" + route + '\'' +
                ", childrenMenu=" + childrenMenu +
                ", permissionType='" + permissionType + '\'' +
                '}';
    }
}
