package org.hzero.generator.export.helper;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.export.helper.entity.DataSet;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.hzero.generator.liquibase.excel.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @Description 导出新增数据
 * @Date 2020/2/5 15:57
 * @Author wanshun.zhang@hand-china.com
 */
@Component
public class AppendExport extends LiquibaseEngine {
    private static final Logger logger = LoggerFactory.getLogger(AppendExport.class);
    /**
     * 公式，id
     */
    private Map<String, String> idMap = new HashMap<>();
    /**
     * 表名，引用字段 <表名，引用的字段和表名>
     */
    private Map<String, Map<String, String>> refMap = new HashMap<>();
    /**
     * 公式，库数据
     */
    private Map<String, Map<String, Object>> dbData = new HashMap<>();
    /**
     * 过滤后的数据
     */
    private List<DataSet> result = new ArrayList<>();

    public static AppendExport createEngine(String filePath, LiquibaseEngineMode engineMode) {
        AppendExport updateExport = new AppendExport();
        updateExport.setFilePath(filePath);
        updateExport.setEngineMode(engineMode);
        updateExport.loadFile().loadExcel();
        return updateExport;
    }

    /**
     * 过滤数据
     *
     * @param dataSets 需要过滤的数据
     * @return 过滤后的数据
     */
    @Override
    public List<DataSet> dataFilter(List<DataSet> dataSets) {
        logger.info("********************开始过滤数据********************");
        dataSets.forEach(dataSet -> {
            for (TableData tableData : tableDataList) {
                if (StringUtils.equals(tableData.getName(), dataSet.getTableName())) {
                    // 引用字段存储起来
                    Map<String, String> refTable = new HashMap<>();
                    dataSet.getData().getColumnList().forEach(column -> {
                        if (column.isFormula()) {
                            // 字段名，字段属于的表名
                            refTable.put(column.getColumnName(), column.getReference().getTableName());
                        }
                    });
                    refMap.put(tableData.getName(), refTable);
                    DataSet dc = new DataSet();
                    dc.setSheetName(dataSet.getSheetName()).setTableName(dataSet.getTableName()).setData(dataSet.getData());
                    dc.setDataSet(new LinkedHashSet<>());
                    result.add(dc);
                    logger.info("对比数据{} size:{}", dataSet.getTableName(), dataSet.getDataSet().size());
                    for (Map<String, Object> dataMap : dataSet.getDataSet()) {
                        if (!compareData(tableData, dataMap)) {
                            // 不同，检查是否有引用，递归找出将数据添加到插入队列中
                            insertData(tableData, dataMap);
                        }
                    }
                }
            }
        });
        logger.info("********************数据过滤完成，开始写入新增数据********************");
        return result;
    }

