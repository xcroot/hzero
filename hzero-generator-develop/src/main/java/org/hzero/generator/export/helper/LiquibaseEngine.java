package org.hzero.generator.export.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.config.FileConfig;
import org.hzero.generator.export.constants.Constants;
import org.hzero.generator.export.helper.entity.Column;
import org.hzero.generator.export.helper.entity.Data;
import org.hzero.generator.export.helper.entity.DataGroup;
import org.hzero.generator.export.helper.entity.DataSet;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.hzero.generator.export.helper.exception.LiquibaseHelperException;
import org.hzero.generator.export.helper.mapper.LiquibaseHelperMapper;
import org.hzero.generator.export.helper.supporter.CellData;
import org.hzero.generator.export.helper.supporter.ExcelEngine;
import org.hzero.generator.export.helper.supporter.FileHelper;
import org.hzero.generator.liquibase.excel.TableData;
import org.hzero.generator.util.DBConfigUtils;
import org.hzero.generator.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * Liquibase Engine
 * </p>
 *
 * @author qingsheng.chen 2018/11/24 星期六 9:32
 */
@Component
public class LiquibaseEngine implements ApplicationContextAware {
    public static final String ORACLE = "oracle";
    public static final String MYSQL = "mysql";
    public static final String SQLSERVER = "sqlserver";
    public static final String POSTGRESQL = "postgresql";
    private static final Logger logger = LoggerFactory.getLogger(LiquibaseEngine.class);
    private static final List<CellData> INIT_CELL;
    private static final int START_ROW = 7;
    private static final int START_COLUMN = 5;
    protected static DBConfigUtils dbConfigUtils;
    protected static int nextStartRow = START_ROW;
    private static ApplicationContext applicationContext;
    private static LiquibaseHelperMapper liquibaseHelperMapper;
    private static FileConfig fileConfig;

    static {
        INIT_CELL = new ArrayList<>();
        INIT_CELL.add(new CellData("A", 1, "日期", false, CellData.CellStyle.BOLD));
        INIT_CELL.add(new CellData("B", 1, "作者", false, CellData.CellStyle.BOLD));
        INIT_CELL.add(new CellData("C", 1, "说明", false, CellData.CellStyle.BOLD));
        INIT_CELL.add(new CellData("D", 1, "表", false, CellData.CellStyle.BOLD));
        INIT_CELL.add(new CellData("E", 4, "*自动生成", false, CellData.CellStyle.ORANGE));
        INIT_CELL.add(new CellData("F", 4, "#唯一性检查", false, CellData.CellStyle.BLUE));
        INIT_CELL.add(new CellData("G", 4, "公式=外键引用", false, CellData.CellStyle.GREEN));
    }

    protected ExcelEngine excelEngine;
    protected List<TableData> tableDataList;
    protected List<DataSet> dataSets = new ArrayList<>();
    protected DataSourceProperty dsp = new DataSourceProperty();
    private String filePath;
    /**
     * 是否去掉未找到关联的数据
     */
    private boolean clearFlag = true;
    private LiquibaseEngineMode engineMode;
    private File excelFile;
    private List<DataGroup> dataGroupList;
    private Map<String, List<CellData>> sheetTitleMap = new HashMap<>();
    private Map<Object, CellData> refMap = new HashMap<>();

    public LiquibaseEngine() {
    }

    public static LiquibaseEngine createEngine(String filePath, LiquibaseEngineMode engineMode) {
        LiquibaseEngine liquibaseEngine = new LiquibaseEngine();
        liquibaseEngine.setFilePath(filePath);
        liquibaseEngine.setEngineMode(engineMode);
        liquibaseEngine.loadFile().loadExcel();
        return liquibaseEngine;
    }

    static FileConfig getFileConfig() {
        if (fileConfig == null) {
            fileConfig = applicationContext.getBean(FileConfig.class);
        }
        return fileConfig;
    }

    static LiquibaseHelperMapper getLiquibaseHelperMapper() {
        if (liquibaseHelperMapper == null) {
            liquibaseHelperMapper = applicationContext.getBean(LiquibaseHelperMapper.class);
            dbConfigUtils = applicationContext.getBean(DBConfigUtils.class);
        }
        return liquibaseHelperMapper;
    }

