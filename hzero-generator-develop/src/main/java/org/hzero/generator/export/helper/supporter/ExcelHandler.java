package org.hzero.generator.export.helper.supporter;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jianbo.li@hand-china.com 2019/3/04
 */
public class ExcelHandler {

    /**
     * 公式引用sheetName结束的标识符
     */
    private static final String FORMULA_SHEET_NAME_END_FLAG = "!";

    /**
     * 空字符串标识符
     */
    private static final String EMPTY = "";

    /**
     * 临时文件
     */
    private static final String TEM_FILE = "temp.xlsx";

    /**
     * 提取正常公式的正则表达式
     */
    private static final String REGEX = ".*!(\\$.+)";

    /**
     * 判断是否是公式类型
     *
     * @param cell 单元格
     * @return 若是返回true
     */
    private static boolean isFormula(XSSFCell cell) {
        return cell != null && cell.getCellType() == CellType.FORMULA;
    }

    /**
     * 提取公式
     *
     * @param formula 公式
     * @return 返回提取的公式
     */
    private static String extractFormula(String formula) {
        return getFirstMatcher(formula, 1, Pattern.compile(REGEX));
    }

    /**
     * 获取匹配上的正则字符串
     *
     * @param str        待处理字符串
     * @param groupIndex 指标
     * @param pattern    正则表达式
     * @return 返回匹配上的增则表达式字符串
     */
    private static String getFirstMatcher(String str, int groupIndex, Pattern pattern) {
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            String mstr = m.group(groupIndex);
            if (mstr != null) {
                return mstr;
            }
        }
        return null;
    }

    /**
     * @param file 待处理的excel
     */
    public static void doHandleExcel(String file) {
        File srcFile = new File(file);
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(srcFile);
            fileOutputStream = new FileOutputStream(TEM_FILE);
            XSSFWorkbook hw = new XSSFWorkbook(fileInputStream);
            List<String> sheetList = new ArrayList<>();
            for (int i = 0; i < hw.getNumberOfSheets(); i++) {
                sheetList.add(hw.getSheetName(i));
            }
            for (String sheet : sheetList) {
                XSSFSheet xssfSheet = hw.getSheet(sheet);
                for (int i = 0; i <= xssfSheet.getLastRowNum(); i++) {
                    XSSFRow row = xssfSheet.getRow(i);
                    if (row != null) {
                        for (int j = 0; j <= row.getLastCellNum(); j++) {
                            XSSFCell cell = row.getCell(j);
                            if (ExcelHandler.isFormula(cell)) {
                                String oriFormula = cell.getCellFormula();
                                String formula = ExcelHandler.extractFormula(cell.getCellFormula());
                                String sheetName = oriFormula
                                        .replace(formula, EMPTY)
                                        .replace(FORMULA_SHEET_NAME_END_FLAG, EMPTY);
                                if (sheetList.stream().noneMatch(item -> item.equals(sheetName))) {
                                    cell.setCellFormula(formula);
                                }
                            }
                        }
                    }
                }
            }
            hw.write(fileOutputStream);
            FileCopyUtils.copy(new File(TEM_FILE), srcFile);
            new File(TEM_FILE).deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
