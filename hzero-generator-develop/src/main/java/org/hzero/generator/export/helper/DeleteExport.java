package org.hzero.generator.export.helper;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.export.helper.entity.DataSet;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.hzero.generator.liquibase.excel.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description 导出删除数据
 * @Date 2020/2/5 15:57
 * @Author wanshun.zhang@hand-china.com
 */
@Component
public class DeleteExport extends LiquibaseEngine {
    private static final Logger logger = LoggerFactory.getLogger(DeleteExport.class);
    /**
     * 公式，id
     */
    private Map<String, String> idMap = new HashMap<>();
    /**
     * 过滤后的数据
     */
    private List<DataSet> result = new ArrayList<>();

    public static DeleteExport createEngine(String filePath, LiquibaseEngineMode engineMode) {
        DeleteExport updateExport = new DeleteExport();
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
                    DataSet dc = new DataSet();
                    dc.setSheetName(dataSet.getSheetName()).setTableName(dataSet.getTableName()).setData(dataSet.getData());
                    dc.setDataSet(new LinkedHashSet<>());
                    result.add(dc);
                    logger.info("对比数据{} size:{}", dataSet.getTableName(), dataSet.getDataSet().size());
                    // 存储相同的数据
                    List<TableData.TableRow> tableRows = new ArrayList<>();
                    for (int i = 0; i < tableData.getTableRows().size(); i++) {
                        if (compareData(tableData.getTableRows().get(i), dataSet.getDataSet())) {
                            tableRows.add(tableData.getTableRows().get(i));
                        }
                    }
                    // 剔除相同的数据
                    tableData.getTableRows().removeAll(tableRows);
                    // 插入数据
                    insertData(tableData);
                }
            }
        });
        logger.info("********************数据过滤完成，开始写入删除数据********************");
        return result;
    }

    /**
     * 比较数据，相同则记录真实id和原数据，不同则添加到插入队列
     *
     * @param tableRow excel表数据
     * @param dataSet  查询出的数据
     * @return 是否相同
     */
    private boolean compareData(TableData.TableRow tableRow, Set<Map<String, Object>> dataSet) {
        List<TableData.Column> uniqueColumns = tableRow.getTable().getUniqueColumns();
        boolean flag = false;
        String id = tableRow.getTableCellValues().get(0).getColumn().getName();
        for (Map<String, Object> dataMap : dataSet) {
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
                    idMap.put(tableRow.getTable().getSheet().getSheetName() + "!$E$" + tableRow.getLineNumber(), dataMap.get("*" + id).toString());
                }
                return true;
            }
        }
        logger.info("对比出不同数据：{} data:{}", tableRow.getTable().getName(), tableRow);
        return false;
    }


    /**
     * 遍历插入节点
     *
     * @param tableData 表数据
     */
    private void insertData(TableData tableData) {
        // 插入到对应的set中
        result.forEach(dataSet -> {
            if (StringUtils.equals(tableData.getName(), dataSet.getTableName())) {
                tableData.getTableRows().forEach(tableRow -> {
                    dataSet.getDataSet().add(getTableData(tableRow));
                });
            }
        });
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
