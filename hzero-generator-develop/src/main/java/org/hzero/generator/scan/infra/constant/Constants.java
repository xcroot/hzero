package org.hzero.generator.scan.infra.constant;

/**
 * @author jianbo.li
 * @date 2019/12/25 11:01
 */
public interface Constants {

    interface VariableScope {
        /**
         * 全局
         */
        String A = "A";
        /**
         * 作用于函数
         */
        String P = "P";
    }

    interface TenantIdPathFlag {
        /**
         * 租户id
         */
        String TENANT_ID = "${tenantId}";

        /**
         * 租户id
         */
        String ORGANIZATION_ID = "${organizationId}";

        /**
         * 函数调用
         */
        String GET_CURRENT_ORGANIZATION_ID = "getCurrentOrganizationId()";

        String STARDARD_PATH_FLAG = "{organizationId}";

        String PARAMS_ORGANIZATION_ID = "${params.organizationId}";

        String PARAMS_TENANT_ID = "${params.tenantId}";

        String GET_CURRENT_ORGANIZATION_ID_SPECIAL = "${getCurrentOrganizationId()}";
    }

    interface PathLevel {
        /**
         * 租户层
         */
        String organization = "organization";
        /**
         * 平台层
         */
        String site = "site";

        /**
         * 租户层与平台层
         */
        String both = "BOTH";
    }

    /**
     * url 路径分割符
     */
    String PATH_DIVIDER = "/";

    String COMMA = ",";

    String DOUBLE_VERTICAL_LINE = "||";

    String UI_ROUTE = "uiRoute";

    String SERVICE_NAME = "serviceName";

    String TRANSPORT = "transport";

    interface CompType {
        String LOV = "lov";
        String BUTTON = "button";
        String PROMPT = "prompt";
    }

    /**
     * 路由信息缓存前坠
     */
    String ADMIN_ROUTE_INFO_CACHE_PREFIX = "hadm:routes";

    interface YesNoFlag {
        String YES = "Y";
        String NO = "N";
        String DELETE = "X";
    }

    /**
     * 租户管理员模板
     *
     * @author mingwei.liu@hand-china.com
     * @since 2018/12/12
     */
    String ORGANIZATION_TENANT_ROLE_TPL_CODE = RoleCode.TENANT_TEMPLATE;

    interface RoleCode {
        String SITE = "role/site/default/administrator";
        String TENANT = "role/organization/default/administrator";
        String TENANT_TEMPLATE = "role/organization/default/template/administrator";
    }

    public interface Symbol {
        String AND = "&";
        String COMMA = ",";
        String POINT = ".";
        String SLASH = "/";
    }

    public interface Flag {
        Integer YES = 1;
        Integer NO = 0;
    }

    String PATH_SEPARATOR = "|";
}
