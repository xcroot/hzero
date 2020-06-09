package org.hzero.generator.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.scan.domain.*;
import org.hzero.generator.scan.domain.dto.PermissionSetSearchDTO;
import org.hzero.generator.scan.domain.vo.Lov;
import org.hzero.generator.scan.domain.vo.ServiceRouteVO;
import org.hzero.generator.scan.domain.vo.UiComponentVO;
import org.hzero.generator.scan.infra.constant.*;
import org.hzero.generator.scan.mapper.MenuMapper;
import org.hzero.generator.scan.mapper.RoleMapper;
import org.hzero.generator.scan.mapper.UiScanMapper;
import org.hzero.generator.service.IPermissionSetService;
import org.hzero.generator.util.SwitchDatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author fanghan.liu 2020/02/27 10:50
 */
@Service
public class PermissionSetServiceImpl implements IPermissionSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionSetServiceImpl.class);

    @Autowired
    SwitchDatabaseUtils switchDatabaseUtils;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private UiScanMapper uiScanMapper;
    @Autowired
    private RoleMapper roleMapper;

    @Value("${scan.tool.temporary:hzero_generator}")
    private String generator;

    @Value("${scan.tool.permission:hzero_platform}")
    private String platform;

    @Value("${scan.tool.service:hzero_admin}")
    private String admin;

    @Override
    public void refreshPermissionSet(List<ServiceRouteVO> routes) {
        routes.forEach(route -> {
            refreshUiPermissionOfMenu(Long.parseLong(route.getId()));
            if (CollectionUtils.isNotEmpty(route.getChildren())) {
                refreshPermissionSet(route.getChildren());
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshUiPermissionOfMenu(Long menuId) {
        switchDatabaseUtils.switchDatabase(platform);
        Menu menu = menuMapper.getMenuById(menuId);
        if (menu == null) {
            return;
        }
        if (StringUtils.isEmpty(menu.getRoute()) || !"menu".equals(menu.getType())) {
            LOGGER.info("menu {} has no route config, exit.", menu.getCode());
            return;
        }
        LOGGER.info("start refresh menu : {}", menu.getName());
        switchDatabaseUtils.switchDatabase(generator);
        // 查询路由对应可分配api
        List<UiApi> allApis = uiScanMapper.selectApiByUiRoute(menu.getRoute());
        // 查询路由对应的可分配UI权限组件
        List<UiComponentVO> rawUiPermissionComps = uiScanMapper.selectUiPermissionByPageRoute(menu.getRoute());
        // 查询路由对应的SQL LOV
        List<UiComponent> uiLovs = uiScanMapper.listLovByRoutes(Collections.singletonList(menu.getRoute()));
        List<Permission> allPermissions = convertApiToPermission(allApis);
        switchDatabaseUtils.switchDatabase(platform);
        String[] lovCodes = null;
        if (CollectionUtils.isNotEmpty(uiLovs)) {
            List<String> sqlLovs = uiScanMapper.selectUiSqlLovByPageRoute(uiLovs);
            lovCodes = sqlLovs.toArray(new String[0]);
        }
        PermissionSetSearchDTO searchDTO = new PermissionSetSearchDTO();
        searchDTO.setParentMenuId(menuId);
        searchDTO.setTenantId(0L);
        List<Menu> childrenPermissionSets = menuMapper.selectMenuPermissionSet(searchDTO);
        // 查找默认权限集
        Menu defaultPs = null;
        String defaultPsCode = menu.getCode() + ".ps.default";
        for (Menu ps : childrenPermissionSets) {
            if (defaultPsCode.equals(ps.getCode())) {
                defaultPs = ps;
            }
        }
        if (defaultPs != null) {
            // 分配SQL类型LOV
            if (lovCodes != null && lovCodes.length > 0) {
                assignPsPermissions(defaultPs.getId(), PermissionType.LOV, lovCodes);
            }
            // 默认权限集对应分配API
            List<Permission> assignPermissions = menuMapper.selectPermissionSetPermissions(defaultPs.getId(), defaultPs.getTenantId());
            Iterator<Permission> iterator = allPermissions.iterator();
            while (iterator.hasNext()) {
                Permission permission = iterator.next();
                if (!permission.getLevel().equals(defaultPs.getLevel())) {
                    iterator.remove();
                    continue;
                }
                for (Permission assignPrmission : assignPermissions) {
                    if (assignPrmission.getId().equals(permission.getId())) {
                        iterator.remove();
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(allPermissions)) {
                String[] codes = allPermissions.stream().map(Permission::getCode).toArray(String[]::new);
                assignPsPermissions(defaultPs.getId(), PermissionType.PERMISSION, codes);
            }
        }
        // 按照权限代码分组, 如果重复则取第一个发现的元素的名称及描述, 类型字段为逗号拼接的结果
        Map<String, List<UiComponentVO>> rawUiPermissionCompsMap = rawUiPermissionComps.stream().collect(Collectors.groupingBy(UiComponentVO::getCompCode));
        List<UiComponentVO> uiPermissionComps = new ArrayList<>();

        rawUiPermissionCompsMap.forEach((permissionCode, groupUiPermissionComps) -> {
            String pageRoute = groupUiPermissionComps.get(0).getUiRoute();
            String permissionName = groupUiPermissionComps.get(0).getDescription();
            String permissionType = groupUiPermissionComps.stream().map(UiComponentVO::getCompType).collect(Collectors.joining(Constants.Symbol.COMMA));
            uiPermissionComps.add(new UiComponentVO(pageRoute, permissionCode, permissionName, permissionType).setUiCompId(groupUiPermissionComps.get(0).getUiCompId()));
        });

        Set<String> uiPermissionCompCodeSet = uiPermissionComps.stream().map(UiComponentVO::getCompCode).collect(Collectors.toSet());

        // 查询菜单下已分配的权限集信息
        PermissionSetSearchDTO permissionSetSearchDTO = new PermissionSetSearchDTO();
        permissionSetSearchDTO.setParentMenuId(menu.getId());
        permissionSetSearchDTO.setTenantId(menu.getTenantId());

        // 菜单已分配权限集
        List<Menu> assignedPermissionSetList = menuMapper.selectMenuPermissionSet(permissionSetSearchDTO);
        Set<String> assignedPermissionSetCodeSet = assignedPermissionSetList.stream().map(Menu::getCode).collect(Collectors.toSet());

        // 识别操作类别
        // 更新 - 名称、类型可能会变
        List<Menu> updatePermissionSetList = assignedPermissionSetList.parallelStream().filter(
                item -> uiPermissionCompCodeSet.stream().anyMatch(comp -> item.getCode().endsWith(comp))
        ).collect(Collectors.toList());
        updatePermissionSetList.forEach(item -> {
            UiComponentVO targetUiPermissionCompVO = uiPermissionComps.stream().filter(target -> item.getCode().endsWith(target.getCompCode())).findFirst().orElseThrow(IllegalStateException::new);
            if (!Objects.equals(item.getDescription(), targetUiPermissionCompVO.getDescription()) || !Objects.equals(item.getName(), targetUiPermissionCompVO.getCompType())) {
                item.setName(targetUiPermissionCompVO.getDescription());
                item.setPermissionType(targetUiPermissionCompVO.getCompType());
                switchDatabaseUtils.switchDatabase(platform);
                menuMapper.updatePermissionType(item);
            }
            switchDatabaseUtils.switchDatabase(generator);
            // 查询需要新加Api权限
            UiApi uiApi = new UiApi();
            uiApi.setApiLevel(item.getLevel());
            uiApi.setUiCompId(targetUiPermissionCompVO.getUiCompId());
            List<UiApi> uiApiList = uiScanMapper.select(uiApi);
            String[] permissionCodes = convertPermissionCompDtlToPermissionCode(uiApiList);
            if (ArrayUtils.isNotEmpty(permissionCodes)) {
                assignPsPermissions(item.getId(), PermissionType.PERMISSION, permissionCodes);
            }
        });

        // 删除 - 权限集无删除操作
//        List<Menu> deletePermissionSetList = ListUtils.subtract(assignedPermissionSetList, updatePermissionSetList);
//        deletePermissionSetList.forEach(item -> {
//            this.deleteMenuById(item.getTenantId(), item.getId());
//        });

        // 新增
        List<UiComponentVO> insertingUiPermissionCompVOList = uiPermissionComps.stream().filter(item -> assignedPermissionSetCodeSet.stream().noneMatch(a -> a.endsWith(item.getCompCode()))).collect(Collectors.toList());
        insertingUiPermissionCompVOList.forEach(item -> {
            Menu ps = new Menu();
            /**
             * ADD
             * 权限编码 = 角色编码 + 扫描到的权限编码
             */
            ps.setCode(menu.getCode() + "." + item.getCompCode());
            ps.setName(item.getDescription());
            ps.setPermissionType(item.getCompType());
            ps.setLevel(menu.getLevel());
            ps.setParentId(menu.getId());
            ps.setType("ps");
            ps.setSort(0);
            ps.setTenantId(menu.getTenantId());
            // 初始化
            ps.initMenu();
            ps.initQuickIndex();
            ps.setIcon(menu.getIcon());
            ps.setEnabledFlag(menu.getEnabledFlag());
            ps.setVirtualFlag(0);
            ps.setCustomFlag(menu.getCustomFlag());
            // 创建权限集
            this.createMenu(ps);

            // 查询需要新加Api权限集
            UiApi uiApi = new UiApi();
            uiApi.setApiLevel(ps.getLevel());
            uiApi.setUiCompId(item.getUiCompId());
            switchDatabaseUtils.switchDatabase(generator);
            List<UiApi> uiApiList = uiScanMapper.select(uiApi);
            String[] permissionCodes = convertPermissionCompDtlToPermissionCode(uiApiList);
            if (ArrayUtils.isNotEmpty(permissionCodes)) {
                assignPsPermissions(ps.getId(), PermissionType.PERMISSION, permissionCodes);
            }

        });
    }

    private void createMenu(Menu menu) {
        switchDatabaseUtils.switchDatabase(platform);
        Menu parentMenu = null;
        if (!Menu.ROOT_ID.equals(menu.getParentId())) {
            parentMenu = menuMapper.getMenuById(menu.getParentId());
            if (parentMenu == null) {
                throw new RuntimeException("hiam.error.menu.parentMenuNotFound");
            }
        }

        // 验证
        menu.validate(parentMenu);

        // level path
        menu.initLevelPath(parentMenu);

        // 创建并返回
        menuMapper.insertSelective(menu);
        // 创建多语言
        initMenuTl(menu);

        // 权限集挂到超级角色下
        if (HiamMenuType.PS.value().equals(menu.getType())) {
            assignPermissionSetsToSuperAdmin(menu.getLevel(), menu.getId(), RolePermissionType.PS);
            //add by 吴星星： 如果层级是organization  且客户化标志为1，还需要将角色新增到当前租户的租户管理员下
            if (HiamResourceLevel.ORGANIZATION.value().equals(menu.getLevel()) &&
                    Constants.Flag.YES.equals(menu.getCustomFlag())) {
                assignPermissionSetsToTenantManager(menu.getTenantId(), menu.getId(), RolePermissionType.PS);
            }
        }
        LOGGER.info("create menu {}", menu);
    }

    private List<Permission> convertApiToPermission(List<UiApi> uiApiList) {
        List<Permission> permissions = new ArrayList<>();
        uiApiList.forEach(uiApi -> {
            Permission uniqueIndex = new Permission();
            uniqueIndex.setLevel(uiApi.getApiLevel());
            uniqueIndex.setMethod(uiApi.getMethod());
            uniqueIndex.setPath(uiApi.getPath());
            // 由服务简码获取全服务名
            switchDatabaseUtils.switchDatabase(admin);
            List<String> serviceNames = menuMapper.getServiceName(uiApi.getServiceName());
            if (CollectionUtils.isEmpty(serviceNames)) {
                return;
            }
            switchDatabaseUtils.switchDatabase(platform);
            uniqueIndex.setServiceName(serviceNames.get(0));
            List<Permission> permissionList = menuMapper.selectOne(uniqueIndex);
            if (CollectionUtils.isNotEmpty(permissionList)) {
                permissions.add(permissionList.get(0));
            }
        });
        return permissions;
    }

    @Override
    public void assignPsPermissions(Long permissionSetId, PermissionType permissionType, String[] permissionCodes) {
        switchDatabaseUtils.switchDatabase(platform);
        Menu ps = menuMapper.getMenuById(permissionSetId);
        if (ps == null) {
            throw new RuntimeException("hiam.error.ps.notFound");
        }

        if (!HiamMenuType.PS.value().equals(ps.getType())) {
            throw new RuntimeException("hiam.error.ps.errorType");
        }

        // 权限需验证层级 权限层级必须和菜单层级一致
        if (PermissionType.PERMISSION.equals(permissionType)) {
            String psLevel = ps.getLevel();
            List<Permission> permissions = menuMapper.selectPermissionByCodes(Arrays.asList(permissionCodes));
            for (Permission permission : permissions) {
                if (!StringUtils.equals(psLevel, permission.getLevel())) {
                    throw new RuntimeException("hiam.warn.ps.permissionLevelNotMatch");
                }
            }
        }
        // Lov校验租户是否一样
        else if (PermissionType.LOV.equals(permissionType)) {
            Long tenantId = ps.getTenantId();
            List<Lov> lovs = menuMapper.selectLovByCodes(Arrays.asList(permissionCodes), tenantId);
            for (Lov lov : lovs) {
                if (!Objects.equals(lov.getTenantId(), 0L) && !Objects.equals(lov.getTenantId(), tenantId)) {
                    throw new RuntimeException("hiam.warn.ps.lovDenyToTenant");
                }
            }
        }

        for (String code : permissionCodes) {
            MenuPermission mp = new MenuPermission();
            mp.setMenuId(permissionSetId);
            mp.setPermissionCode(code);

            if (menuMapper.selectMenuPermissionCount(mp) == 0) {
                menuMapper.insertMenuPermission(mp);
            }
        }
    }

    private String[] convertPermissionCompDtlToPermissionCode(List<UiApi> uiApiList) {
        return uiApiList.stream()
                .map(uiApi -> {
                    Permission uniqueIndex = new Permission();
                    uniqueIndex.setLevel(uiApi.getApiLevel());
                    uniqueIndex.setMethod(uiApi.getMethod());
                    uniqueIndex.setPath(uiApi.getPath());
                    // 由服务简码获取全服务名
                    switchDatabaseUtils.switchDatabase(admin);
                    List<String> serviceNames = menuMapper.getServiceName(uiApi.getServiceName());
                    if (CollectionUtils.isEmpty(serviceNames)) {
                        return null;
                    }
                    switchDatabaseUtils.switchDatabase(platform);
                    uniqueIndex.setServiceName(serviceNames.get(0));
                    List<Permission> permissions = menuMapper.selectOne(uniqueIndex);
                    if (CollectionUtils.isNotEmpty(permissions)) {
                        return permissions.get(0).getCode();
                    } else {
                        return null;
                    }
                })
                .filter(StringUtils::isNotEmpty)
                .toArray(String[]::new);
    }

    private void assignPermissionSetsToSuperAdmin(String level, Long permissionSetId, RolePermissionType rolePermissionType) {
        String superRoleCode = StringUtils.equals(level, HiamResourceLevel.SITE.value()) ?
                Constants.RoleCode.SITE : Constants.RoleCode.TENANT;
        List<Role> superRole = roleMapper.selectByCode(superRoleCode);
        RolePermission rolePermission = new RolePermission(superRole.get(0).getId(), permissionSetId, Constants.YesNoFlag.NO,
                Constants.YesNoFlag.YES, rolePermissionType.name());
        roleMapper.insertRolePermission(rolePermission);
    }

    private void assignPermissionSetsToTenantManager(Long tenantId, Long permissionSetId, RolePermissionType rolePermissionType) {
        //查询租户管理员模板角色
        Role tenantRoleTemplate = roleMapper.selectByCode(Constants.ORGANIZATION_TENANT_ROLE_TPL_CODE).get(0);
        if (tenantRoleTemplate == null) {
            LOGGER.warn("assign permission to tenant admin, not found template role [{}]", Constants.ORGANIZATION_TENANT_ROLE_TPL_CODE);
            return;
        }
        Role queryParam = new Role();
        queryParam.setInheritRoleId(tenantRoleTemplate.getId());
        queryParam.setTenantId(tenantId);
        List<Role> tenantRoles = roleMapper.selectbyTenantIdAndInheritRoleId(queryParam);
        if (CollectionUtils.isEmpty(tenantRoles)) {
            LOGGER.info("assign permission to tenant admin, not found tenant admin role, tenantId is {}", tenantId);
            return;
        }
        //直接挂上去
        RolePermission rolePermission = new RolePermission(tenantRoles.get(0).getId(), permissionSetId, Constants.YesNoFlag.NO,
                Constants.YesNoFlag.YES, rolePermissionType.name());
        roleMapper.insertRolePermission(rolePermission);
    }

    private void initMenuTl(Menu menu) {
        List<String> lang = new ArrayList<>();
        lang.add("zh_CN");
        lang.add("en_US");
        menuMapper.insertMenuTl(menu.getId(), menu.getName(), lang);
    }
}
