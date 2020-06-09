package org.hzero.generator.service.impl;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.entity.Config;
import org.hzero.generator.entity.Mapping;
import org.hzero.generator.liquibase.LiquibaseExecutor;
import org.hzero.generator.service.ImportDataService;
import org.hzero.generator.service.InitDataInfoService;
import org.hzero.generator.util.DBConfigUtils;
import org.hzero.generator.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 导入数据实现
 * @Date 2019/12/16 15:04
 * @Author wanshun.zhang@hand-china.com
 */
@Service
public class ImportDataServiceImpl implements ImportDataService {
    public static final String ORACLE = "oracle";
    public static final String MYSQL = "mysql";
    public static final String SQLSERVER = "sqlserver";
    public static final String POSTGRESQL = "postgresql";
    @Autowired
    DBConfigUtils dbConfigUtils;
    @Autowired
    InitDataInfoService initDataInfoService;
    /**
     * 服务列表
     */
    private List<Mapping> mappingList = XmlUtils.MAPPING_LIST;
    private Map<String, Mapping> mappingMap = XmlUtils.MAPPING_MAP;
    private Map<String, Config> schemaMarge = XmlUtils.SCHEMA_MERGE;
    private LiquibaseExecutor liquibaseExecutor = new LiquibaseExecutor();
    private Logger LOGGER = LoggerFactory.getLogger(ImportDataServiceImpl.class);

    /**
     * 获取服务列表
     *
     * @param dir 文件路径
     * @param env 文件路径
     * @return 服务列表
     */
    @Override
    public List<Mapping> getDataServices(String dir, String env) {
        File file = new File(dir);
        List<Mapping> mappings = null;
        if (file.isDirectory()) {
            List<String> fileNames = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(f -> !f.isFile()).map(File::getName).collect(Collectors.toList());
            mappings = mappingList.stream().filter(mapping -> fileNames.contains(mapping.getName())).collect(Collectors.toList());
        }
        return getMappings(mappings, env);
    }

    /**
     * 获取服务列表
     *
     * @param dir 文件路径
     * @return 服务列表
     */
    @Override
    public List<Mapping> getGroovyServices(String dir, String env) {
        File file = new File(dir);
        List<Mapping> mappings = null;
        if (file.isDirectory()) {
            List<String> fileNames = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(f -> !f.isFile()).map(File::getName).collect(Collectors.toList());
            mappings = mappingList.stream().filter(mapping -> fileNames.contains(mapping.getFilename())).collect(Collectors.toList());
        }
        return getMappings(mappings, env);
    }

    private List<Mapping> getMappings(List<Mapping> mappings, String env) {
        final Map<String, String> dataSource = dbConfigUtils.getMapByEnv(env);
        String url = dataSource.get("url");
        Config config = getConfig(url);
        mappings.forEach(mapping -> {
            if (StringUtils.equals(config.getMerge(), "true")) {
                mapping.setSchema(StringUtils.defaultIfBlank(config.getTargetSchema(), mapping.getSchema()));
                if (StringUtils.equals(ORACLE, config.getName())) {
                    mapping.setSchema(StringUtils.defaultIfBlank(StringUtils.substringAfterLast(url, ":"), mapping.getSchema()));
                }
            }
        });
        return mappings;
    }

