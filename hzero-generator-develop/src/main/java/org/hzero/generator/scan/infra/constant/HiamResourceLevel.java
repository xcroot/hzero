package org.hzero.generator.scan.infra.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * HIAM 层级值
 */
public enum HiamResourceLevel {

    /**
     * 平台层
     */
    SITE(0, "site", "role/site/custom/", "S"),

    /**
     * 租户层 此处的租户层相当于猪齿鱼的组织层，为了保持统一使用 organization
     */
    ORGANIZATION(1, "organization", "role/organization/custom/", "T"),

    /**
     * 组织层
     */
    ORG(2, "org", "role/org/custom/", "O"),

    /**
     * 项目层
     */
    PROJECT(3, "project", "role/project/custom/", "P"),

    /**
     * 用户层
     */
    USER(4, "user", "role/user/custom/", "U");

    private final int level;
    private final String value;
    private final String code;
    private final String simpleCode;

    private static Map<String, HiamResourceLevel> resourceLevelMap = new HashMap<>(8);
    static {
        for (HiamResourceLevel resourceLevel : HiamResourceLevel.values()) {
            resourceLevelMap.put(resourceLevel.value(), resourceLevel);
        }
    }

    /**
     *
     * @param level 角色层级值
     * @param value 角色层级
     * @param code 自定义角色编码前缀
     */
    HiamResourceLevel(int level, String value, String code, String simpleCode) {
        this.level = level;
        this.value = value;
        this.code = code;
        this.simpleCode = simpleCode;
    }

    public int level() {
        return level;
    }

    public String value() {
        return value;
    }

    public String code() {
        return code;
    }

    public String simpleCode() {
        return simpleCode;
    }

    public static HiamResourceLevel levelOfNullable(String level) {
        return resourceLevelMap.get(level);
    }

    public static HiamResourceLevel levelOf(String level) {
        HiamResourceLevel resourceLevel = resourceLevelMap.get(level);
        if (resourceLevel == null) {
            throw new RuntimeException("hiam.warn.resource.resourceLevelIllegal");
        }
        return resourceLevel;
    }

}
