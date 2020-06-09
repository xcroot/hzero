package org.hzero.generator.export.helper;

import org.hzero.generator.export.helper.enums.LiquibaseEngineMode;
import org.springframework.stereotype.Component;

/**
 * @Description 全量导出
 * @Date 2020/2/5 15:56
 * @Author wanshun.zhang@hand-china.com
 */
@Component
public class FullExport extends LiquibaseEngine {

    public static FullExport createEngine(String filePath, LiquibaseEngineMode engineMode) {
        FullExport fullExport = new FullExport();
        fullExport.setFilePath(filePath);
        fullExport.setEngineMode(engineMode);
        fullExport.loadFile().loadExcel();
        return fullExport;
    }
}