    // 获取文件名
    private static String getFileName(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }
        try {
            // 第一个@之后的
            int index = fileUrl.indexOf("@");
            if (index > -1) {
                return fileUrl.substring(index + 1);
            } else {
                String[] s = fileUrl.split("/");
                return s[s.length - 1];
            }
        } catch (Exception e) {
            logger.error("Get filename failed : {}", e);
            return null;
        }
    }

    /**
     * 将InputStream写入本地文件
     *
     * @param destination 写入本地目录
     * @param input       输入流
     */
    private static void writeToLocal(String destination, InputStream input) throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }

    public LiquibaseEngine loadFile() {
        Assert.notNull(filePath, "指定的文件路径不能为 [NULL]！");
        if (engineMode == LiquibaseEngineMode.OVERRIDE) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                throw new LiquibaseHelperException("文件删除失败！");
            }
            engineMode = LiquibaseEngineMode.CREATE;
        }
        this.excelFile = new File(filePath);
        if (engineMode == LiquibaseEngineMode.CREATE) {
            try {
                if (!excelFile.getParentFile().exists()) {
                    excelFile.getParentFile().mkdirs();
                }
                Assert.isTrue(excelFile.createNewFile(), "文件创建失败！");
            } catch (IOException e) {
                throw new LiquibaseHelperException("文件创建失败！", e);
            }
        }
        if (!excelFile.isFile()) {
            excelFile = null;
            throw new LiquibaseHelperException("指定的路径不是一个文件！");
        }
        if (engineMode == LiquibaseEngineMode.CREATE) {
            InputStream in = getClass().getClassLoader().getResourceAsStream("static/liquibase-template.xlsx");
            FileHelper.copyFile(in, excelFile);
        }
        return this;
    }

    public LiquibaseEngine loadExcel() {
        Assert.notNull(excelFile, "找不到加载的excel文件！");
        excelEngine = new ExcelEngine(excelFile);
        return this;
    }

    public void generate() {
        logger.debug("Generate Excel : {}", dataGroupList);
        if (CollectionUtils.isEmpty(dataGroupList)) {
            logger.warn("初始化内容为空！");
            return;
        }
        logger.info("开始生成数据....");
        dataGroupList.forEach(this::generateSheet);
        // 按需过滤数据
        dataSets = dataFilter(dataSets);
        int count = 0;
        for (DataSet dataSet : dataSets) {
            count += dataSet.getDataSet().size();
        }
        if (count == 0 && excelFile.exists()) {
            excelFile.delete();
            logger.info("数据为空，{} 文件已删除", excelFile);
            return;
        }
        writeData(dataSets);
        logger.info("开始写入文件....");
        excelEngine.writeFile();
        logger.info("写入文件成功！");
    }

    public void generateSheet(DataGroup dataGroup) {
        Assert.isTrue(StringUtils.isNotBlank(dataGroup.getSheetName()), "Excel Sheet页名称不能为空！");
        // 创建 Sheet 页
        logger.info("开始创建Sheet页 {}...", dataGroup.getSheetName());
        excelEngine.createSheet(dataGroup.getSheetName());
        // 初始化 Sheet 页数据
        excelEngine.writeCell(dataGroup.getSheetName(), INIT_CELL);
        if (StringUtils.isEmpty(dataGroup.getSheetName())) {
            return;
        }
        nextStartRow = START_ROW;
        dataGroup.getDataList().forEach(data -> generateData(dataGroup, data));
    }

    public void generateData(final DataGroup dataGroup, Data data) {
        // load data
        logger.info("开始查询数据 {}...", data.getTableName());
        LiquibaseHelperMapper liquibaseHelperMapper = getLiquibaseHelperMapper();
        String env = DynamicDataSourceContextHolder.getDataSourceLookupKey();
        Map<String, String> dataSource = dbConfigUtils.getMapByEnv(StringUtils.defaultIfBlank(env, "gen"));
        boolean isOracle = StringUtils.contains(dataSource.get("url"), ORACLE);
        if (StringUtils.contains(dataSource.get("url"), MYSQL) || StringUtils.contains(dataSource.get("url"), SQLSERVER)) {
            // 选择数据库
            liquibaseHelperMapper.selectSchema(data.getSchemaName());
        }/* else if (StringUtils.contains(dataSource.get("url"), POSTGRESQL)){
            dsp.setDriverClassName(dataSource.get("driver-class-name"));
            dsp.setUrl(dataSource.get("url") + data.getSchemaName());
            dsp.setUsername(dataSource.get("username"));
            dsp.setPassword(dataSource.get("password"));
            DataSource ds = DataSourceFactory.createDataSource(dsp);
            DataSourceUtils.getConnection(ds);
        }*/
        List<Map<String, Object>> dataList = liquibaseHelperMapper.selectData(data.getTableName(), data.getColumnList(), data.getWhere());
        logger.info("查询全部数据数据 {} 条记录...", dataList.size());
        // 把数据存储起来最后写入
        appendData(dataList, dataGroup.getSheetName(), data);

        // 处理需要下载的文件
        downloadFile(data.getServiceName(), data.getSchemaName(), data.getColumnList(), dataList);
    }

    private void downloadFile(String serviceName, String schemaName, List<Column> columnList, List<Map<String, Object>> dataList) {
        FileConfig config = getFileConfig();
        Map<String, String> downloadMap = columnList.stream().filter(Column::isDownload).collect(Collectors.toMap(Column::getColumnName, Column::getBucket, (k1, k2) -> k2));
        if (CollectionUtils.isEmpty(downloadMap)) {
            return;
        }
        dataList.forEach(map ->
                map.forEach((k, v) -> {
                    String fieldName;
                    if (k.startsWith("*") || k.startsWith("#")) {
                        fieldName = k.substring(1);
                    } else {
                        fieldName = k;
                    }
                    String fileUrl = String.valueOf(v);
                    if (StringUtils.isNotBlank(fileUrl) && downloadMap.containsKey(fieldName)) {
                        // 下载文件
                        String bucketName = downloadMap.get(fieldName);
                        String filename = getFileName(fileUrl);
                        String url = config.getTokenUrl() + "?access_token=" + config.getAccessToken() + "&bucketName=" + bucketName + "&url=" + fileUrl;
                        String signUrl = null;
                        try {
                            signUrl = IOUtils.toString(HttpUtils.get(url), StandardCharsets.UTF_8);
                            InputStream inputStream = HttpUtils.get(signUrl);
                            // 写入本地
                            String path = Constants.BASE_OUTPUT_PATH + serviceName + "/" + schemaName + "/" + filename;
                            writeToLocal(path, inputStream);
                        } catch (Exception e) {
                            logger.error("download file failed. url : {}", signUrl);
                        }
                    }
                }));
    }

    private void appendData(List<Map<String, Object>> dataList, String sheetName, Data data) {
        DataSet dataSet = new DataSet();
        dataSet.setSheetName(sheetName);
        dataSet.setTableName(data.getTableName());
        dataSet.setData(data);
        dataSet.setDataSet(new LinkedHashSet<>(dataList));
        dataSets.add(dataSet);
    }

    public void writeData(List<DataSet> dataSets) {
        // 数据过滤后写入
        String sheetName = "";
        for (DataSet dataSet : dataSets) {
            // 换sheet页初始化nextStartRow
            if (!StringUtils.equals(sheetName, dataSet.getSheetName())) {
                nextStartRow = START_ROW;
                sheetName = dataSet.getSheetName();
            }
            // 初始化 Title
            logger.info("开始写入标题 {}...", dataSet.getTableName());
            excelEngine.writeCell(dataSet.getSheetName(), initCell(dataSet.getData()));
            AtomicInteger rowIndex = new AtomicInteger(nextStartRow + 1);
            Set<Map<String, Object>> dataMaps = dataSet.getDataSet();
            logger.info("写入数据{}...", dataMaps.size());
            for (Map<String, Object> dataMap : dataMaps) {
                List<CellData> cellDataList = initDataCell(dataSet.getSheetName(), dataSet.getTableName(), dataMap, rowIndex.get());
                if (!CollectionUtils.isEmpty(cellDataList)) {
                    rowIndex.incrementAndGet();
                    excelEngine.writeCell(dataSet.getSheetName(), cellDataList);
                }
            }
            logger.info("写入完成{}...", dataMaps.size());
            nextStartRow = rowIndex.incrementAndGet();
        }
    }

    /**
     * 数据过滤
     *
     * @param dataSets 需要过滤的数据
     * @return 过滤后的数据
     */
    public List<DataSet> dataFilter(List<DataSet> dataSets) {
        return dataSets;
    }

    /**
     * 初始化表字段行
     *
     * @param data 表结构
     * @return 单元格数据
     */
    public List<CellData> initCell(Data data) {
        List<CellData> cellData = new LinkedList<>();
        cellData.add(new CellData("A", nextStartRow, data.getCreationDateText()));
        cellData.add(new CellData("B", nextStartRow, data.getAuthor()));
        cellData.add(new CellData("C", nextStartRow, data.getDescription()));
        cellData.add(new CellData("D", nextStartRow, data.getTableName(), false, CellData.CellStyle.BOLD));
        if (!CollectionUtils.isEmpty(data.getColumnList())) {
            AtomicInteger columnIndex = new AtomicInteger(START_COLUMN);
            data.getColumnList().forEach(column -> {
                if (column.isMultiLang()) {
                    Assert.isTrue(StringUtils.isNotBlank(column.getPkName()), "请指定多语言字段的主键名称！");
                    column.getLang().forEach(lang -> {
                        CellData columnCell = new CellData(columnIndex.getAndIncrement(), nextStartRow, column.getColumnName() + ":" + lang, column.isFormula(), column.getCellStyle());
                        cellData.add(columnCell);
                    });
                } else {
                    CellData columnCell = new CellData(columnIndex.getAndIncrement(), nextStartRow, column.getColumnNameText(), column.isFormula(), column.getCellStyle());
                    cellData.add(columnCell.setCited(column.isCited()).setAutoGenerate(column.isAutoGenerate()).setId(column.isId()).setColumnName(column.getColumnName())
                            .setRelTableName(column.getReference() != null ? column.getReference().getTableName() : null));
                }
            });
        }
        sheetTitleMap.put(data.getTableName(), cellData);
        return cellData;
    }

    public List<CellData> initDataCell(String sheetName, String tableName, Map<String, Object> dataMap, int currentRowIndex) {
        List<CellData> cellData = new LinkedList<>();
        Assert.isTrue(sheetTitleMap.containsKey(tableName), "无法找到表名对应的列配置！");
        List<CellData> titleCellList = sheetTitleMap.get(tableName);
        for (int i = START_COLUMN - 1; i < titleCellList.size(); ++i) {
            if (titleCellList.get(i).isCited()) {
                refMap.put(tableName + "-" + dataMap.get(String.valueOf(titleCellList.get(i).getValue())), CellData.copy(titleCellList.get(i)).setSheetName(sheetName).setRow(currentRowIndex));
            }
            if (titleCellList.get(i).isAutoGenerate()) {
                dataMap.put(String.valueOf(titleCellList.get(i).getValue()), "*");
            }
            Object value;
            if (titleCellList.get(i).isCited()) {
                value = tableName + "-" + currentRowIndex;
            } else if (titleCellList.get(i).isFormula()) {
                CellData ref = refMap.get(titleCellList.get(i).getRelTableName() + "-" + dataMap.get(String.valueOf(titleCellList.get(i).getValue())));
                // 自关联根节点处理
                if (ref == null && tableName.equals(titleCellList.get(i).getRelTableName())) {
                    value = dataMap.get(String.valueOf(titleCellList.get(i).getValue()));
                } else if (ref == null && !clearFlag) {
                    // 没有找到关联,但不清理
                    value = dataMap.get(String.valueOf(titleCellList.get(i).getValue()));
                } else if (ref == null && dataMap.get(String.valueOf(titleCellList.get(i).getValue())) == null) {
                    // 为null不清理
                    value = null;
                } else if (ref == null) {
                    // 没有找到关联,去除无用数据
                    logger.warn("无法找到引用 {}", dataMap);
                    return null;
                } else {
                    StringBuilder sb = new StringBuilder("=");
                    if (StringUtils.isNotBlank(ref.getSheetName())) {
                        sb.append(ref.getSheetName()).append("!");
                    }
                    sb.append("$").append(ref.getColumnText()).append("$").append(ref.getRow());
                    value = sb.toString();
                }
            } else {
                value = dataMap.get(String.valueOf(titleCellList.get(i).getValue()));
            }
            CellData newCell = new CellData(i + 1, currentRowIndex, value, titleCellList.get(i).isFormula());
            if (i == START_COLUMN - 1 && !clearFlag) {
                newCell.setCellStyle(CellData.CellStyle.STRICKOUT);
            }
            cellData.add(newCell);
        }
        return cellData;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isClearFlag() {
        return clearFlag;
    }

    public void setClearFlag(boolean clearFlag) {
        this.clearFlag = clearFlag;
    }

    public LiquibaseEngineMode getEngineMode() {
        return engineMode;
    }

    public void setEngineMode(LiquibaseEngineMode engineMode) {
        this.engineMode = engineMode;
    }

    public File getExcelFile() {
        return excelFile;
    }

    public void setExcelFile(File excelFile) {
        this.excelFile = excelFile;
    }

    public ExcelEngine getExcelEngine() {
        return excelEngine;
    }

    public LiquibaseEngine setExcelEngine(ExcelEngine excelEngine) {
        this.excelEngine = excelEngine;
        return this;
    }

    public List<DataGroup> getDataGroupList() {
        return dataGroupList;
    }

    public void setDataGroupList(List<DataGroup> dataGroupList) {
        this.dataGroupList = dataGroupList;
    }

    public List<TableData> getTableDataList() {
        return tableDataList;
    }

    public void setTableDataList(List<TableData> tableDataList) {
        this.tableDataList = tableDataList;
    }

    public List<DataSet> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<DataSet> dataSets) {
        this.dataSets = dataSets;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LiquibaseEngine.applicationContext = applicationContext;
    }
}
