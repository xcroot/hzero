package org.hzero.generator.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * description
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/11 20:01
 */
@Mapper
public interface MenuPermissionExportMapper {

    List<Map<String, Long>> selectIdByRoute(@Param("route") String route, @Param("version") String version);

    Long selectParendId(@Param("id") Long id);

    List<Long> selectChildId(@Param("id") Long id);

    void changeSchema(@Param("schema") String schema);
}
