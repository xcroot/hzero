package org.hzero.generator.export.helper;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.export.helper.entity.DataSet;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.springframework.stereotype.Component;

/**
 * @Description 虚拟菜单导出
 * @Date 2020/2/5 15:56
 * @Author wanshun.zhang@hand-china.com
 */
@Component
public class VirtualMenuExport extends LiquibaseEngine {
    /**
     * 过滤后的数据
     */
    private List<DataSet> result = new ArrayList<>();
    private static Map<String, Object> ROOT_MENU = new HashMap<>();

    static {
        ROOT_MENU.put("*id", 0);
        ROOT_MENU.put("#code", "hzero");
        ROOT_MENU.put("name:zh_CN", "虚拟根目录");
        ROOT_MENU.put("name:en_US", "virtual root menu");
        ROOT_MENU.put("parent_id", -1);
        ROOT_MENU.put("h_level_path", "hzero");
        ROOT_MENU.put("type", "root");
        ROOT_MENU.put("is_default", 0);
        ROOT_MENU.put("h_custom_flag", 1);
        ROOT_MENU.put("#h_tenant_id", 0);
        ROOT_MENU.put("h_virtual_flag", 1);
        ROOT_MENU.put("h_enabled_flag", 1);
    }

    public static VirtualMenuExport createEngine(String filePath, LiquibaseEngineMode engineMode) {
        VirtualMenuExport fullExport = new VirtualMenuExport();
        fullExport.setFilePath(filePath);
        fullExport.setEngineMode(engineMode);
        fullExport.loadFile().loadExcel();
        return fullExport;
    }

    @Override
    public List<DataSet> dataFilter(List<DataSet> dataSets) {
        for (DataSet dataSet : dataSets){
            if (StringUtils.equals(dataSet.getTableName(),"iam_menu")){
                DataSet ds = new DataSet();
                ds.setData(dataSet.getData());
                ds.setTableName(dataSet.getTableName());
                ds.setSheetName(dataSet.getSheetName());
                Set<Map<String, Object>> set = new LinkedHashSet<>();
                if (!StringUtils.equals(dataSet.getSheetName(), "菜单SAAS版")){
                    ROOT_MENU.put("#fd_level", "organization");
                } else {
                    ROOT_MENU.put("#fd_level", "site");
                }
                set.add(ROOT_MENU);
                for (Map<String, Object> dataMap : dataSet.getDataSet()) {
                    dataMap.put("h_level_path", "hzero|" + dataMap.get("h_level_path"));
                }
                set.addAll(dataSet.getDataSet());
                ds.setDataSet(set);
                result.add(ds);
            } else {
                result.add(dataSet);
            }
        }
        return result;
    }
}
