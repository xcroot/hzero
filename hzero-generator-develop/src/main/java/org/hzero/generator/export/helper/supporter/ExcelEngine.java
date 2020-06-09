package org.hzero.generator.export.helper.supporter;

import org.hzero.generator.export.helper.exception.LiquibaseHelperException;
import oracle.sql.CLOB;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Excel Helper
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 9:59
 */
public class ExcelEngine {
    private static final Logger logger = LoggerFactory.getLogger(ExcelEngine.class);
    private Workbook workbook;
    private File excelFile;
    private Map<String, Sheet> sheetMap = new HashMap<>();

    public ExcelEngine(File file) {
        excelFile = file;
        String fileName = file.getName();
        Assert.isTrue(fileName.contains("."), "请确认指定的文件是否正确的后缀！");
        try {
            switch (fileName.substring(fileName.lastIndexOf('.'))) {
                case ".xls":
                    throw new LiquibaseHelperException("请使用Excel 2007及以上版本！");
                case ".xlsx":
                    workbook = new XSSFWorkbook(new FileInputStream(file));
                    break;
                default:
                    throw new LiquibaseHelperException("请确认指定的文件是否正确的后缀！");
            }
        } catch (IOException e) {
            throw new LiquibaseHelperException("文件读取失败！");
        }
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public boolean checkSheetExist(String sheetName) {
        return workbook.getSheet(sheetName) != null;
    }

    public void createSheet(String sheetName) {
        // 校验 Sheet 页是不是存在
        Assert.isTrue(!checkSheetExist(sheetName), "Excel Sheet 页 [" + sheetName + "] 已经存在！");

        Sheet sheet = workbook.createSheet(sheetName);
        sheetMap.put(sheetName, sheet);
    }

    public void writeCell(String sheetName, CellData cellData) {
        if (cellData.getValue() == null) {
            return;
        }
        Assert.isTrue(sheetMap.containsKey(sheetName), "无法找到 [" + sheetName + "]");
        Sheet sheet = sheetMap.get(sheetName);
        Row row = sheet.getRow(cellData.getRow() - 1);
        if (row == null) {
            row = sheet.createRow(cellData.getRow() - 1);
        }
        Cell cell = row.getCell(cellData.getColumn() - 1);
        if (cell == null) {
            cell = row.createCell(cellData.getColumn() - 1);
        }
        if (cellData.getValue() instanceof Boolean){
            cellData.setValue((boolean)cellData.getValue() ? 1 : 0);
        }
        try {
            if (cellData.isFormula() && String.valueOf(cellData.getValue()).startsWith("=")) {
                cell.setCellFormula(String.valueOf(cellData.getValue()).substring(1));
                cell.setCellType(CellType.FORMULA);
            } else {
                if (cellData.getValue() instanceof CLOB){
                    cell.setCellValue(String.valueOf(((CLOB) cellData.getValue()).stringValue()));
                }else {
                    cell.setCellValue(String.valueOf(cellData.getValue()));
                }

            }
        } catch (Exception e) {
            logger.error("写入数据失败：{}", e);
        }
        if (cellData.getCellStyle() != null) {
            CellStyle cellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            switch (cellData.getCellStyle()) {
                case BOLD:
                    font.setBold(true);
                    cellStyle.setFont(font);
                    break;
                case ORANGE:
                    font.setColor(IndexedColors.ORANGE.index);
                    break;
                case BLUE:
                    font.setColor(IndexedColors.LIGHT_BLUE.index);
                    break;
                case GREEN:
                    font.setColor(IndexedColors.LIGHT_GREEN.index);
                    break;
                case STRICKOUT:
                    font.setStrikeout(true);
                    break;
                default:
                    break;
            }
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
        }
    }

    public void writeCell(String sheetName, List<CellData> initCellList) {
        if (CollectionUtils.isEmpty(initCellList)) {
            return;
        }
        initCellList.forEach(cellData -> writeCell(sheetName, cellData));
    }

    public void writeFile() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(excelFile)) {
            // 重新计算
            workbook.setForceFormulaRecalculation(true);
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            throw new LiquibaseHelperException("写入文件时发生错误！");
        }
    }
}
