package org.hzero.generator.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.entity.*;
import org.hzero.generator.export.constants.Constants;
import org.hzero.generator.export.helper.AppendExport;
import org.hzero.generator.export.helper.DeleteExport;
import org.hzero.generator.export.helper.FullExport;
import org.hzero.generator.export.helper.VirtualMenuExport;
import org.hzero.generator.export.helper.entity.Column;
import org.hzero.generator.export.helper.entity.Data;
import org.hzero.generator.export.helper.entity.DataGroup;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.hzero.generator.liquibase.CusFileSystemResourceAccessor;
import org.hzero.generator.liquibase.excel.ExcelSeedDataReader;
import org.hzero.generator.liquibase.excel.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 描述:装配script脚本数据
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/12/02 16:01
 */
@Component
public class ScriptUtils {

    private static final String SUFFIX_XLSX = ".xlsx";

    /**
     * <p>
     * 主键非自增标记前缀
     * </p>
     *
     * @author Andy 2019/12/20 15:23
     */
    private final String TAG = "*";
    private final Logger LOGGER = LoggerFactory.getLogger(ScriptUtils.class);

    public void pullCreate(List<Service> serviceList) {
        serviceList.forEach(service -> {
            LOGGER.info("开始处理：{}", service.getDescription());
            service.getExcelList().forEach(excel -> {
                LOGGER.info("开始处理：[{}]，使用数据库：[{}]", excel.getDescription(), excel.getSchema());
                FullExport liquibaseEngine = FullExport.createEngine(Constants.BASE_OUTPUT_PATH + service.getName() + "/" + excel.getSchema() + "/" + excel.getName() + ".xlsx", LiquibaseEngineMode.OVERRIDE);
                List<DataGroup> dataGroupList = create(service.getName(), excel);
                liquibaseEngine.setDataGroupList(dataGroupList);
                liquibaseEngine.generate();
            });
        });
    }

    public void virtualMenuCreate(List<Service> serviceList) {
        serviceList.forEach(service -> {
            LOGGER.info("开始处理：{}", service.getDescription());
            service.getExcelList().forEach(excel -> {
                LOGGER.info("开始处理：[{}]，使用数据库：[{}]", excel.getDescription(), excel.getSchema());
                VirtualMenuExport liquibaseEngine = VirtualMenuExport.createEngine(Constants.BASE_OUTPUT_PATH + service.getName() + "/" + excel.getSchema() + "/" + excel.getName() + ".xlsx", LiquibaseEngineMode.OVERRIDE);
                List<DataGroup> dataGroupList = create(service.getName(), excel);
                liquibaseEngine.setDataGroupList(dataGroupList);
                liquibaseEngine.generate();
            });
        });
    }

    public void diffCreate(List<Service> serviceList, String dir) {
        ResourceAccessor accessor = new CusFileSystemResourceAccessor(dir);
        final List<String> fileList = getFile(accessor);
        serviceList.forEach(service -> {
            LOGGER.info("开始处理：{}", service.getDescription());
            service.getExcelList().forEach(excel -> {
                List<TableData> tableDataList = null;
                for (String file : fileList) {
                    if (file.endsWith(SUFFIX_XLSX) && StringUtils.contains(file, service.getName() + "/" + excel.getSchema() + "/" + excel.getName())) {
                        Set<InputStream> inputStream = null;
                        try {
                            inputStream = accessor.getResourcesAsStream(file);
                        } catch (IOException e) {
                            LOGGER.info("获取文件流失败 {}...", e.getMessage());
                        }
                        ExcelSeedDataReader excelSeedDataReader = new ExcelSeedDataReader(inputStream.iterator().next());
                        tableDataList = excelSeedDataReader.load();
                    }
                }
                LOGGER.info("开始处理：[{}]，使用数据库：[{}]", excel.getDescription(), excel.getSchema());
                List<DataGroup> dataGroupList = create(service.getName(), excel);
                if (tableDataList == null) {
                    LOGGER.error(">>>>>>>>>>>>>>>>>>>>>> excel 中不包含[{}]的某些数据", excel.getDescription());
                    return;
                }
                // 新增
                AppendExport appendExport = AppendExport.createEngine(Constants.BASE_OUTPUT_PATH + service.getName() + "/" + excel.getSchema() + "/" + excel.getName() + "-append.xlsx", LiquibaseEngineMode.OVERRIDE);
                appendExport.setTableDataList(tableDataList);
                appendExport.setDataGroupList(dataGroupList);
                appendExport.generate();
                // 删除
                /*DeleteExport deleteExport = DeleteExport.createEngine(Constants.BASE_OUTPUT_PATH + service.getName() + "/" + excel.getSchema() + "/" + excel.getName() + "-delete.xlsx", LiquibaseEngineMode.OVERRIDE);
                deleteExport.setTableDataList(tableDataList);
                deleteExport.setDataGroupList(dataGroupList);
                // 不清理找不到引用的数据
                deleteExport.setClearFlag(false);
                deleteExport.generate();*/
            });
        });
    }

