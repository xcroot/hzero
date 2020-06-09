package org.hzero.generator.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.export.constants.Constants;
import org.hzero.generator.export.helper.LiquibaseEngine;
import org.hzero.generator.export.helper.entity.Data;
import org.hzero.generator.export.helper.entity.DataGroup;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.hzero.generator.mapper.MenuPermissionExportMapper;
import org.hzero.generator.service.MenuPermissionExportService;
import org.hzero.generator.util.ScriptUtils;
import org.hzero.generator.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Description 菜单权限导出实现
 * @Date 2020-02-17 17:21
 * @Author wanshun.zhang@hand-china.com
 */
@Service
public class MenuPermissionExportServiceImpl implements MenuPermissionExportService {

    @Autowired
    ScriptUtils scriptUtils;
    @Autowired
    MenuPermissionExportMapper menuPermissionExportMapper;
    private Logger logger = LoggerFactory.getLogger(MenuPermissionExportServiceImpl.class);
    private List<Long> ids = new ArrayList<>();

    @Override
    public void exportMenuPermission(List<String> routes, String version) {
        // 清空
        ids.clear();
        AtomicReference<List<DataGroup>> dataGroups = new AtomicReference<>();
        AtomicReference<String> serviceName = new AtomicReference<>("");
        AtomicReference<String> fileName = new AtomicReference<>("");
        AtomicReference<String> schema = new AtomicReference<>("");
        XmlUtils.SERVICE_LIST.forEach(service -> {
            if (StringUtils.equals("hzero-platform", service.getName())) {
                serviceName.set(service.getName());
                service.getExcelList().forEach(excel -> {
                    if (StringUtils.equals("hzero-iam-menu", excel.getName())) {
                        dataGroups.set(scriptUtils.create(service.getName(), excel));
                        fileName.set(excel.getName());
                        schema.set(excel.getSchema());
                    }
                });
            }
        });
        if (CollectionUtils.isEmpty(dataGroups.get())) {
            logger.warn("初始化内容为空！");
        }

        // 选择数据库
        menuPermissionExportMapper.changeSchema(schema.get());
        // 根据route查parent_id和id，将id添加到列表中
        for (String route : routes) {
            List<Map<String, Long>> maps = selectIds(route, version);
            for (Map<String, Long> map : maps) {
                Long parentId = map.get("parent_id");
                Long id = map.get("id");
                if (parentId != null && id != null) {
                    ids.add(parentId);
                    ids.add(id);
                    // 根据parent_id递归查父级
                    selectParentId(parentId);
                    // 根据id查下级
                    selectChild(parentId);
                }
            }
        }
        // 过滤重复id并排序
        final List<Long> allId = ids.stream().distinct().sorted().collect(Collectors.toList());

        String finalVersion = StringUtils.equals("op", version) ? "菜单SAAS版" : "菜单OP版";;
        List<DataGroup> dataGroupList = dataGroups.get().stream().filter(dataGroup -> !StringUtils.equals("角色权限", dataGroup.getSheetName()) && !StringUtils.equals(finalVersion, dataGroup.getSheetName())).collect(Collectors.toList());
        // 修改where条件
        for (DataGroup dataGroup : dataGroupList) {
            List<Data> dataList = dataGroup.getDataList();
            for (Data data : dataList) {
                if (StringUtils.equals("iam_menu", data.getTableName()) && allId.size() > 0) {
                    data.setWhere("h_tenant_id = 0 and h_enabled_flag = 1 and id in (" + StringUtils.join(allId, ",") + ") ORDER BY h_level_path");
                } else if (StringUtils.equals("iam_menu_permission", data.getTableName()) && allId.size() > 0) {
                    data.setWhere("menu_id in (" + StringUtils.join(allId, ",") + ")");
                } else if (allId.size() == 0) {
                    data.setWhere("1=0");
                }
            }
        }
        LiquibaseEngine liquibaseEngine = LiquibaseEngine.createEngine(Constants.BASE_OUTPUT_PATH + serviceName.get() + "/" + schema.get() + "/" + fileName.get() + ".xlsx", LiquibaseEngineMode.OVERRIDE);
        liquibaseEngine.setDataGroupList(dataGroupList);
        liquibaseEngine.generate();
    }

    /**
     * 递归找下一级
     *
     * @param id id
     */
    private void selectChild(Long id) {
        List<Long> childIds = menuPermissionExportMapper.selectChildId(id);
        if (childIds.size() > 0) {
            ids.addAll(childIds);
            for (int i = 0; i < childIds.size(); i++) {
                selectChild(childIds.get(i));
            }
        }
    }

    /**
     * 递归查父级
     *
     * @param parentId 父级id
     */
    private void selectParentId(Long parentId) {
        if (parentId.intValue() != 0) {
            Long id = menuPermissionExportMapper.selectParendId(parentId);
            if (id != null) {
                ids.add(id);
                // 递归查父级
                selectParentId(id);
            }
        }
    }

    /**
     * 根据路由查parent_id和id
     *
     * @param uiRoute 路由
     * @return parent_id和id
     */
    private List<Map<String, Long>> selectIds(String uiRoute, String version) {
        return menuPermissionExportMapper.selectIdByRoute(uiRoute, version);
    }
}
