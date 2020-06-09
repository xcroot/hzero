package org.hzero.generator.service;

import java.util.List;
import org.hzero.generator.entity.Mapping;

/**
 * @Description 导入数据服务
 * @Date 2019/12/16 15:01
 * @Author wanshun.zhang@hand-china.com
 */
public interface ImportDataService {
    /**
     * 获取文件列表
     * @param dir 文件路径
     * @param env 环境
     * @return 文件列表
     */
    List<Mapping> getGroovyServices(String dir, String env);
    /**
     * 获取文件列表
     * @param dir 文件路径
     * @param env 环境
     * @return 文件列表
     */
    List<Mapping> getDataServices(String dir, String env);

    /**
     * 导入数据、更新数据库
     * @param services 服务列表
     * @param dir 文件路径
     * @param env 环境
     */
    void importData(List<String> services, String dir, String env);

    void updateGroovy(List<String> services, String dir, String env);
}