    /**
     * 导入数据和更新脚本
     *
     * @param services 服务列表
     * @param dir      文件路径
     * @param env      环境
     */
    @Override
    public void importData(List<String> services, String dir, String env) {
        Map<String, String> mappingMap = new HashMap<>();
        mappingList.forEach(m -> mappingMap.put(m.getFilename(), m.getSchema()));
        final Map<String, String> dataSource = dbConfigUtils.getMapByEnv(env);
        String url = dataSource.get("url");
        Config config = getConfig(url);
        for (String service : services) {
            String rootPath = dir + "/" + service;
            File file = new File(rootPath);
            if (file.isDirectory()) {
                List<String> filenameList = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(f -> !f.isFile()).map(File::getName).collect(Collectors.toList());
                for (String filename : filenameList) {
                    // 处理数据库
                    String schema = mappingMap.get(filename);
                    if (StringUtils.equals(config.getMerge(), "true")) {
                        if (StringUtils.equals(ORACLE, config.getName())) {
                            schema = (StringUtils.defaultIfBlank(StringUtils.substringAfterLast(url, ":"), mappingMap.get(filename)));
                        } else {
                            schema = StringUtils.defaultIfBlank(config.getTargetSchema(), mappingMap.get(filename));
                        }
                    }
                    try {
                        executor(rootPath + "/" + filename, env, service, schema);
                    } catch (Exception e) {
                        LOGGER.error("初始化数据失败 {}", e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void updateGroovy(List<String> services, String dir, String env) {
        Map<String, Mapping> mappingMap = new HashMap<>();
        mappingList.forEach(m -> mappingMap.put(m.getName(), m));
        final Map<String, String> dataSource = dbConfigUtils.getMapByEnv(env);
        String url = dataSource.get("url");
        Config config = getConfig(url);
        for (String service : services) {
            // 处理数据库
            String schema = mappingMap.get(service).getSchema();
            if (StringUtils.equals(config.getMerge(), "true")) {
                if (StringUtils.equals(ORACLE, config.getName())) {
                    schema = (StringUtils.defaultIfBlank(StringUtils.substringAfterLast(url, ":"), mappingMap.get(service).getSchema()));
                } else {
                    schema = StringUtils.defaultIfBlank(config.getTargetSchema(), mappingMap.get(service).getSchema());
                }
            }
            try {
                switch (env) {
                    case InitDataInfoService.ENV_DEV:
                        initDataInfoService.createDevDatabase(config.getName(), StringUtils.defaultIfBlank(schema, service));
                        break;
                    case InitDataInfoService.ENV_TST:
                        initDataInfoService.createTstDatabase(config.getName(), StringUtils.defaultIfBlank(schema, service));
                        break;
                    case InitDataInfoService.ENV_UAT:
                        initDataInfoService.createUatDatabase(config.getName(), StringUtils.defaultIfBlank(schema, service));
                        break;
                    case InitDataInfoService.ENV_PRD:
                        initDataInfoService.createPrdDatabase(config.getName(), StringUtils.defaultIfBlank(schema, service));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LOGGER.error("Can't create database {};", service);
                LOGGER.info(e.getMessage());
            }
            String fileDir = dir + "/" + mappingMap.get(service).getFilename();
            executor(fileDir, env, service, schema);
        }
    }

    private void executor(String dir, String env, String service, String schema) {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> start : service={}, schema={} <<<<<<<<<<<<<<<<<<<<<<<<<<<<<", service, schema);
        final Map<String, String> dataSource = dbConfigUtils.getMapByEnv(env);
        String url = dataSource.get("url");
        Config config = getConfig(url);
        Mapping mapping = mappingMap.get(schema);
        String dbUrl = dataSource.get("url");
        String username = dataSource.get("username");
        String password = dataSource.get("password");
        if (StringUtils.equals(config.getName(), MYSQL)) {
            dbUrl = StringUtils.replace(url, "?", "/" + schema + "?");
        } else if (StringUtils.equals(config.getName(), SQLSERVER)) {
            dbUrl += "DatabaseName=" + schema;
        } else if (StringUtils.equals(config.getName(), ORACLE) && StringUtils.equals(config.getMerge(), "false")) {
            username = StringUtils.defaultIfBlank(mapping.getUsername(), username);
            password = StringUtils.defaultIfBlank(mapping.getPassword(), password);
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> username : {} | password : {}", username, password);
        } else if (StringUtils.equals(POSTGRESQL, config.getName())) {
            dbUrl = StringUtils.replace(url, "/postgres", "/" + schema);
        }
        liquibaseExecutor.setDsUrl(dbUrl);
        liquibaseExecutor.setDsUserName(username);
        liquibaseExecutor.setDsPassword(password);
        liquibaseExecutor.setDefaultDir(dir);
        liquibaseExecutor.execute();
    }

    /**
     * 获取config
     *
     * @param url 数据库url
     * @return config
     */
    private Config getConfig(String url) {
        if (StringUtils.contains(url, MYSQL)) {
            return schemaMarge.get(MYSQL);
        } else if (StringUtils.contains(url, SQLSERVER)) {
            return schemaMarge.get(SQLSERVER);
        } else if (StringUtils.contains(url, ORACLE)) {
            return schemaMarge.get(ORACLE);
        } else if (StringUtils.contains(url, POSTGRESQL)) {
        return schemaMarge.get(POSTGRESQL);
        }
        return null;
    }
}
