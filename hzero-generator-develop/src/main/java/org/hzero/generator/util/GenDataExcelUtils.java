package org.hzero.generator.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.hzero.generator.dto.ColumnEntity;
import org.hzero.generator.dto.GeneratorEntity;
import org.hzero.generator.dto.TableEntity;

/**
 * 初始化数据生成工具类
 * 
 * @name GenDataExcelUtils
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:22:25
 * @version
 */
public class GenDataExcelUtils {

    /**
     * 生成代码
     * 
     * @param indexs
     */
    public static void generatorCode(GeneratorEntity info, Map<String, String> table, List<Map<String, String>> columns, ZipOutputStream zip) {
        // 配置信息
        Configuration config = GeneratorUtils.getConfig();
        // 表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        // 列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));
            columnEntity.setNullAble(column.get("nullAble"));
            columnEntity.setColumnType(column.get("columnType"));
            columnEntity.setColumnDefault(column.get("columnDefault"));
            // 是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }
            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        // 封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("tableComment", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("comments", tableEntity.getComments());
        map.put("columns", tableEntity.getColumns());
        map.put("indexs", tableEntity.getIndexs());
        map.put("author", StringUtils.isBlank(info.getAuthor()) ? config.getString("author") : info.getAuthor());
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_PATTERN));

        // 初始化模板
        String template = "template/data/init-data.xlsx";

    }

    /**
     * 获取文件名
     */
    public static String getFileName(String dateStr, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("main");
        sb.append(File.separator);
        sb.append("resources");
        sb.append(File.separator);
        sb.append("script");
        sb.append(File.separator);
        sb.append("db");
        sb.append(File.separator);
        sb.append(dateStr);
        sb.append(tableName);
        sb.append(".xlsx");
        return sb.toString();

    }
}
