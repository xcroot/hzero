package org.hzero.generator.export.helper.supporter;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Cell
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 13:16
 */
public class CellData {
    private String sheetName;
    private String columnName;
    private String relTableName;
    private int column;
    private int row;
    private Object value;
    private boolean formula = false;
    private CellStyle cellStyle;
    private boolean cited;
    private boolean autoGenerate = false;
    private boolean id = false;

    private CellData() {
    }

    public static CellData copy(CellData cellData) {
        CellData newCellData = new CellData();
        BeanUtils.copyProperties(cellData, newCellData);
        return newCellData;
    }

    public String getColumnName() {
        return columnName;
    }

    public CellData setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getRelTableName() {
        return relTableName;
    }

    public CellData setRelTableName(String relTableName) {
        this.relTableName = relTableName;
        return this;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public CellData setAutoGenerate(boolean autoGenerate) {
        this.autoGenerate = autoGenerate;
        return this;
    }

    public boolean isId() {
        return id;
    }

    public CellData setId(boolean id) {
        this.id = id;
        return this;
    }

    public CellData(int column, int row, Object value) {
        this.column = column;
        this.row = row;
        this.value = value;
    }

    public CellData(int column, int row, Object value, boolean formula) {
        this.column = column;
        this.row = row;
        this.value = value;
        this.formula = formula;
    }

    public CellData(int column, int row, Object value, boolean formula, CellStyle cellStyle) {
        this.column = column;
        this.row = row;
        this.value = value;
        this.formula = formula;
        this.cellStyle = cellStyle;
    }

    public CellData(String columnText, int row, Object value) {
        this.setColumnText(columnText);
        this.row = row;
        this.value = value;
    }

    public CellData(String columnText, int row, Object value, boolean formula) {
        this.setColumnText(columnText);
        this.row = row;
        this.value = value;
        this.formula = formula;
    }

    public CellData(String columnText, int row, Object value, boolean formula, CellStyle cellStyle) {
        this.setColumnText(columnText);
        this.row = row;
        this.value = value;
        this.formula = formula;
        this.cellStyle = cellStyle;
    }

    public String getSheetName() {
        return sheetName;
    }

    public CellData setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public int getRow() {
        return row;
    }

    public CellData setRow(int row) {
        this.row = row;
        return this;
    }

    public int getColumn() {
        return column;
    }

    public CellData setColumn(int column) {
        this.column = column;
        return this;
    }

    public String getColumnText() {
        char[] chars = Integer.toString(column - 1, 26).toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            chars[i] += 17;
        }
        return new String(chars);
    }

    public CellData setColumnText(String columnText) {
        Assert.isTrue(StringUtils.hasText(columnText), "列序号不能为空！");
        char[] chars = columnText.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            Assert.isTrue(chars[i] >= 65 && chars[i] <= 90, "不能识别的列序号");
            chars[i] -= 17;
        }
        column = Integer.parseInt(new String(chars), 26) + 1;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public CellData setValue(Object value) {
        this.value = value;
        return this;
    }

    public boolean isFormula() {
        return formula;
    }

    public CellData setFormula(boolean formula) {
        this.formula = formula;
        return this;
    }

    public CellStyle getCellStyle() {
        return cellStyle;
    }

    public CellData setCellStyle(CellStyle cellStyle) {
        this.cellStyle = cellStyle;
        return this;
    }

    public boolean isCited() {
        return cited;
    }

    public CellData setCited(boolean cited) {
        this.cited = cited;
        return this;
    }

    public String getFormulaText() {
        StringBuilder sb = new StringBuilder("=");
        if (StringUtils.hasText(sheetName)){
            sb.append("{sheetName}!");
        }
        return sb.append("${column}${row}").toString();
    }


    public enum CellStyle {
        /**
         * 粗体
         */
        BOLD,
        /**
         * 橙色
         */
        ORANGE,
        /**
         * 蓝色
         */
        BLUE,
        /**
         * 绿色
         */
        GREEN,
        /**
         * 删除线
         */
        STRICKOUT
    }
}
