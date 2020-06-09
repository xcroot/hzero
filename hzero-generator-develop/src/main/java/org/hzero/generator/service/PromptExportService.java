package org.hzero.generator.service;

import java.util.List;
import org.hzero.generator.scan.domain.UiComponent;

/**
 * @Description 多语言导出
 * @Date 2020-02-17 14:21
 * @Author wanshun.zhang@hand-china.com
 */
public interface PromptExportService {
    /**
     * 导出多语言
     *
     * @param params 导出参数
     */
    void exportPrompt(List<UiComponent> params);

    /**
     * 导出新增多语言
     *
     * @param params 导出参数
     */
    void exportNewPrompt(List<UiComponent> params);
}
