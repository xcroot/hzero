package org.hzero.generator.liquibase.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.hzero.generator.liquibase.exception.LiquibaseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel 数据对象
 *
 * @author dongfan117@gmail.com
 */
public class TableData {

    private Sheet sheet;
    private int insert = 0;
    private int delete = 0;
    private Set<String> langs = new HashSet<>();
    private int startLine;
    private int startCol;

    /**
     * 在使用序列的数据库里，记录excel里插入数据中，
     * 手动设置id的最大值(即excel 中带*的id列值为整数，这种情况下使用该值插入，而不使用sequence自动生成的值；如果是字符串，则使用sequence生成的值插入)
     */
    private long maxId;
    private String name;
    private List<Column> columns = new ArrayList<>();
    private List<Column> uniqueColumns = new ArrayList<>();
    private List<TableRow> tableRows = new ArrayList<>();

    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

    public List<TableRow> getTableRows() {
        return tableRows;
    }

    public void setTableRows(List<TableRow> tableRows) {
        this.tableRows = tableRows;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getUniqueColumns() {
        return uniqueColumns;
    }

    public void setUniqueColumns(List<Column> uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public int getInsert() {
        return insert;
    }

    public void setInsert(int insert) {
        this.insert = insert;
    }

    public int getDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }

    public Set<String> getLangs() {
        return langs;
    }

    public void setLangs(Set<String> langs) {
        this.langs = langs;
    }

    /**
     * 完全处理
     *
     * @return 是否处理完毕
     */
    public boolean complete() {
        for (TableRow tableRow : tableRows) {
            if (!tableRow.processFlag) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取处理概述
     *
     * @return 处理概述
     */
    public String processSummary() {
        int complete = 0;
        for (TableRow tableRow : tableRows) {
            if (tableRow.processFlag) {
                complete++;
            }
        }
        String str = String.format("total:%-4d  process:%-4d  insert:%-4d  delete:%-4d",
                tableRows.size(), complete, insert, delete);
        if (complete == tableRows.size()) {
            str += "  complete";
        }
        return str;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * 添加列
     *
     * @param column 列
     * @return 添加的列
     */
    public Column addCol(Column column) {
        columns.add(column);
        if (column.unique) {
            uniqueColumns.add(column);
        }
        if (column.lang != null) {
            langs.add(column.lang);
        }
        return column;
    }

    /**
     * 检查合法
     */
    public void validate() {
        int genCount = 0;
        int uniqueCount = 0;
        for (Column column : columns) {
            if (column.gen) {
                genCount++;
            } else if (column.unique) {
                uniqueCount++;
            }
        }
        if (genCount > 1) {
            throw new LiquibaseException("table has more than one generated column :"
                    + getName());
        }
        if (uniqueCount == 0) {
            throw new LiquibaseException("table has no unique check column :"
                    + getName());
        }
    }

    public String getSummaryInfo() {
        return String.format("%s[sheet:%s;startLine:%d]", name, sheet.getSheetName(), startLine);
    }

    public static class Column {
        private boolean gen = false;
        private String type = "VARCHAR";
        private String name;
        private boolean unique = false;

        private String lang = null;

        public Column(String name) {
            setName(name);
        }

        public boolean isGen() {
            return gen;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        private void setName(String originName) {
            this.name = originName;
            if (originName.startsWith("*")) {
                name = originName.substring(1);
                gen = true;
            } else if (originName.startsWith("#")) {
                unique = true;
                name = originName.substring(1);
            }

            int sem = name.indexOf(':');
            if (sem > 0) {
                String localLang = name.substring(sem + 1);
                name = name.substring(0, sem);
                if (StringUtils.isEmpty(localLang)) {
                    throw new LiquibaseException("invalid tl language :" + name);
                }
                this.lang = localLang;
            }
            if (name.endsWith("_DATE")){
                type = "DATE";
            }
            int lb = name.indexOf('(');
            if (lb > 0) {
                type = name.substring(lb + 1, name.indexOf(')')).trim().toUpperCase();
                name = name.substring(0, lb).trim();
            }
        }

        public boolean isUnique() {
            return unique;
        }

        public String getLang() {
            return lang;
        }

        @Override
        public String toString() {
            return (gen ? "*" : "") + name + ":" + type;
        }
    }

    public static class TableRow {
        private TableData table;
        private int lineNumber;
        private List<TableCellValue> tableCellValues = new ArrayList<>();

        private boolean processFlag = false;

        private boolean existsFlag = false;
        private boolean insertFlag = false;
        private boolean updateFlag = false;
        private boolean deleteFlag = false;
        /**
         * 自增列(带*的列，只能有一个)是否可以使用excel里的value做值插入
         * 只有值为整数的情况下才能插入，其他情况自增长
         */
        private boolean generatedColumnInserted = false;


        /**
         * 是否是当前TD
         *
         * @return boolean
         */
        public boolean present() {
            for (TableCellValue tableCellValue : tableCellValues) {
                if (!tableCellValue.valuePresent) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return Arrays.toString(tableCellValues.toArray());
        }

        /**
         * 检查：是否有需要生成、但未生成的值（*主键）
         *
         * @return 否有需要生成、但未生成的值（*主键）
         */
        public boolean needGen() {
            for (TableCellValue tableCellValue : tableCellValues) {
                if (tableCellValue.column.gen && !tableCellValue.valuePresent) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 检查：是否除了需要自动生成的值，其他值均已具备。
         *
         * @return 是否除了需要自动生成的值，其他值均已具备
         */
        public boolean canInsert() {
            for (TableCellValue tableCellValue : tableCellValues) {
                if (tableCellValue.column.gen || tableCellValue.valuePresent) {
                    continue;
                }
                return false;
            }
            return true;
        }

        /**
         * 检查第一个单元格是否加上删除线，表示对该行需要执行删除查找
         *
         * @return true，删除
         */
        public boolean canDelete() {
            XSSFCellStyle cellStyle = (XSSFCellStyle) this.getTableCellValues().get(0).getCell().getCellStyle();
            return cellStyle.getFont().getStrikeout();
        }


        public TableData getTable() {
            return table;
        }

        public void setTable(TableData table) {
            this.table = table;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public List<TableCellValue> getTableCellValues() {
            return tableCellValues;
        }

        public void setTableCellValues(List<TableCellValue> tableCellValues) {
            this.tableCellValues = tableCellValues;
        }

        public boolean isProcessFlag() {
            return processFlag;
        }

        public void setProcessFlag(boolean processFlag) {
            this.processFlag = processFlag;
        }

        public boolean isExistsFlag() {
            return existsFlag;
        }

        public void setExistsFlag(boolean existsFlag) {
            this.existsFlag = existsFlag;
        }

        public boolean isInsertFlag() {
            return insertFlag;
        }

        public void setInsertFlag(boolean insertFlag) {
            this.insertFlag = insertFlag;
        }

        public boolean isUpdateFlag() {
            return updateFlag;
        }

        public void setUpdateFlag(boolean updateFlag) {
            this.updateFlag = updateFlag;
        }

        public boolean isDeleteFlag() {
            return deleteFlag;
        }

        public void setDeleteFlag(boolean deleteFlag) {
            this.deleteFlag = deleteFlag;
        }

        public boolean isGeneratedColumnInserted() {
            return generatedColumnInserted;
        }

        public void setGeneratedColumnInserted(boolean generatedColumnInserted) {
            this.generatedColumnInserted = generatedColumnInserted;
        }
    }

    public static class TableCellValue {
        private static Pattern pattern = Pattern.compile("[A-Z]+[0-9]+");
        private String value;
        private String formula;
        private boolean valuePresent = true;
        private boolean isFormula = false;
        private Cell cell;
        private TableRow tableRow;
        private Column column;

        public TableCellValue() {

        }

        /**
         * 创建TableCellValue
         *
         * @param cell     单元格
         * @param tableRow 表行
         * @param column   列
         */
        public TableCellValue(Cell cell, TableRow tableRow, Column column) {
            this.tableRow = tableRow;
            this.column = column;
            this.cell = cell;
            if (cell != null && cell.getCellType() == CellType.FORMULA) {
                isFormula = true;
                formula = cell.getCellFormula().replace("$", "");
                valuePresent = false;
            }
            if (column.gen) {
                valuePresent = false;
            }
        }

        public static Pattern getPattern() {
            return pattern;
        }

        /**
         * 获取相关Cell
         *
         * @return 相关Cell
         */
        public List<String> getRelatedCell() {
            if (!isFormula) {
                return Collections.emptyList();
            }
            if (formula.contains("!")) {
                return Collections.singletonList(formula);
            }
            List<String> list = new ArrayList<>();
            Matcher matcher = pattern.matcher(formula);
            while (matcher.find()) {
                list.add(matcher.group());
            }
            return list;
        }

        /**
         * 更新值
         *
         * @param value 要更新的值
         */
        public void updateValue(String value) {
            cell.setCellValue(value);
            this.value = value;
            valuePresent = true;
        }

        /**
         * toString
         *
         * @return String of Cell Value
         */
        @Override
        public String toString() {
            if (cell == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            if (isFormula) {
                sb.append('[').append("F:").append(formula).append(']');
            }
            sb.append(value);
            return sb.toString();
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getFormula() {
            return formula;
        }

        public boolean isValuePresent() {
            return valuePresent;
        }

        public boolean isFormula() {
            return isFormula;
        }

        public Cell getCell() {
            return cell;
        }

        public TableRow getTableRow() {
            return tableRow;
        }

        public Column getColumn() {
            return column;
        }
    }

}
