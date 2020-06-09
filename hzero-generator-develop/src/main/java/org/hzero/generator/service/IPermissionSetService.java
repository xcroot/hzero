package org.hzero.generator.service;

import org.hzero.generator.scan.domain.vo.ServiceRouteVO;
import org.hzero.generator.scan.infra.constant.PermissionType;

import java.util.List;

/**
 * 权限集服务
 *
 * @author fanghan.liu 2020/02/27 10:49
 */
public interface IPermissionSetService {

    void refreshPermissionSet(List<ServiceRouteVO> routes);

    /**
     * 刷新特定菜单下的UI权限组件集
     *
     * 用于菜单更换路由时使用, 依据菜单路由, 查找已扫描的路由对应的UI权限信息, 并进行更新
     *
     * 注意:
     * 仅仅会新增此路由尚未添加的权限集, 不会更新和删除;
     * 1. 因为只涉及名称的更新(且此名称可能涉及前端用户主动修改), 所以未执行更新操作 --> 仅更新permissionType
     * 2. 权限集作为基础数据不涉及删除操作
     *
     * @param menuId 待刷新菜单
     */
    void refreshUiPermissionOfMenu(Long menuId);

    void assignPsPermissions(Long permissionSetId, PermissionType permissionType, String[] permissionCodes);


}
