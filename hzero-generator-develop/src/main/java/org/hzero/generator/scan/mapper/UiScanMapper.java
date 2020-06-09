package org.hzero.generator.scan.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hzero.generator.scan.domain.Menu;
import org.hzero.generator.scan.domain.Prompt;
import org.hzero.generator.scan.domain.UiApi;
import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.dto.PermissionSetSearchDTO;
import org.hzero.generator.scan.domain.vo.UiComponentVO;

import java.util.List;

/**
 * description
 *
 * @author fanghan.liu 2020/02/12 10:31
 */
@Mapper
public interface UiScanMapper {

    /**
     * 清除组件
     *
     * @param uiRoute
     * @param compType
     */
    void clearComp(@Param("uiRoute") String uiRoute, @Param("compType") String compType);

    /**
     * 新增组件
     *
     * @param uiComponent
     */
    void insertComp(@Param("uiComponent") UiComponent uiComponent);

    /**
     * 清除api
     *
     * @param uiRoute
     */
    void clearApi(@Param("uiRoute") String uiRoute);

    /**
     * 根据code获取组件id
     *
     * @param compCode
     * @return
     */
    List<UiComponent> getCompByCode(@Param("compCode") String compCode);

    /**
     * 新增api
     *
     * @param uiApi
     */
    void insertApi(@Param("uiApi") UiApi uiApi);

    /**
     * 根据路由获取权限按钮
     *
     * @param uiRoutes 路由
     * @return api列表
     */
    List<UiComponent> listButtonByRoutes(@Param("uiRoutes") List<String> uiRoutes);

    /**
     * 根据路由获取值集&视图
     *
     * @param uiRoutes 路由
     * @return lov列表
     */
    List<UiComponent> listLovByRoutes(@Param("uiRoutes") List<String> uiRoutes);

    /**
     * 根据路由获取多语言
     *
     * @param uiRoutes 路由
     * @return 多语言列表
     */
    List<UiComponent> listPromptByRoutes(@Param("uiRoutes") List<String> uiRoutes);

    /**
     * 获取菜单
     *
     * @return 菜单
     */
    List<Menu> listMenu(@Param("level") String level);

    /**
     * 根据路由获取API
     *
     * @param uiRoute 页面路由
     * @return api
     */
    List<UiApi> selectApiByUiRoute(@Param("uiRoute") String uiRoute);

    /**
     * 依据页面路由, 获取已扫描UI权限代码信息
     *
     * @param pageRoute 页面路由
     * @return UI权限代码集合
     */
    List<UiComponentVO> selectUiPermissionByPageRoute(@Param("pageRoute") String pageRoute);

    /**
     * 根据路由获取SQL LOV
     *
     * @return sql lov
     */
    List<String> selectUiSqlLovByPageRoute(@Param("uiLovs") List<UiComponent> uiLovs);


    List<UiApi> select(@Param("uiApi") UiApi uiApi);

    /**
     * 根据key获取多语言
     * @param promptKey
     * @return
     */
    List<Prompt> selectPromptByKey(@Param("promptKey") String promptKey);

    List<UiComponent> selectAllUiPrompt();
}