    private List<String> getFile(ResourceAccessor accessor) {
        Set<String> fileNameSet = null;
        try {
            fileNameSet = accessor.list(null, File.separator, true, false, true);
        } catch (IOException e) {
            LOGGER.info("加载为文件失败 {}", e.getMessage());
        }
        List<String> nameList = new ArrayList<>(fileNameSet);
        Collections.sort(nameList);
        return nameList;
    }

    public List<DataGroup> create(String serviceName, Excel excel) {
        List<DataGroup> dataGroupList = new LinkedList<>();
        List<Sheet> sheetList = excel.getSheetList();
        sheetList.forEach(sheet -> {
            DataGroup dataGroup = new DataGroup();
            dataGroup.setSheetName(sheet.getDescription());
            List<Data> dataList = new LinkedList<>();
            sheet.getTableList().forEach(table -> {
                LOGGER.info("主表：[{}]，SQL：{}", table.getName(), table.getSql());
                String where = Optional.of(StringUtils.substringAfter(table.getSql().replaceAll("WHERE", "where"), "where")).orElse("1=1");
                if (StringUtils.isNotBlank(where) && !StringUtils.equals("1=1", where.replaceAll(" ", ""))) {
                    LOGGER.info("[where]条件：{}", where);
                }
                List<String> columns = Arrays
                        .asList(StringUtils.substringBetween(table.getSql().toLowerCase(), "select", " from ").split(",")).stream().map(String::trim).collect(Collectors.toList());
                String id = table.getId();
                String cited = table.getCited();
                List<Type> types = table.getTypes();
                List<Lang> langs = table.getLangs();
                List<Reference> references = table.getReferences();
                Data data = new Data();
                // 数据封装
                data.setSchemaName(excel.getSchema());
                data.setServiceName(serviceName);
                data.setTableName(table.getName());
                data.setAuthor(Constants.AUTHOR);
                data.setDescription(table.getDescription());
                data.setWhere(where);
                List<Column> columnList = new ArrayList<>();
                columns.forEach(columnItem -> {
                    Column column = new Column();
                    column.setColumnName(columnItem.toLowerCase());
                    if (StringUtils.equals(columnItem, id)) {
                        column.setAutoGenerate(true);
                    }
                    if (StringUtils.equals(TAG + columnItem, id)) {
                        column.setId(true);
                    }
                    if (StringUtils.equals(columnItem, cited)) {
                        column.setCited(true);
                    }
                    if (table.getUnique() != null) {
                        List<String> uniques = Arrays.asList(table.getUnique().toLowerCase().replaceAll(" ", "").split(","));
                        if (uniques.contains(columnItem.toLowerCase())) {
                            column.setUnique(true);
                        }
                    }
                    if (table.getDownload() != null) {
                        List<String> downloads = Arrays.asList(table.getDownload().toLowerCase().replaceAll(" ", "").split(","));
                        if (downloads.contains(columnItem.toLowerCase())) {
                            column.setDownload(true);
                            column.setBucket(table.getBucket());
                        }
                    }
                    types.forEach(type -> {
                        if (type != null && columnItem.equals(type.getField())) {
                            column.setType(type.getType());
                        }
                    });
                    langs.forEach(lang -> {
                        if (lang != null && columnItem.equals(lang.getField().toLowerCase())) {
                            column.setLang(Constants.LANG);
                            column.setPkName(lang.getPkName());
                        }
                    });
                    references.forEach(reference -> {
                        if (reference != null && columnItem.equals(reference.getField().toLowerCase())) {
                            // 没有获取sheetName属性
                            sheetList.forEach(s -> {
                                if (StringUtils.equals(s.getName(), reference.getSheetName())) {
                                    reference.setSheetName(s.getDescription());
                                }
                            });
                            column.setReference(reference.getReference());
                        }
                    });
                    columnList.add(column);
                });
                data.setColumnList(columnList);
                dataList.add(data);
            });
            dataGroup.setDataList(dataList);
            dataGroupList.add(dataGroup);
        });
        return dataGroupList;
    }
}
