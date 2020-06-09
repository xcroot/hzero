package org.hzero.generator.scan.infra.constant;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色权限类型
 *
 * @author bojiangzhou 2019/06/12
 */
public enum RolePermissionType {

    /**
     * 权限集
     */
    PS,
    /**
     * 服务
     */
    HITF_SVR,
    /**
     * 接口
     */
    HITF_ITF,
    /**
     * 安全组
     */
    SG
    ;

    private static Map<String, RolePermissionType> map = new HashMap<>(3);

    static {
        for (RolePermissionType rpt : RolePermissionType.values()) {
            map.put(rpt.name(), rpt);
        }
    }

    public static RolePermissionType value(String type) {
        RolePermissionType rolePermissionType = map.get(StringUtils.upperCase(type));
        if (rolePermissionType == null) {
            throw new RuntimeException(String.format("role permission type [%s] is not match.", type));
        }
        return rolePermissionType;
    }

}
