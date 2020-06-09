package org.hzero.generator.service;

import java.util.List;
import org.hzero.generator.scan.domain.UiComponent;

/**
 * @Description 值集导出
 * @Date 2020-02-17 14:20
 * @Author wanshun.zhang@hand-china.com
 */
public interface LovExportService {
    /**
     * 导出值集
     *
     * @param params 导出参数
     */
    void exportLov(List<UiComponent> params);
}
