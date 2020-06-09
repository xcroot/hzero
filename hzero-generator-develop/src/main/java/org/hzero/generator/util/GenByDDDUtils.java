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
import org.hzero.generator.dto.TableEntity;

/**
 * 代码生成器 工具类 DDD模型
 * 
 * @name GenByDDDUtils
 * @author xianzhi.chen@hand-china.com 2018年1月31日下午5:22:25
 * @version
 */
public class GenByDDDUtils {

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/ddd/Controller.java.vm");
        templates.add("template/ddd/E.java.vm");
        templates.add("template/ddd/Mapper.java.vm");
        templates.add("template/ddd/Repository.java.vm");
        templates.add("template/ddd/RepositoryImpl.java.vm");
        templates.add("template/ddd/Service.java.vm");
        templates.add("template/ddd/ServiceImpl.java.vm");
        templates.add("template/ddd/Mapper.xml.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(GeneratorEntity info, Map<String, String> table, List<Map<String, String>> columns,
                    ZipOutputStream zip) {
        // 配置信息
        Configuration config = GeneratorUtils.getConfig();
        boolean hasBigDecimal = false;
        boolean hasDate = false;
        // 表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        // 表名转换成Java类名
        String className = GeneratorUtils.tableToJava(tableEntity.getTableName(),
                        StringUtils.isBlank(info.getTablePrefix()) ? config.getString("tablePrefix")
                                        : info.getTablePrefix());
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));

        // 列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setUpperColumnName(
                            StringUtils.upperCase(column.get("columnName").replace(info.getTablePrefix(), "")));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));
            columnEntity.setNullAble(column.get("nullAble"));

            // 列名转换成Java属性名
            String attrName = GeneratorUtils.columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setAttrname(StringUtils.uncapitalize(attrName));

            // 列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);
            if (!hasBigDecimal && attrType.equals("BigDecimal")) {
                hasBigDecimal = true;
            }
            if (!hasBigDecimal && attrType.equals("Date")) {
                hasDate = true;
            }
            // JDBC类型
            columnEntity.setJdbcType(config.getString(attrType));
            // 是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }

            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);

        // 没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        // 设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        // 封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("pkUpperFileName", StringUtils.upperCase(tableEntity.getPk().getColumnName()));
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("upperClassName", StringUtils.upperCase(tableEntity.getTableName()));
        map.put("lineClassName", GenStringUtils.camelToHorizontalline(tableEntity.getClassname()));
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("package", StringUtils.isBlank(info.getPkg()) ? config.getString("pkg") : info.getPkg());
        map.put("author", StringUtils.isBlank(info.getAuthor()) ? config.getString("author") : info.getAuthor());
        map.put("level", StringUtils.isBlank(info.getLevel()) ? "tenant" : info.getLevel());
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
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
                zip.putNextEntry(new ZipEntry(
                                getFileName(template, tableEntity.getClassName(), map.get("package").toString())));
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
    public static String getFileName(String template, String className, String packageName) {

        String javaPackagePath = "main" + File.separator + "java" + File.separator;

        if (StringUtils.isNotBlank(packageName)) {
            javaPackagePath += packageName.replace(".", File.separator) + File.separator;
        }

        String resourcePackagePath = "main" + File.separator + "resources" + File.separator;

        if (template.contains("E.java.vm")) {
            return javaPackagePath + "domain" + File.separator + "entity" + File.separator + className + ".java";
        }

        if (template.contains("Controller.java.vm")) {
            return javaPackagePath + "api" + File.separator + "controller" + File.separator + "v1" + File.separator
                            + className + "Controller.java";
        }

        if (template.contains("Mapper.java.vm")) {
            return javaPackagePath + "infra" + File.separator + "mapper" + File.separator + className + "Mapper.java";
        }

        if (template.contains("Repository.java.vm")) {
            return javaPackagePath + "domain" + File.separator + "repository" + File.separator + className
                            + "Repository.java";
        }

        if (template.contains("RepositoryImpl.java.vm")) {
            return javaPackagePath + "infra" + File.separator + "repository" + File.separator + "impl" + File.separator
                            + className + "RepositoryImpl.java";
        }

        if (template.contains("Service.java.vm")) {
            return javaPackagePath + "app" + File.separator + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            return javaPackagePath + "app" + File.separator + "service" + File.separator + "impl" + File.separator
                            + className + "ServiceImpl.java";
        }

        if (template.contains("Mapper.xml.vm")) {
            return resourcePackagePath + "mapper" + File.separator + className + "Mapper.xml";
        }

        return null;
    }
}
