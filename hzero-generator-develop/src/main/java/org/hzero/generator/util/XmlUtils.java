package org.hzero.generator.util;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.hzero.generator.dto.DataUpdateDTO;
import org.hzero.generator.entity.*;
import org.hzero.generator.export.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 描述:解析xml文件
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/12/02 16:01
 */


@Component
public class XmlUtils {

    public static final List<Service> SERVICE_LIST = new ArrayList<>();
    public static final List<Mapping> MAPPING_LIST = new ArrayList<>();
    public static final Map<String, String> DATA_MAP = new HashMap<>();
    public static final Map<String, Mapping> MAPPING_MAP = new HashMap<>();
    public static final Map<String, Config> SCHEMA_MERGE = new HashMap<>();
    public static final Map<String, String> SERVICE_PATH = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);
    public static String UPDATE_EXCLUSION = null;
    /**
     * 是否开启值替换
     */
    public static Boolean ENABLE_REPLACE = null;
    /**
     * 需要过滤掉的文件
     */
    private static final List<String> SKIP_FILE = new ArrayList<>();


    static {
        // 忽略模板文件
        SKIP_FILE.add("template-vacancy.xml");
        SKIP_FILE.add("template-demo.xml");
        SKIP_FILE.add("hzero-iam.xml");
    }

    /**
     * 扫描解析xml文件
     */
    public static void resolver() {
        ClassPathResource classPathResource = new ClassPathResource(Constants.MAPPING_FILE);
        ClassPathResource xmlFile = new ClassPathResource(Constants.XML_PATH);
        ClassPathResource pathResource = new ClassPathResource(Constants.SERVICE_PATH_FILE);
        try {
            InputStream mapping = classPathResource.getInputStream();
            File file = xmlFile.getFile();
            createMapping(mapping);
            handleFile(file);
            if (pathResource.exists()) {
                InputStream servicePath = pathResource.getInputStream();
                createServicePath(servicePath);
            }
        } catch (DocumentException | IOException e) {
            LOGGER.error("解析XML文件出错 >>>>> {}", e.getMessage());
        }
    }

    private static void createServicePath(InputStream servicePath) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(servicePath);
        Element rootElement = document.getRootElement();
        for (Object element: rootElement.elements()) {
            for (Object serviceElement: ((DefaultElement) element).elements()) {
                String fileName = ((DefaultElement) serviceElement).attributeValue("filename");
                if (fileName == null) {
                    fileName = ((DefaultElement) serviceElement).attributeValue("name");
                }
                String path = ((DefaultElement) element).attributeValue("path-prefix") + fileName;
                SERVICE_PATH.put(((DefaultElement) serviceElement).attributeValue("name"), path);
            }
        }
    }

    private static void handleFile(File file) throws DocumentException {
        //处理目录
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    handleFile(subFile);
                }
            }
        } else {
            // 过滤忽略文件
            if (SKIP_FILE.contains(file.getName())) {
                return;
            }
            Service service = createService(file);
            if (SERVICE_LIST.size() > 0) {
                for (int i = 0; i < SERVICE_LIST.size(); i++) {
                    // 服务合并
                    if (StringUtils.equals(SERVICE_LIST.get(i).getName(), service.getName())) {
                        SERVICE_LIST.get(i).getExcelList().addAll(service.getExcelList());
                        return;
                    }
                }
                SERVICE_LIST.add(service);
            } else {
                SERVICE_LIST.add(service);
            }
        }
    }

    /**
     * 解析xml文件封装成Service
     *
     * @param file
     * @return Service
     * @throws DocumentException
     */
    private static Service createService(File file) throws DocumentException {
//        LOGGER.info("开始解析文件：{}", file.getName());
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        Element rootElement = document.getRootElement();
        Service service = new Service();
        service.setName(rootElement.attributeValue("name"));
        service.setOrder(Integer.parseInt(rootElement.attributeValue("order")));
        service.setDescription(MAPPING_MAP.get(service.getName()) != null ? MAPPING_MAP.get(service.getName()).getDescription() : rootElement.attributeValue("description"));
        List<Excel> excels = new ArrayList<>();
        rootElement.elements().forEach(excelElement -> {
            Excel excel = new Excel();
            excel.setName(((DefaultElement) excelElement).attributeValue("name"));
            excel.setFileName(((DefaultElement) excelElement).attributeValue("fileName"));
            excel.setDescription(((DefaultElement) excelElement).attributeValue("description"));
            excel.setSchema(((DefaultElement) excelElement).attributeValue("schema"));
            List<Sheet> sheets = new ArrayList<>();
            ((DefaultElement) excelElement).elements().forEach(sheetElement -> {
                Sheet sheet = new Sheet();
                sheet.setName(((DefaultElement) sheetElement).attributeValue("name"));
                sheet.setVersion(((DefaultElement) sheetElement).attributeValue("version"));
                sheet.setDescription(((DefaultElement) sheetElement).attributeValue("description"));
                List<Table> tables = new ArrayList<>();
                ((DefaultElement) sheetElement).elements().forEach(tableElement -> {
                    Table table = new Table();
                    table.setName(((DefaultElement) tableElement).attributeValue("name"));
                    table.setDescription(((DefaultElement) tableElement).attributeValue("description"));
                    table.setId(((DefaultElement) tableElement).elementTextTrim("id"));
                    table.setSql(((DefaultElement) tableElement).elementTextTrim("sql").trim());
                    table.setCited(((DefaultElement) tableElement).elementTextTrim("cited"));
                    table.setUnique(((DefaultElement) tableElement).elementTextTrim("unique"));
                    table.setDownload(((DefaultElement) tableElement).elementTextTrim("download"));
                    table.setBucket(((DefaultElement) tableElement).attributeValue("bucket"));
                    List<Reference> references = new ArrayList<>();
                    List<Lang> langs = new ArrayList<>();
                    List<Type> types = new ArrayList<>();
                    ((DefaultElement) tableElement).elements().forEach(tableContent -> {
                        String field = ((DefaultElement) tableContent).elementTextTrim("field");
                        Type type = new Type();
                        type.setField(field);
                        type.setType(((DefaultElement) tableContent).elementTextTrim("type"));
                        if (type.getType() != null) {
                            types.add(type);
                        }
                        Lang lang = new Lang();
                        lang.setField(field);
                        lang.setPkName(((DefaultElement) tableContent).elementTextTrim("pkName"));
                        if (lang.getPkName() != null) {
                            langs.add(lang);
                        }
                        Reference reference = new Reference();
                        reference.setField(field);
                        reference.setColumnName(((DefaultElement) tableContent).elementTextTrim("columnName"));
                        reference.setSheetName(((DefaultElement) tableContent).elementTextTrim("sheetName"));
                        reference.setTableName(((DefaultElement) tableContent).elementTextTrim("tableName"));
                        if (reference.getColumnName() != null) {
                            references.add(reference);
                        }
                    });
                    table.setTypes(types);
                    table.setLangs(langs);
                    table.setReferences(references);
                    tables.add(table);
                });
                sheet.setTableList(tables);
                sheets.add(sheet);
            });
            excel.setSheetList(sheets);
            excels.add(excel);
        });
        service.setExcelList(excels);
        return service;
    }

    /**
     * 解析xml映射文件封装成Mapping
     *
     * @param file
     * @return Service
     * @throws DocumentException
     */
    private static void createMapping(InputStream file) throws DocumentException {
//        LOGGER.info("开始解析文件：{}", file.getName());
        List<Mapping> mappingList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        Element rootElement = document.getRootElement();
        rootElement.elements().forEach(element -> {
            if (StringUtils.equals("schema-merge", ((DefaultElement) element).getName())) {
                ((DefaultElement) element).elements().forEach(configItem -> {
                    Config config = new Config();
                    config.setName(((DefaultElement) configItem).getName());
                    config.setMerge(((DefaultElement) configItem).attributeValue("merge"));
                    config.setTargetSchema(((DefaultElement) configItem).attributeValue("target-schema"));
                    SCHEMA_MERGE.put(config.getName(), config);
                });
            }
            if (StringUtils.equals("exclusion", ((DefaultElement) element).getName())) {
                UPDATE_EXCLUSION = StringUtils.trim(((DefaultElement) element).getText());
            }
            if (StringUtils.equals("value-replace", ((DefaultElement) element).getName())) {
                ENABLE_REPLACE = Boolean.parseBoolean(((DefaultElement) element).attributeValue("flag"));
                ((DefaultElement) element).elements().forEach(replaceItem -> {
                    String key = (((DefaultElement) replaceItem).attributeValue("key"));
                    String value = (((DefaultElement) replaceItem).attributeValue("value"));
                    if (StringUtils.isNotBlank(key)) {
                        DATA_MAP.put(key, value);
                    }
                });
            }
            Mapping mapping = new Mapping();
            mapping.setName(((DefaultElement) element).attributeValue("name"));
            mapping.setFilename(((DefaultElement) element).attributeValue("filename"));
            mapping.setSchema(((DefaultElement) element).attributeValue("schema"));
            mapping.setUsername(((DefaultElement) element).attributeValue("username"));
            mapping.setPassword(((DefaultElement) element).attributeValue("password"));
            mapping.setDescription(((DefaultElement) element).attributeValue("description"));
            mappingList.add(mapping);
            MAPPING_MAP.put(mapping.getName(), mapping);
        });
        MAPPING_LIST.addAll(mappingList);
    }

    public static void upgradePomParentVersion(String sourceVersion, String targetVersion) throws IOException, DocumentException {
        for (Map.Entry<String, String> map : SERVICE_PATH.entrySet()) {
            File file = new File(map.getValue() + "/pom.xml");
            if (file.exists()) {
                SAXReader reader = new SAXReader();
                InputStream inputStream = new FileInputStream(file);
                Document pomDocument = reader.read(inputStream);
                // 升级pomDocument中版本号
                boolean writerFlag = updatePomVersion(pomDocument, sourceVersion, targetVersion);
                // 有改动-更新文件
                if (writerFlag) {
                    XMLWriter writer = new XMLWriter(new FileWriter(file));
                    //写入数据
                    writer.write(pomDocument);
                    writer.close();
                }
                LOGGER.info(map.getKey() + "升级版本号完成");
            }
        }

    }

    private static boolean updatePomVersion(Document pomDocument, String sourceVersion, String targetVersion) {
        boolean writerFlag = false, parentFlag = false, dependencyFlag = false;
        Element pomRootElement = pomDocument.getRootElement();
        for (Object element: pomRootElement.elements()) {
            // 修改 hzero-parent
            if ("parent".equals(((DefaultElement) element).getName())) {
                for (Object element1: ((DefaultElement) element).elements()) {
                    if ("artifactId".equals(((DefaultElement) element1).getName())) {
                        if ("hzero-parent".equals(((DefaultElement) element1).getText())) {
                            parentFlag = true;
                        }
                    }
                    if ("version".equals(((DefaultElement) element1).getName()) && parentFlag && ((DefaultElement) element1).getText().contains(sourceVersion)) {
                        String version = ((DefaultElement) element1).getText().replace(sourceVersion, targetVersion);
                        ((DefaultElement) element1).setText(version);
                        writerFlag = true;
                    }
                }
            }
            // 修改 hzero dependency
            if ("dependencies".equals(((DefaultElement) element).getName())) {
                for (Object element1: ((DefaultElement) element).elements()) {
                    for (Object element2: ((DefaultElement) element1).elements()) {
                        if ("groupId".equals(((DefaultElement) element2).getName())) {
                            if (((DefaultElement) element2).getText().contains("org.hzero")) {
                                dependencyFlag = true;
                            }
                        }
                        if ("version".equals(((DefaultElement) element2).getName()) && dependencyFlag && ((DefaultElement) element2).getText().contains(sourceVersion)) {
                            String version = ((DefaultElement) element2).getText().replace(sourceVersion, targetVersion);
                            ((DefaultElement) element2).setText(version);
                            writerFlag = true;
                        }
                    }
                }
            }
        }
        return writerFlag;
    }

    public static void fileUpdateHandler(String fileUpdatePath) {
        ClassPathResource pathResource = new ClassPathResource(fileUpdatePath + "/file-update.xml");
        if (pathResource.exists()) {
            InputStream inputStream;
            try {
                inputStream = pathResource.getInputStream();
                updateFile(inputStream, fileUpdatePath);
            } catch (IOException | DocumentException e) {
                LOGGER.error("解析file-update.xml文件出错 >>>>> {}", e.getMessage());
            }
        }
    }

    private static void updateFile(InputStream inputStream, String versionPath) throws DocumentException {
        if (inputStream != null) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element rootElement = document.getRootElement();
            for (Object element : rootElement.elements()) {
                String serviceName = ((DefaultElement) element).attributeValue("name");
                String servicePath = SERVICE_PATH.get(serviceName);
                if (servicePath != null) {
                    for (Object fileElement : ((DefaultElement) element).elements()) {
                        String filePath = ((DefaultElement) fileElement).attributeValue("file-path");
                        String replacePath = ((DefaultElement) fileElement).attributeValue("replace-path");
                        if (filePath != null) {
                            String fileFullPath = servicePath + "/" + filePath;
                            if (replacePath == null) {
                                // 根据配置文件更新文件
                                updateFileByXml(fileElement, fileFullPath);
                            } else {
                                // 根据配置文件替换文件
                                String localFilePath = Constants.RESOURCES_PREFIX + versionPath + "/" + serviceName + "/" + replacePath;
                                replaceFileByXml(localFilePath, fileFullPath);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void replaceFileByXml(String localFilePath, String fileFullPath) {
        FileCodeUtils.fileReplace(localFilePath, fileFullPath);
        LOGGER.info("替换文件成功： 文件来源：{} , 被替换文件{}", localFilePath, fileFullPath);
    }

    private static void updateFileByXml(Object fileElement, String fileFullPath) {
        List<String> addStrList = new ArrayList<>(1);
        List<String> removeStrList = new ArrayList<>(1);
        Map<String, String> replaceStrMap = new HashMap<>(1);
        for (Object contextElement : ((DefaultElement) fileElement).elements()) {
            if ("file-add".equals(((DefaultElement)contextElement).getName())) {
                addStrList.add (spaceHandler(((DefaultElement)contextElement).getStringValue()));
            } else if ("file-remove".equals(((DefaultElement)contextElement).getName())) {
                removeStrList.addAll(splitStr(((DefaultElement)contextElement).getStringValue()));
            } else if ("file-update".equals(((DefaultElement)contextElement).getName())) {
                replaceStrMap.put(((DefaultElement)contextElement).attributeValue("source"), ((DefaultElement)contextElement).attributeValue("target"));
            }
        }
        try {
            if (addStrList.size() > 0 || removeStrList.size() > 0 || replaceStrMap.size() > 0) {
                writeLog(fileFullPath, addStrList, removeStrList, replaceStrMap);
                FileCodeUtils.fileUpdateByStr(fileFullPath, addStrList, removeStrList, replaceStrMap);
                LOGGER.info("更新文件完成");
            }
        } catch (IOException e) {
            LOGGER.error("根据file-update.xml修改文件失败 >>>>> {}", e.getMessage());
        }
    }

    private static void writeLog(String fileFullPath, List<String> addStrList, List<String> removeStrList, Map<String, String> replaceStrMap) {
        LOGGER.info("更新文件：" + fileFullPath);
        LOGGER.info("新增内容：" + String.join("\n", addStrList));
        LOGGER.info("删除内容：" + String.join("\n", removeStrList));
        LOGGER.info("更新内容：");
        for (Map.Entry<String, String> map : replaceStrMap.entrySet()) {
            LOGGER.info("更新前： {}", map.getKey());
            LOGGER.info("更新后： {}", map.getValue());
            LOGGER.info("\n");
        }
    }

    /**
     * 空格处理并按换行符分隔
     * @param textTrim 内容
     * @return List<String>
     */
    private static List<String> splitStr(String textTrim) {
        return Arrays.asList(textTrim.replaceAll(" {2}", "").split("\n"));
    }

    /**
     * 空格处理
     * @param stringValue 内容
     * @return Sting
     */
    private static String spaceHandler(String stringValue) {
        return stringValue.replace("                ", "");
    }

    /**
     * 解析xml得到修复数据
     * @param dataUpdatePath 路径
     * @return map
     */
    public static Map<String, List<DataUpdateDTO>> getSchemaData(String dataUpdatePath) {
        ClassPathResource xmlFile = new ClassPathResource(dataUpdatePath);
        Map<String, List<DataUpdateDTO>> map = new HashMap<>();
        try {
            if (xmlFile.exists()) {
                InputStream inputStream = xmlFile.getInputStream();
                SAXReader reader = new SAXReader();
                Document document = reader.read(inputStream);
                Element rootElement = document.getRootElement();
                for (Object element1: rootElement.elements()) {
                    String schemaName = ((DefaultElement) element1).attributeValue("schema");
                    List<DataUpdateDTO> dtoList = new ArrayList<>();
                    for (Object element2: ((DefaultElement) element1).elements()) {
                        DataUpdateDTO dataUpdateDTO = new DataUpdateDTO();
                        dataUpdateDTO.setType(((DefaultElement) element2).attributeValue("type"));
                        dataUpdateDTO.setDriverClass(((DefaultElement) element2).attributeValue("driver-class"));
                        dataUpdateDTO.setOrder(((DefaultElement) element2).attributeValue("order"));
                        dataUpdateDTO.setContent(((DefaultElement) element2).getStringValue());
                        dtoList.add(dataUpdateDTO);
                    }
                    map.put(schemaName, dtoList);
                }
            }
        } catch (DocumentException | IOException e) {
            LOGGER.error("解析XML文件出错 >>>>> {}", e.getMessage());
        }
        return map;
    }
}
