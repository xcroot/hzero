package org.hzero.generator.scan.infra.engine;

import org.hzero.generator.export.helper.LiquibaseEngine;
import org.hzero.generator.export.helper.entity.DataSet;
import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.hzero.generator.scan.domain.UiComponent;

import java.util.*;

/**
 * description
 *
 * @author fanghan.liu 2020/03/13 14:59
 */
public class PromptExportEngine extends LiquibaseEngine {

    private List<UiComponent> prompts;
    private static final String PROMPT_KEY = "#prompt_key";
    private static final String TENANT_ID = "#tenant_id";
    private static final String PROMPT_CODE = "#prompt_code";
    private static final String PROMPT_ID = "*prompt_id";
    private static final String DESCRIPTION = "description";
    private static final String LANG = "#lang";
    private static final String ZH_CN = "zh_CN";
    private static final String EN_US = "en_US";
    private static final Long DEFAULT_TENANT_ID = 0L;

    public static PromptExportEngine createEngine(String filePath, LiquibaseEngineMode engineMode, List<UiComponent> prompts) {
        PromptExportEngine promptExportEngine = new PromptExportEngine();
        promptExportEngine.setFilePath(filePath);
        promptExportEngine.setEngineMode(engineMode);
        promptExportEngine.loadFile().loadExcel();
        promptExportEngine.setPrompts(prompts);
        return promptExportEngine;
    }

    @Override
    public List<DataSet> dataFilter(List<DataSet> dataSets) {
        dataSets.forEach(dataSet -> {
            Set<Map<String, Object>> set = new HashSet<>();
            prompts.forEach(prompt -> {
                HashMap<String, Object> zhData = new HashMap<>();
                zhData.put(PROMPT_KEY, prompt.getPromptKey());
                zhData.put(TENANT_ID, DEFAULT_TENANT_ID);
                zhData.put(PROMPT_CODE, prompt.getPromptCode());
                zhData.put(DESCRIPTION, prompt.getDescription());
                zhData.put(PROMPT_ID, null);
                zhData.put(LANG, ZH_CN);
                set.add(zhData);
                HashMap<String, Object> enData = new HashMap<>();
                enData.put(PROMPT_KEY, prompt.getPromptKey());
                enData.put(TENANT_ID, DEFAULT_TENANT_ID);
                enData.put(PROMPT_CODE, prompt.getPromptCode());
                enData.put(DESCRIPTION, prompt.getDescription());
                enData.put(PROMPT_ID, null);
                enData.put(LANG, EN_US);
                set.add(enData);
            });
            dataSet.setDataSet(set);
        });
        return dataSets;
    }

    private void setPrompts(List<UiComponent> prompts) {
        this.prompts = prompts;
    }
}
