package org.hzero.generator.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.hzero.generator.dto.DataUpdateDTO;
import org.hzero.generator.execute.SqlExecutor;
import org.hzero.generator.service.ServiceUpgradeService;
import org.hzero.generator.util.DBConfigUtils;
import org.hzero.generator.util.FileCodeUtils;
import org.hzero.generator.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liguo.wang
 */
@Service
public class ServiceUpgradeServiceImpl implements ServiceUpgradeService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUpgradeServiceImpl.class);

    private static final String PATH = "src/main/resources/upgrade/";
    private static final String UPGRADE = "upgrade";

    @Autowired
    DBConfigUtils dbConfigUtils;

    @Override
    public List<String> listServiceUpgrade() {
        return FileCodeUtils.getDirectory(PATH);
    }

    @Override
    public void serviceUpgrade(String version) {
        // 升级pom中parent版本号
        upgradePomParentVersion(version);
        // 文件比对升级
        fileComparisonUpgrade(version);
    }

    @Override
    public void dataUpdate(String version) {
        String dataUpdatePath = UPGRADE + "/" + version + "/" + "data-update.xml";
        // 得到修复数据
        Map<String , List<DataUpdateDTO>> map = XmlUtils.getSchemaData(dataUpdatePath);

        // 执行修复数据
        execute(map);
    }

    private void execute(Map<String, List<DataUpdateDTO>> map) {
        for (Map.Entry<String, List<DataUpdateDTO>> entry: map.entrySet()) {
            // 分schema执行
            doExecute(entry.getKey(), entry.getValue());
        }
    }

    private void doExecute(String schema, List<DataUpdateDTO> list) {
        String env = DynamicDataSourceContextHolder.getDataSourceLookupKey();
        Map<String, String> dataSource = dbConfigUtils.getMapByEnv(StringUtils.defaultIfBlank(env, "gen"));

        List<DataUpdateDTO> sqlList = list.stream().filter(dataUpdateDTO -> "sql".equals(dataUpdateDTO.getType())).collect(Collectors.toList());

        Map<String, List<DataUpdateDTO>> map = sqlList.stream().collect(Collectors.groupingBy(DataUpdateDTO::getDriverClass));

        String dbUrl = dataSource.get("url");
        String username = dataSource.get("username");
        String password = dataSource.get("password");
        for (Map.Entry<String, List<DataUpdateDTO>> entry : map.entrySet()) {
            dbUrl = buildDbUrl(dbUrl, entry.getKey(), schema);
            entry.getValue().sort(Comparator.comparing(DataUpdateDTO::getOrder));
            List<String> sqls = entry.getValue().stream().map(DataUpdateDTO::getContent).collect(Collectors.toList());
            SqlExecutor sqlExecutor = new SqlExecutor(dbUrl, username, password ,entry.getKey());
            try {
                sqlExecutor.execute(sqls);
            } catch (SQLException e) {
                logger.error("数据修复脚本执行失败：{}" , e.getMessage());
            }
        }

    }

    private String buildDbUrl(String dbUrl, String type, String schema) {
        if (StringUtils.equals("mysql", type)) {
            dbUrl = StringUtils.replace(dbUrl, "?", "/" + schema + "?");
        } else if (StringUtils.equals("sqlserver", type)) {
            dbUrl += "DatabaseName=" + schema;
        } else if (StringUtils.equals("oracle", type)) {
            // todo
        } else if (StringUtils.equals("postgresql", type)) {
            dbUrl = StringUtils.replace(dbUrl, "/postgres", "/" + schema);
        }
        return dbUrl;
    }

    private void fileComparisonUpgrade(String version) {
        String versionPath = PATH + version;
        // 配置文件替换更新
        FileCodeUtils.fileReplaceUpdate(versionPath);
        // 配置文件增删改 \ 其他文件替换
        String fileUpdatePath = UPGRADE + "/" + version;
        XmlUtils.fileUpdateHandler(fileUpdatePath);
    }

    private void upgradePomParentVersion(String version) {
        // 得到原始版本
        String sourceVersion = version.split("-")[0];
        // 得到目标版本
        String targetVersion = getTargetVersion(version);
        if (sourceVersion != null && targetVersion != null) {
            try {
                XmlUtils.upgradePomParentVersion(sourceVersion, targetVersion);
            } catch (IOException | DocumentException e) {
                logger.error("升级版本号失败  >>>>> {}", e.getMessage());
            }
        }
    }

    private String getTargetVersion(String version) {
        if (version.contains("-")) {
            return version.split("-")[1];
        }
        return null;
    }
}
