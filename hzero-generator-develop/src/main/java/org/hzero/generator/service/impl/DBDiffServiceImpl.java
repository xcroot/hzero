package org.hzero.generator.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hzero.generator.service.IDBDiffService;
import org.hzero.generator.service.IDBInfoService;
import org.hzero.generator.util.DateUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * 数据库差异对比服务
 * 
 * @author xianzhi.chen@hand-china.com 2018年9月17日下午1:48:25
 */
@Service
public class DBDiffServiceImpl implements IDBDiffService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IDBInfoService dBInfoService;

    @Override
    public List<String> selectDatabase(String env) {
        List<String> databases;
        switch (env) {
            case IDBInfoService.ENV_DEV:
                databases = dBInfoService.selectDevDatabase();
                break;
            case IDBInfoService.ENV_TST:
                databases = dBInfoService.selectTstDatabase();
                break;
            case IDBInfoService.ENV_UAT:
                databases = dBInfoService.selectUatDatabase();
                break;
            case IDBInfoService.ENV_PRD:
                databases = dBInfoService.selectPrdDatabase();
                break;
            default:
                databases = new ArrayList<>();
                break;
        }
        return databases;
    }

    @Override
    public List<String> selectDatabaseTable(String env, String dbname) {
        List<String> tables;
        switch (env) {
            case IDBInfoService.ENV_DEV:
                tables = dBInfoService.selectDevDatabaseTable(dbname);
                break;
            case IDBInfoService.ENV_TST:
                tables = dBInfoService.selectTstDatabaseTable(dbname);
                break;
            case IDBInfoService.ENV_UAT:
                tables = dBInfoService.selectUatDatabaseTable(dbname);
                break;
            case IDBInfoService.ENV_PRD:
                tables = dBInfoService.selectPrdDatabaseTable(dbname);
                break;
            default:
                tables = new ArrayList<>();
                break;
        }
        return tables;
    }


    @Override
    public List<Map<String, String>> selectDatabaseColumn(String env, String dbname) {
        List<Map<String, String>> columns;
        switch (env) {
            case IDBInfoService.ENV_DEV:
                columns = dBInfoService.selectDevDatabaseColumn(dbname);
                break;
            case IDBInfoService.ENV_TST:
                columns = dBInfoService.selectTstDatabaseColumn(dbname);
                break;
            case IDBInfoService.ENV_UAT:
                columns = dBInfoService.selectUatDatabaseColumn(dbname);
                break;
            case IDBInfoService.ENV_PRD:
                columns = dBInfoService.selectPrdDatabaseColumn(dbname);
                break;
            default:
                columns = new ArrayList<>();
                break;
        }
        return columns;
    }

    @Override
    public List<Map<String, String>> selectDatabaseIndex(String env, String dbname) {
        List<Map<String, String>> indexs;
        switch (env) {
            case IDBInfoService.ENV_DEV:
                indexs = dBInfoService.selectDevDatabaseIndex(dbname);
                break;
            case IDBInfoService.ENV_TST:
                indexs = dBInfoService.selectTstDatabaseIndex(dbname);
                break;
            case IDBInfoService.ENV_UAT:
                indexs = dBInfoService.selectUatDatabaseIndex(dbname);
                break;
            case IDBInfoService.ENV_PRD:
                indexs = dBInfoService.selectPrdDatabaseIndex(dbname);
                break;
            default:
                indexs = new ArrayList<>();
                break;
        }
        return indexs;
    }

    @Override
    @Transactional
    public void dbUpdateImport(String updateEnv, String updateDB, MultipartFile xmlFile) throws Exception {
        List<String> sqls = getExecuteSqls(updateDB, xmlFile);
        for (String sql : sqls) {
            // 打印脚本
            logger.info("==>" + sql);
            switch (updateEnv) {
                case IDBInfoService.ENV_DEV:
                    dBInfoService.updateDevDatabase(sql);
                    break;
                case IDBInfoService.ENV_TST:
                    dBInfoService.updateTstDatabase(sql);
                    break;
                case IDBInfoService.ENV_UAT:
                    dBInfoService.updateUatDatabase(sql);
                    break;
                case IDBInfoService.ENV_PRD:
                    dBInfoService.updatePrdDatabase(sql);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Document compareDiff(String sourceEnv, String sourceDB, String targetEnv, String targetDB) {
        // 来源数据库信息
        List<String> sourceTables = selectDatabaseTable(sourceEnv, sourceDB);
        List<Map<String, String>> sourceColumns = selectDatabaseColumn(sourceEnv, sourceDB);
        List<Map<String, String>> sourceIndexs = selectDatabaseIndex(sourceEnv, sourceDB);
        // 目标数据库信息
        List<String> targetTables = selectDatabaseTable(targetEnv, targetDB);
        List<Map<String, String>> targetColumns = selectDatabaseColumn(targetEnv, targetDB);
        List<Map<String, String>> targetIndexs = selectDatabaseIndex(targetEnv, targetDB);
        // 差异XML文档
        Document document = new Document();
        Element rootElement = new Element("databaseChangeLog");
        document.setRootElement(rootElement);
        // 脚本计数器
        int i = 1;
        // 新增表
        i = processAddTable(sourceTables, targetTables, sourceColumns, targetColumns, rootElement, i);
        // 删除索引
        i = processDelIndex(sourceIndexs, targetIndexs, rootElement, i);
        // 删除表
        i = processDelTable(sourceTables, targetTables, sourceColumns, targetColumns, rootElement, i);
        // 新增索引
        i = processAddIndex(sourceIndexs, targetIndexs, rootElement, i);
        return document;
    }

    // 新增表信息
    private int processAddTable(List<String> sourceTables, List<String> targetTables,
                    List<Map<String, String>> sourceColumns, List<Map<String, String>> targetColumns,
                    Element rootElement, int i) {
        //boolean noExist = true;
        // 差异处理
        for (String sourceTable : sourceTables) {
            if (targetTables.contains(sourceTable)) {
                for (Map<String, String> sourceColumn : sourceColumns) {
                    if (sourceTable.equals(sourceColumn.get("TABLE_NAME"))) {
                        boolean isColumnModify = false;
                        boolean noExist = true;
                        for (Map<String, String> targetColumn : targetColumns) {
                            if (sourceTable.equals(targetColumn.get("TABLE_NAME")) && sourceColumn.get("COLUMN_NAME")
                                            .equals(targetColumn.get("COLUMN_NAME"))) {
                                noExist = false;
                                if (!sourceColumn.get("COL_DEF").equals(targetColumn.get("COL_DEF"))) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("列改变").append(targetColumn.get("TABLE_NAME")).append("-")
                                                    .append(sourceColumn.get("COLUMN_NAME")).append(":")
                                                    .append(targetColumn.get("COL_DEF")).append("==>")
                                                    .append(sourceColumn.get("COL_DEF"));
                                    logger.info(sb.toString());
                                    isColumnModify = true;
                                }
                            }
                        }
                        // 字段被修改
                        if (isColumnModify) {
                            Element sqlElement = getSqlElement(i++);
                            StringBuilder sb = new StringBuilder();
                            sb.append("ALTER TABLE ");
                            sb.append(sourceTable);
                            sb.append(" MODIFY COLUMN ");
                            sb.append(sourceColumn.get("COL_DEF"));
                            sqlElement.addContent(sb.toString());
                            // 加入XML文档
                            rootElement.addContent(sqlElement);
                        }
                        // 字段新增
                        if (noExist) {
                            Element sqlElement = getSqlElement(i++);
                            StringBuilder sb = new StringBuilder();
                            sb.append("ALTER TABLE ");
                            sb.append(sourceTable);
                            sb.append(" ADD COLUMN ");
                            sb.append(sourceColumn.get("COL_DEF"));
                            sqlElement.addContent(sb.toString());
                            // 加入XML文档
                            rootElement.addContent(sqlElement);
                        }
                    }
                }
            } else {
                int fst = 1;
                String priCols = "";
                StringBuilder sb = new StringBuilder();
                sb.append("CREATE TABLE ");
                sb.append(sourceTable);
                sb.append("( ");
                for (Map<String, String> sourceColumn : sourceColumns) {
                    if (sourceTable.equals(sourceColumn.get("TABLE_NAME"))) {
                        if (fst == 0) {
                            sb.append(",\n");
                        }
                        sb.append(sourceColumn.get("COL_DEF"));
                        fst = 0;
                        if ("PRI".equals(sourceColumn.get("COLUMN_KEY"))) {
                            if (priCols.length() > 1) {
                                priCols = priCols + ",";
                            }
                            priCols = priCols + sourceColumn.get("COLUMN_NAME");
                        }
                    }
                }
                if (priCols.length() > 1) {
                    sb.append(",\n PRIMARY KEY (");
                    sb.append(priCols);
                    sb.append(")");
                }
                sb.append(")");
                // 拼接节点
                Element sqlElement = getSqlElement(i++);
                sqlElement.addContent(sb.toString());
                // 加入XML文档
                rootElement.addContent(sqlElement);
            }
        }
        return i;
    }

    // 删除表信息
    private int processDelTable(List<String> sourceTables, List<String> targetTables,
                    List<Map<String, String>> sourceColumns, List<Map<String, String>> targetColumns,
                    Element rootElement, int i) {
        // 删除表逻辑处理
        for (String targetTable : targetTables) {
            if (!sourceTables.contains(targetTable)) {
                Element sqlElement = getSqlElement(i++);
                StringBuilder sb = new StringBuilder();
                sb.append("DROP TABLE ");
                sb.append(targetTable);
                sqlElement.addContent(sb.toString());
                // 加入XML文档
                rootElement.addContent(sqlElement);
            } else {
                for (Map<String, String> targetColumn : targetColumns) {
                    if (targetTable.equals(targetColumn.get("TABLE_NAME"))) {
                        boolean colNoExist = true;
                        for (Map<String, String> sourceColumn : sourceColumns) {
                            if (targetColumn.get("COLUMN_NAME").equals(sourceColumn.get("COLUMN_NAME"))) {
                                colNoExist = false;
                            }
                        }
                        if (colNoExist) {
                            Element sqlElement = getSqlElement(i++);
                            StringBuilder sb = new StringBuilder();
                            sb.append("ALTER TABLE ");
                            sb.append(targetTable);
                            sb.append(" ");
                            sb.append("DROP COLUMN ");
                            sb.append(targetColumn.get("COLUMN_NAME"));
                            sqlElement.addContent(sb.toString());
                            // 加入XML文档
                            rootElement.addContent(sqlElement);
                        }
                    }
                }
            }
        }
        return i;
    }

    // 删除索引
    private int processDelIndex(List<Map<String, String>> sourceIndexs, List<Map<String, String>> targetIndexs,
                    Element rootElement, int i) {
        boolean isExist = false;
        // 删除存在差异索引
        for (Map<String, String> targetIndex : targetIndexs) {
            isExist = true;
            for (Map<String, String> sourceIndex : sourceIndexs) {
                if (targetIndex.get("TABLE_NAME").equals(sourceIndex.get("TABLE_NAME"))
                                && targetIndex.get("INDEX_DEF").equals(sourceIndex.get("INDEX_DEF"))) {
                    isExist = false;
                }
            }
            if (isExist) {
                Element sqlElement = getActionSqlElement(i++);
                StringBuilder sb = new StringBuilder();
                sb.append("ALTER TABLE ");
                sb.append(targetIndex.get("TABLE_NAME"));
                sb.append(" ");
                sb.append("DROP INDEX ");
                sb.append(targetIndex.get("INDEX_NAME"));
                sqlElement.addContent(sb.toString());
                // 加入XML文档
                rootElement.addContent(sqlElement);
            }
        }
        return i;
    }

    // 新增索引
    private int processAddIndex(List<Map<String, String>> sourceIndexs, List<Map<String, String>> targetIndexs,
                    Element rootElement, int i) {
        boolean isExist = false;
        // 新增目标库不存在索引
        for (Map<String, String> sourceIndex : sourceIndexs) {
            isExist = true;
            for (Map<String, String> targetIndex : targetIndexs) {
                if (sourceIndex.get("TABLE_NAME").equals(targetIndex.get("TABLE_NAME"))
                                && sourceIndex.get("INDEX_DEF").equals(targetIndex.get("INDEX_DEF"))) {
                    isExist = false;
                }
            }
            if (isExist) {
                Element sqlElement = getSqlElement(i++);
                StringBuilder sb = new StringBuilder();
                sb.append("ALTER TABLE ");
                sb.append(sourceIndex.get("TABLE_NAME"));
                sb.append(" ");
                sb.append(sourceIndex.get("INDEX_DEF"));
                sqlElement.addContent(sb.toString());
                // 加入XML文档
                rootElement.addContent(sqlElement);
            }
        }
        return i;
    }

    /**
     * 
     * 获取XML中表SQL节点
     * 
     * @return
     */
    private Element getSqlElement(int i) {
        Element sqlElement = new Element("sql");
        sqlElement.setAttribute("id", String.valueOf(i));
        sqlElement.setAttribute("time", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        return sqlElement;
    }

    /**
     * 
     * 获取XML中索引SQL节点
     * 
     * @return
     */
    private Element getActionSqlElement(int i) {
        Element sqlElement = new Element("sql");
        sqlElement.setAttribute("id", String.valueOf(i));
        sqlElement.setAttribute("time", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        sqlElement.setAttribute("type", "TABLE");
        sqlElement.setAttribute("action", "ALTER");
        return sqlElement;
    }

    private List<String> getExecuteSqls(String updateDB, MultipartFile xmlFile) throws Exception {
        List<String> sqls = new LinkedList<>();
        SAXBuilder sax = new SAXBuilder();
        InputStream is = xmlFile.getInputStream();
        Document doc = sax.build(is);
        Element root = doc.getRootElement();
        // 获得根节点下的节点数据
        List<Element> list = root.getChildren();
        sqls.add(0, "use " + updateDB);
        for (Element e : list) {
            sqls.add(Integer.parseInt(e.getAttributeValue("id")), e.getValue());
        }
        return sqls;
    }

}
