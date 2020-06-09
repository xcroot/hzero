package org.hzero.generator.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hzero.generator.dto.ColumnEntity;
import org.hzero.generator.dto.GeneratorEntity;
import org.hzero.generator.dto.IndexEntity;
import org.hzero.generator.dto.TableEntity;

/**
 * 代码生成器 工具类
 * 
 * @name GenUtils
 * @description
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:22:25
 * @version
 */
public class GenDBScriptUtils {

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/db/groovy.vm");
        return templates;
    }

    /**
     * 生成代码
     * 
     * @param indexs
     */
    public static void generatorCode(GeneratorEntity info, Map<String, String> table, List<Map<String, String>> columns,
                    List<Map<String, String>> indexs, ZipOutputStream zip) {
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
            columnEntity.setColumnSize(String.valueOf(column.get("columnSize")));
            columnEntity.setColumnDefault(column.get("columnDefault"));
            // 是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }
            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        // 索引信息
        List<IndexEntity> indexList = new ArrayList<>();
        String indexName = "$";
        StringBuilder columnSb = new StringBuilder();
        IndexEntity indexEntity = null;
        List<String> indexColumns = null;
        int pkCnt = 0;
        for (Map<String, String> index : indexs) {
            if (!indexName.equals(index.get("indexName"))) {
                if (StringUtils.isNotBlank(columnSb.toString())) {
                    indexEntity.setIndexColumn(columnSb.toString().substring(0, columnSb.toString().length() - 1));
                    indexEntity.setIndexColumns(indexColumns);
                    indexList.add(indexEntity);
                }
                columnSb.delete(0, columnSb.length());
                indexColumns = new ArrayList<>();
                indexEntity = new IndexEntity();
                indexName = index.get("indexName");
                indexEntity.setTableName(tableEntity.getTableName());
                indexEntity.setIndexName(indexName);
                String indexType = index.get("indexType").trim();
                switch (indexType) {
                    case "0": // 普通索引
                        indexEntity.setIndexType("N");
                        break;
                    case "2": // 唯一性索引
                        indexEntity.setIndexType("U");
                        break;
                    case "3": // 主键索引
                        indexEntity.setIndexType("P");
                        break;
                }
                columnSb.append(index.get("indexFiled"));
                columnSb.append(",");
                indexColumns.add(index.get("indexFiled"));
            } else {
                columnSb.append(index.get("indexFiled"));
                columnSb.append(",");
                indexColumns.add(index.get("indexFiled"));
            }
            // 判断组件字段数量
            if ("3".equals(index.get("indexType").trim())) {
                pkCnt++;
            }
        }
        if (StringUtils.isNotBlank(columnSb.toString())) {
            indexEntity.setIndexColumn(columnSb.toString().substring(0, columnSb.toString().length() - 1));
            indexEntity.setIndexColumns(indexColumns);
            indexList.add(indexEntity);
        }
        tableEntity.setIndexs(indexList);

        // 设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        // 封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("tableComment", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("pkCnt", pkCnt);
        map.put("className", tableEntity.getClassName());
        map.put("comments", tableEntity.getComments());
        map.put("columns", tableEntity.getColumns());
        map.put("indexs", tableEntity.getIndexs());
        map.put("author", StringUtils.isBlank(info.getAuthor()) ? config.getString("author") : info.getAuthor());
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_PATTERN));
        VelocityContext context = new VelocityContext(map);

        // 获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, GeneratorUtils.DEFAULT_CHARACTER_SET);
            tpl.merge(context, sw);
            try {
                // 添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getTableName())));
                IOUtils.write(sw.toString(), zip, GeneratorUtils.DEFAULT_CHARACTER_SET);
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new GenException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String tableName) {

        String dbScriptPath = "main" + File.separator + "resources" + File.separator;

        if (template.contains("groovy.vm")) {
            return dbScriptPath + "script" + File.separator + "db" + File.separator + tableName + ".groovy";
        }

        return null;
    }
}
