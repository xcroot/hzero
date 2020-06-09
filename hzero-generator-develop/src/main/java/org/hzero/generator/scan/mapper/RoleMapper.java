package org.hzero.generator.scan.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hzero.generator.scan.domain.Role;
import org.hzero.generator.scan.domain.RolePermission;

import java.util.List;

/**
 * description
 *
 * @author fanghan.liu 2020/02/27 16:39
 */
@Mapper
public interface RoleMapper {

    List<Role> selectByCode(@Param("code") String code);

    void insertRolePermission(@Param("rolePermission") RolePermission rolePermission);

    List<Role> selectbyTenantIdAndInheritRoleId(@Param("queryParam") Role queryParam);
}
