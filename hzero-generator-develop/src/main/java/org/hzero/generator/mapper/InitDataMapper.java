package org.hzero.generator.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * description
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/11/11 20:01
 */
@Mapper
public interface InitDataMapper {
    /**
     *
     * 查询数据库
     *
     * @return
     */
    List<String> selectDatabase();
    /**
     * 创建数据库
     * @param schema 数据库名称
     */
    void createDatabase(@Param("schema") String schema);
    /**
     * 创建数据库
     * @param schema 数据库名称
     */
    void createDatabaseMysql(@Param("schema") String schema);
}