    /**
     * 判断当前数据是否包含引用，遍历插入节点
     *
     * @param tableData 表数据
     * @param dataMap   库数据
     */
    private void insertData(TableData tableData, Map<String, Object> dataMap) {
        // 当前表有引用
        if (refMap.get(tableData.getName()) != null && refMap.get(tableData.getName()).size() > 0) {
            // 得到此表字段引用的<引用字段，引用的表>
            Map<String, String> refTable = refMap.get(tableData.getName());
            // 得到引用的字段集合
            Set<String> keySet = refTable.keySet();
            // 判断是否所有的关联都存在
            boolean flag = false;
            Map<String,Map<String, Object>> insertMap = new HashMap<>();
            // 遍历此表的所有引用字段，找到引用的数据添加到插入队列中
            for (String column : keySet) {
                String refId = "";
                if (dataMap.get(column) != null) {
                    refId = dataMap.get(column).toString();
                } else if (dataMap.get("#" + column) != null) {
                    // 关联id为唯一性索引
                    refId = dataMap.get("#" + column).toString();
                }
                String refTableName = refTable.get(column);
                Map<String, Object> refDataMap = dbData.get(refTableName + "-" + refId);
                if(!CollectionUtils.isEmpty(refDataMap)){
                    flag = true;
                    // 添加到插入队列
                    insertMap.put(refTableName,refDataMap);
                }else {
                    flag = false;
                }
            }
            if (flag) {
                final Set<String> refTableNameSet = insertMap.keySet();
                for (String refTableName : refTableNameSet) {
                    for (TableData tbData : tableDataList) {
                        if (StringUtils.equals(refTableName, tbData.getName())) {
                            // 递归插入
                            insertData(tbData, insertMap.get(refTableName));
                        }
                    }
                    // 父级插入到对应的set中
                    result.forEach(dataSet -> {
                        if (StringUtils.equals(refTableName, dataSet.getTableName())) {
                            dataSet.getDataSet().add(insertMap.get(refTableName));
                        }
                    });
                }
                // 当前数据插入到对应的set中
                result.forEach(dataSet -> {
                    if (StringUtils.equals(tableData.getName(), dataSet.getTableName())) {
                        dataSet.getDataSet().add(dataMap);
                    }
                });
            }

        } else {
            // 插入到对应的set中
            result.forEach(dataSet -> {
                if (StringUtils.equals(tableData.getName(), dataSet.getTableName())) {
                    dataSet.getDataSet().add(dataMap);
                }
            });
        }
    }

    /**
     * 比较数据，相同则记录真实id和原数据，不同则添加到插入队列
     *
     * @param tableData excel表数据
     * @param dataMap   查询出的数据
     * @return 是否相同
     */
    private boolean compareData(TableData tableData, Map<String, Object> dataMap) {
        List<TableData.Column> uniqueColumns = tableData.getUniqueColumns();
        boolean flag = false;
        String id = tableData.getColumns().get(0).getName();
        // 记录库数据
        if (dataMap.get("*" + id) != null) {
            dbData.put(tableData.getName() + "-" + dataMap.get("*" + id).toString(), dataMap);
        }
        for (TableData.TableRow tableRow : tableData.getTableRows()) {
            // 将excel数据转换成map
            Map<String, Object> excelMap = getTableData(tableRow);
            for (TableData.Column uniqueColumn : uniqueColumns) {
                String key = "#" + uniqueColumn.getName();
                if (dataMap.get(key) != null && excelMap.get(key) != null) {
                    flag = StringUtils.equals(excelMap.get(key).toString().trim(), dataMap.get(key).toString().trim());
                }
                if (!flag) {
                    // 唯一性索引不同,遍历下一个
                    break;
                }
            }
            if (flag) {
                // 数据相同，记录下来真实id
                if (dataMap.get("*" + id) != null) {
                    idMap.put(tableData.getSheet().getSheetName() + "!$E$" + tableRow.getLineNumber(), dataMap.get("*" + id).toString());
                }
                return true;
            }
        }
        logger.info("对比出不同数据：{} data:{}", tableData.getName(), dataMap);
        return false;
    }

    /**
     * 将excel数据封装成map数据
     *
     * @param tableRow 行数据
     * @return 结果
     */
    private Map<String, Object> getTableData(TableData.TableRow tableRow) {
        List<TableData.Column> columns = tableRow.getTable().getColumns();
        List<TableData.TableCellValue> tableCellValues = tableRow.getTableCellValues();
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            String key = columns.get(i).getName();
            String value = tableCellValues.get(i).getValue();
            // 唯一性索引是公式，换成id
            if (tableCellValues.get(i).isFormula()) {
                value = idMap.get(value);
            }
            if (tableRow.getTable().getUniqueColumns() != null && tableRow.getTable().getUniqueColumns().contains(columns.get(i))) {
                key = "#" + columns.get(i).getName();
            }
            if (columns.get(i).isGen()) {
                key = "*" + columns.get(i).getName();
            }
            if (columns.get(i).getLang() != null) {
                key = columns.get(i).getName() + ":" + columns.get(i).getLang();
            }
            data.put(key, value);
        }
        return data;
    }
}
