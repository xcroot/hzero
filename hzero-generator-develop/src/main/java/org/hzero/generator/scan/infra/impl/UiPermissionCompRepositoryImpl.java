package org.hzero.generator.scan.infra.impl;

import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.scan.domain.UiApi;
import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.UiPermissionApi;
import org.hzero.generator.scan.domain.repository.UiPermissionCompRepository;
import org.hzero.generator.scan.infra.constant.Constants;
import org.hzero.generator.util.SwitchDatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源库实现
 *
 * @author allen.liu
 * @date 2019/7/30
 */
@Repository
public class UiPermissionCompRepositoryImpl implements UiPermissionCompRepository {
    @Value("${scan.tool.temporary:hzero_generator}")
    private String generator;

    @Autowired
    private SwitchDatabaseUtils databaseUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(UiPermissionCompRepositoryImpl.class);
    private static final String CLEAR_BUTTON_SQL = "delete from hgen_ui_component where ui_route = ? and comp_type = ?";
    private static final String CLEAR_LOV_SQL = "delete from hgen_ui_component where ui_route = ? and (comp_type = 'lov' or comp_type = 'lovView')";
    private static final String INSERT_COMP_SQL = "insert into hgen_ui_component(ui_route, comp_code, description, comp_type) values (?, ?, ?, ?)";
    private static final String CLEAR_API_SQL = "delete from hgen_ui_api where " +
            "ui_route = ? and ui_comp_id = ? and path = ? and method = ? and api_level = ? and service_name = ?";
    private static final String CLEAR_PUBLIC_API_SQL = "delete from hgen_ui_api where " +
            "ui_route = ? and ui_comp_id is null and path = ? and method = ? and api_level = ? and service_name = ?";
    private static final String SELECT_COMP_ID_BY_CODE = "SELECT ui_comp_id FROM hgen_ui_component where comp_code = ?";
    private static final String INSERT_APIL_SQL = "insert into hgen_ui_api(ui_comp_id,ui_route, path, method, api_level,service_name) values (?, ?, ?, ?, ?,?)";

