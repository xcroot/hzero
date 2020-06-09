package org.hzero.generator.service;

import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.vo.ServiceRouteVO;

import java.util.List;

/**
 * 前端扫描服务接口
 *
 * @author fanghan.liu 2020/02/11 19:10
 */
public interface IUiScanService {

    /**
     * 扫描前端权限按钮
     *
     * @param resourceDirPath 前端工程路径
     * @param routes          选中路由
     * @param version         CLI版本
     */
    boolean scanButton(String resourceDirPath, List<ServiceRouteVO> routes, Integer version);

    /**
     * 扫描前端值集&视图
     *
     * @param resourceDirPath 前端工程路径
     * @param routes          选中路由
     * @param version         CLI版本
     */
    boolean scanLov(String resourceDirPath, List<ServiceRouteVO> routes, Integer version);

    /**
     * 扫描前端多语言
     *
     * @param resourceDirPath 前端工程路径
     * @param routes          选中路由
     * @param version         CLI版本
     */
    boolean scanPrompt(String resourceDirPath, List<ServiceRouteVO> routes, Integer version);

    /**
     * 扫描前端api
     *
     * @param resourceDirPath 前端工程路径
     * @param routes          选中路由
     * @param version         CLI版本
     */
    boolean scanApi(String resourceDirPath, List<ServiceRouteVO> routes, Integer version);

    /**
     * 获取路由明细
     *
     * @param level 层级
     * @return 服务-路由
     */
    List<ServiceRouteVO> getRoutersDetail(String level);

    /**
     * 获取路由
     *
     * @param routes 选中菜单
     * @return api
     */
    List<String> listButton(List<ServiceRouteVO> routes);

    /**
     * 获取lov
     *
     * @param routes 路由
     * @return lov
     */
    List<UiComponent> listLov(List<ServiceRouteVO> routes);

    /**
     * 获取多语言
     *
     * @param routes 路由
     * @return 多语言
     */
    List<UiComponent> listPrompt(List<ServiceRouteVO> routes);

    /**
     * 获取所有页面多语言
     *
     * @return 多语言
     */
    List<UiComponent> listAllPrompt();

    /**
     * 获取新增多语言
     *
     * @param uiComponents 多语言组件
     * @return 多语言
     */
    List<UiComponent> listNewPrompt(List<UiComponent> uiComponents);


}
