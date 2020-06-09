package org.hzero.generator.service.impl;

import java.util.List;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.commons.lang3.StringUtils;
import org.hzero.generator.mapper.InitDataMapper;
import org.hzero.generator.service.InitDataInfoService;
import org.hzero.generator.util.ScriptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 描述:
 *
 * @author wanshun.zhang@hand-china.com
 * @date 2019/12/04 18:03
 */
@Service
public class InitDataInfoServiceImpl implements InitDataInfoService {

    @Autowired
    ScriptUtils scriptUtils;
    @Autowired
    InitDataMapper initDataMapper;

    @DS(ENV_DEV)
    @Override
    public void exportDevData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.pullCreate(serviceList);
    }

    @DS(ENV_TST)
    @Override
    public void exportTstData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.pullCreate(serviceList);
    }

    @DS(ENV_UAT)
    @Override
    public void exportUatData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.pullCreate(serviceList);
    }

    @DS(ENV_PRD)
    @Override
    public void exportPrdData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.pullCreate(serviceList);
    }
    @DS(ENV_DEV)
    @Override
    public void exportVirtualDevData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.virtualMenuCreate(serviceList);
    }

    @DS(ENV_TST)
    @Override
    public void exportVirtualTstData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.virtualMenuCreate(serviceList);
    }

    @DS(ENV_UAT)
    @Override
    public void exportVirtualUatData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.virtualMenuCreate(serviceList);
    }

    @DS(ENV_PRD)
    @Override
    public void exportVirtualPrdData(List<org.hzero.generator.entity.Service> serviceList) {
        scriptUtils.virtualMenuCreate(serviceList);
    }

    @DS(ENV_DEV)
    @Override
    public void diffExportDevData(List<org.hzero.generator.entity.Service> serviceList,String dir) {
        scriptUtils.diffCreate(serviceList,dir);
    }

    @DS(ENV_TST)
    @Override
    public void diffExportTstData(List<org.hzero.generator.entity.Service> serviceList,String dir) {
        scriptUtils.diffCreate(serviceList,dir);
    }

    @DS(ENV_UAT)
    @Override
    public void diffExportUatData(List<org.hzero.generator.entity.Service> serviceList,String dir) {
        scriptUtils.diffCreate(serviceList,dir);
    }

    @DS(ENV_PRD)
    @Override
    public void diffExportPrdData(List<org.hzero.generator.entity.Service> serviceList,String dir) {
        scriptUtils.diffCreate(serviceList,dir);
    }

    @DS(ENV_DEV)
    @Override
    public List<String> selectDevDatabase() {
        return initDataMapper.selectDatabase();
    }

    @DS(ENV_TST)
    @Override
    public List<String> selectTstDatabase() {
        return initDataMapper.selectDatabase();
    }

    @DS(ENV_UAT)
    @Override
    public List<String> selectUatDatabase() {
        return initDataMapper.selectDatabase();
    }

    @DS(ENV_PRD)
    @Override
    public List<String> selectPrdDatabase() {
        return initDataMapper.selectDatabase();
    }

    @DS(ENV_DEV)
    @Override
    public void createDevDatabase(String database, String schema) {
        if (StringUtils.equals(ImportDataServiceImpl.MYSQL, database)){
            initDataMapper.createDatabaseMysql(schema);
        }else {
            initDataMapper.createDatabase(schema);
        }
    }

    @DS(ENV_TST)
    @Override
    public void createTstDatabase(String database, String schema) {
        if (StringUtils.equals(ImportDataServiceImpl.MYSQL, database)){
            initDataMapper.createDatabaseMysql(schema);
        }else {
            initDataMapper.createDatabase(schema);
        }
    }

    @DS(ENV_UAT)
    @Override
    public void createUatDatabase(String database, String schema) {
        if (StringUtils.equals(ImportDataServiceImpl.MYSQL, database)){
            initDataMapper.createDatabaseMysql(schema);
        }else {
            initDataMapper.createDatabase(schema);
        }
    }

    @DS(ENV_PRD)
    @Override
    public void createPrdDatabase(String database, String schema) {
        if (StringUtils.equals(ImportDataServiceImpl.MYSQL, database)){
            initDataMapper.createDatabaseMysql(schema);
        }else {
            initDataMapper.createDatabase(schema);
        }
    }
}
