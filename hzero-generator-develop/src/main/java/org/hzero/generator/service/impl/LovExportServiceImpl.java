package org.hzero.generator.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.export.constants.Constants;
import org.hzero.generator.export.helper.LiquibaseEngine;
import org.hzero.generator.export.helper.entity.Data;
import org.hzero.generator.export.helper.entity.DataGroup;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.service.LovExportService;
import org.hzero.generator.util.ScriptUtils;
import org.hzero.generator.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @Description LOV导出实现
 * @Date 2020-02-17 17:20
 * @Author wanshun.zhang@hand-china.com
 */
@Service
public class LovExportServiceImpl implements LovExportService {

    @Autowired
    ScriptUtils scriptUtils;
    private Logger logger = LoggerFactory.getLogger(LovExportServiceImpl.class);

    @Override
    public void exportLov(List<UiComponent> params) {
        params = params.stream().filter(item -> !StringUtils.isEmpty(item.getCompCode())).collect(Collectors.toList());
        AtomicReference<List<DataGroup>> dataGroups = new AtomicReference<>();
        AtomicReference<String> serviceName = new AtomicReference<>("");
        AtomicReference<String> fileName = new AtomicReference<>("");
        AtomicReference<String> schema = new AtomicReference<>("");
        XmlUtils.SERVICE_LIST.forEach(service -> {
            if (StringUtils.equals("hzero-platform", service.getName())) {
                serviceName.set(service.getName());
                service.getExcelList().forEach(excel -> {
                    if (StringUtils.equals("hzero-platform-lov", excel.getName())) {
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
        List<DataGroup> dataGroupList = dataGroups.get();
        List<String> codes = params.stream().map(UiComponent::getCompCode).collect(Collectors.toList());
        // 修改where条件
        for (DataGroup dataGroup : dataGroupList) {
            List<Data> dataList = dataGroup.getDataList();
            for (Data data : dataList) {
                data.setWhere("tenant_id = 0 and enabled_flag = 1");
                if (StringUtils.equals("hpfm_lov", data.getTableName()) && codes.size() > 0) {
                    data.setWhere("tenant_id = 0 and enabled_flag = 1 and lov_code in ('" + String.join("','", codes) + "')");
                } else if (codes.size() == 0) {
                    data.setWhere("1=0");
                }
            }
        }
        LiquibaseEngine liquibaseEngine = LiquibaseEngine.createEngine(Constants.BASE_OUTPUT_PATH + serviceName.get() + "/" + schema.get() + "/" + fileName.get() + ".xlsx", LiquibaseEngineMode.OVERRIDE);
        liquibaseEngine.setDataGroupList(dataGroupList);
        liquibaseEngine.generate();
    }
}