    @Override
    public void rebuildComponent(DataSource dataSource, List<UiComponent> uiComponents, String compType) {
        databaseUtils.switchDatabase(generator);
        // 重建数据库UI权限组件信息
        LOGGER.info("===================================  重建数据库权限按钮 ==========================================");
        // 按照路由分组
        Map<String, List<UiComponent>> uiComponentMap = uiComponents.stream().collect(Collectors.groupingBy(UiComponent::getUiRoute));
        LOGGER.info("=============== 发现页面[{}] =================\n{}",
                uiComponentMap.keySet().size(), Arrays.toString(uiComponentMap.keySet().toArray()));
        // 构造数据库操作
        Connection connection = DataSourceUtils.getConnection(dataSource);

        PreparedStatement insertCompPreparedStatement = null;
        PreparedStatement clearPreparedStatement = null;
        PreparedStatement clearLovPreparedStatement = null;
        try {
            insertCompPreparedStatement = connection.prepareStatement(INSERT_COMP_SQL);
            clearPreparedStatement = connection.prepareStatement(CLEAR_BUTTON_SQL);
            clearLovPreparedStatement = connection.prepareStatement(CLEAR_LOV_SQL);
            for (Map.Entry<String, List<UiComponent>> entry : uiComponentMap.entrySet()) {
                LOGGER.info("=============== 处理页面[{}] ================", entry.getKey());
                // 首先, 清除历史数据
                if (Constants.CompType.LOV.equals(compType)) {
                    clearLovPreparedStatement.setObject(1, entry.getKey());
                    clearLovPreparedStatement.executeUpdate();
                } else {
                    clearPreparedStatement.setObject(1, entry.getKey());
                    clearPreparedStatement.setObject(2, compType);
                    clearPreparedStatement.executeUpdate();
                }
                // 按照代码分组, 如果重复则取第一个发现的元素的名称及描述, 类型字段为逗号拼接的结果
                Map<String, List<UiComponent>> rawUiComponentMap = entry.getValue().stream().collect(Collectors.groupingBy(UiComponent::getCompCode));
                List<UiComponent> uiComponentList = new ArrayList<>();
                rawUiComponentMap.forEach((compCode, groupUiPermissionComps) -> {
                    String uiRoute = groupUiPermissionComps.get(0).getUiRoute();
                    String description = groupUiPermissionComps.get(0).getDescription();
                    uiComponentList.add(new UiComponent(uiRoute, compCode, description, compType));
                });
                // 写入新的数据
                LOGGER.info("================ 插入数据[{}] ================", uiComponentList.size());
                for (UiComponent uiComponent : uiComponentList) {
                    try {
                        insertCompPreparedStatement.setString(1, uiComponent.getUiRoute());
                        insertCompPreparedStatement.setString(2, uiComponent.getCompCode());
                        insertCompPreparedStatement.setString(3, uiComponent.getDescription());
                        insertCompPreparedStatement.setString(4, uiComponent.getCompType());
                        insertCompPreparedStatement.executeUpdate();
                    } catch (Exception e) {
                        LOGGER.error("error insert into hgen_ui_comp, data: {}, e: {}", uiComponent.toString(), e.getMessage());
                    }
                }
            }
        } catch (SQLException ex) {
            // Close early
            JdbcUtils.closeStatement(clearPreparedStatement);
            JdbcUtils.closeStatement(insertCompPreparedStatement);
            DataSourceUtils.releaseConnection(connection, dataSource);
            LOGGER.error("rebuild ui permission components failed: ", ex);
            throw new RuntimeException(ex);
        } finally {
            JdbcUtils.closeStatement(clearPreparedStatement);
            JdbcUtils.closeStatement(insertCompPreparedStatement);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public void rebuildApi(DataSource dataSource, List<UiPermissionApi> uiPermissionApis) {
        databaseUtils.switchDatabase(generator);
        if (CollectionUtils.isEmpty(uiPermissionApis)) {
            return;
        }
        // 将有按钮权限的api与无按钮权限的api分开
        List<UiPermissionApi> permissionApis = uiPermissionApis.stream().filter(item -> !StringUtils.isEmpty(item.getPermissionCode())).collect(Collectors.toList());
        List<UiPermissionApi> publicApis = uiPermissionApis.stream().filter(item -> StringUtils.isEmpty(item.getPermissionCode())).collect(Collectors.toList());

        //按照路由分组
        Map<String, List<UiPermissionApi>> permissionApiMap = permissionApis.stream().collect(Collectors.groupingBy(UiPermissionApi::getPageRoute));
        // 构造数据库操作
        Connection connection = DataSourceUtils.getConnection(dataSource);

        PreparedStatement clearApiPreparedStatement = null;
        PreparedStatement insertApiPreparedStament = null;
        PreparedStatement selectCompIdByCodeStatement = null;
        PreparedStatement clearPublicApiStatement = null;
        try {
            clearApiPreparedStatement = connection.prepareStatement(CLEAR_API_SQL);
            insertApiPreparedStament = connection.prepareStatement(INSERT_APIL_SQL);
            selectCompIdByCodeStatement = connection.prepareStatement(SELECT_COMP_ID_BY_CODE);
            clearPublicApiStatement = connection.prepareStatement(CLEAR_PUBLIC_API_SQL);
            for (Map.Entry<String, List<UiPermissionApi>> entry : permissionApiMap.entrySet()) {
                List<UiPermissionApi> apis = entry.getValue();
                for (UiPermissionApi api : apis) {
                    selectCompIdByCodeStatement.setObject(1, api.getPermissionCode());
                    Long uiCompId = null;
                    try (ResultSet resultSet = selectCompIdByCodeStatement.executeQuery()) {
                        resultSet.next();
                        uiCompId = resultSet.getLong(1);
                    } catch (Exception e) {
                        continue;
                    }
                    for (UiApi uiApi : api.getApis()) {
                        // 清除数据
                        clearApiPreparedStatement.setObject(1, uiApi.getUiRoute());
                        clearApiPreparedStatement.setObject(2, uiCompId);
                        clearApiPreparedStatement.setObject(3, uiApi.getPath());
                        clearApiPreparedStatement.setObject(4, uiApi.getMethod());
                        clearApiPreparedStatement.setObject(5, uiApi.getApiLevel());
                        clearApiPreparedStatement.setObject(6, uiApi.getServiceName());
                        clearApiPreparedStatement.executeUpdate();
                        insertApiPreparedStament.setObject(1, uiCompId);
                        insertApiPreparedStament.setObject(2, uiApi.getUiRoute());
                        insertApiPreparedStament.setObject(3, uiApi.getPath());
                        insertApiPreparedStament.setObject(4, uiApi.getMethod());
                        insertApiPreparedStament.setObject(5, uiApi.getApiLevel());
                        insertApiPreparedStament.setObject(6, uiApi.getServiceName());
                        try {
                            LOGGER.info("insert into ui_api, api: {}", uiApi);
                            insertApiPreparedStament.executeUpdate();
                        } catch (Exception e) {
                            LOGGER.error("error insert into hgen_ui_api, data: {}", uiApi.toString());
                        }
                    }
                }

            }
            int i = 0;
            for (UiPermissionApi apis : publicApis) {
                // 插入无权限按钮API
                for (UiApi uiApi : apis.getApis()) {
                    // 清除数据
                    clearPublicApiStatement.setObject(1, uiApi.getUiRoute());
                    clearPublicApiStatement.setObject(2, uiApi.getPath());
                    clearPublicApiStatement.setObject(3, uiApi.getMethod());
                    clearPublicApiStatement.setObject(4, uiApi.getApiLevel());
                    clearPublicApiStatement.setObject(5, uiApi.getServiceName());
                    clearPublicApiStatement.executeUpdate();
                    insertApiPreparedStament.setObject(1, null);
                    insertApiPreparedStament.setObject(2, uiApi.getUiRoute());
                    insertApiPreparedStament.setObject(3, uiApi.getPath());
                    insertApiPreparedStament.setObject(4, uiApi.getMethod());
                    insertApiPreparedStament.setObject(5, uiApi.getApiLevel());
                    insertApiPreparedStament.setObject(6, uiApi.getServiceName());
                    insertApiPreparedStament.addBatch();
                    i++;
                    LOGGER.info("prepared insert into ui_api, api: {}", uiApi);
                }
                if (i % 2000 == 0) {
                    try {
                        LOGGER.info("execute batch insert, size: {}", i);
                        insertApiPreparedStament.executeBatch();
                        insertApiPreparedStament.clearBatch();
                        i = 0;
                    } catch (Exception e) {
                        LOGGER.error("error insert into hgen_ui_api, e:{}", e.getMessage());
                    }
                }
            }
            try {
                LOGGER.info("execute batch insert, size: {}", i);
                insertApiPreparedStament.executeBatch();
                insertApiPreparedStament.clearBatch();
            } catch (Exception e) {
                LOGGER.error("error insert into hgen_ui_api, e:{}", e.getMessage());
            }
        } catch (SQLException ex) {
            // Close early
            JdbcUtils.closeStatement(insertApiPreparedStament);
            JdbcUtils.closeStatement(clearApiPreparedStatement);
            JdbcUtils.closeStatement(selectCompIdByCodeStatement);
            DataSourceUtils.releaseConnection(connection, dataSource);

            LOGGER.error("rebuild ui api failed: ", ex);
            throw new RuntimeException(ex);
        } finally {
            JdbcUtils.closeStatement(insertApiPreparedStament);
            JdbcUtils.closeStatement(clearApiPreparedStatement);
            JdbcUtils.closeStatement(selectCompIdByCodeStatement);
            DataSourceUtils.releaseConnection(connection, dataSource);
            LOGGER.info("all api insertions are complete");
        }
    }
}
