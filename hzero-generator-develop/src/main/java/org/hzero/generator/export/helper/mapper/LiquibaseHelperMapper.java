package org.hzero.generator.export.helper.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hzero.generator.export.helper.entity.Column;

/**
 * <p>
 * Liquibase Helper Mapper
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 14:51
 */
@Mapper
public interface LiquibaseHelperMapper {

    /**
     * 查询数据
     *
     * @param tableName  表明
     * @param columnList 列
     * @param where      查询条件
     * @return 全表数据
     */
//    List<Map<String, Object>> selectData(@Param("tableName") String tableName,
//                                         @Param("columnList") List<Column> columnList, @Param("where") String where);

    /**
     * 选择数据库
     * @param schemaName 库名
     */
    void selectSchema(@Param("schemaName") String schemaName);
    /**
     * 查询数据
     *
     * @param tableName  表明
     * @param columnList 列
     * @param where      查询条件
     * @return 全表数据
     */
    List<Map<String, Object>> selectData(@Param("tableName") String tableName,
                                         @Param("columnList") List<Column> columnList, @Param("where") String where);
}
