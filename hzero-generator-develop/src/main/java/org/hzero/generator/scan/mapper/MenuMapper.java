package org.hzero.generator.scan.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hzero.generator.scan.domain.Menu;
import org.hzero.generator.scan.domain.MenuPermission;
import org.hzero.generator.scan.domain.Permission;
import org.hzero.generator.scan.domain.dto.PermissionSetSearchDTO;
import org.hzero.generator.scan.domain.vo.Lov;

import java.util.List;

/**
 * description
 *
 * @author fanghan.liu 2020/02/27 10:53
 */
@Mapper
public interface MenuMapper {

    /**
     * 主键查询菜单
     * @param id
     * @return
     */
    Menu getMenuById(@Param("id") Long id);

    List<Permission> selectOne(@Param("permission") Permission permission);

    /**
     * 查询菜单权限集
     *
     */
    List<Menu> selectMenuPermissionSet(@Param("searchDTO") PermissionSetSearchDTO searchDTO);

    /**
     * 查询权限集下的权限
     *
     */
    List<Permission> selectPermissionSetPermissions(@Param("permissionSetId") Long permissionSetId,@Param("tenantId") Long tenantId);

    void updatePermissionType(@Param("menu") Menu menu);

    void insertSelective(@Param("menu") Menu menu);

    List<Lov> selectLovByCodes(@Param("codes") List<String> codes, @Param("tenantId") Long tenantId);

    List<Permission> selectPermissionByCodes(@Param("permissionCodes") List<String> permissionCodes);

    int selectMenuPermissionCount(@Param("menuPermission") MenuPermission menuPermission);

    void insertMenuPermission(@Param("mp") MenuPermission mp);

    /**
     * 由服务简码获取服务名
     * @param name
     * @return
     */
    List<String> getServiceName(@Param("name") String name);

    void insertMenuTl(@Param("id") Long id,@Param("name") String name,@Param("lang") List<String> lang);
}
