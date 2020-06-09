package org.hzero.generator.service.impl;

import java.util.List;
import java.util.Map;

import org.hzero.generator.mapper.DBInfoMapper;
import org.hzero.generator.service.IDBInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 
 * 数据库对比服务类
 * 
 * @author xianzhi.chen@hand-china.com 2018年9月17日上午11:46:06
 */
@Service
public class DBInfoServiceImpl implements IDBInfoService {

    @Autowired
    private DBInfoMapper dBCmpareMapper;

    @Override
    @DS(ENV_DEV)
    public List<String> selectDevDatabase() {
        return dBCmpareMapper.selectDatabase();
    }

    @Override
    @DS(ENV_DEV)
    public List<String> selectDevDatabaseTable(String dbname) {
        return dBCmpareMapper.selectDatabaseTable(dbname);
    }

    @Override
    @DS(ENV_DEV)
    public List<Map<String, String>> selectDevDatabaseColumn(String dbname) {
        return dBCmpareMapper.selectDatabaseColumn(dbname);
    }

    @Override
    @DS(ENV_DEV)
    public List<Map<String, String>> selectDevDatabaseIndex(String dbname) {
        return dBCmpareMapper.selectDatabaseIndex(dbname);
    }

    @Override
    @DS(ENV_DEV)
    public void updateDevDatabase(String sql) {
        dBCmpareMapper.updateDatabase(sql);
    }

    @Override
    @DS(ENV_TST)
    public List<String> selectTstDatabase() {
        return dBCmpareMapper.selectDatabase();
    }

    @Override
    @DS(ENV_TST)
    public List<String> selectTstDatabaseTable(String dbname) {
        return dBCmpareMapper.selectDatabaseTable(dbname);
    }

    @Override
    @DS(ENV_TST)
    public List<Map<String, String>> selectTstDatabaseColumn(String dbname) {
        return dBCmpareMapper.selectDatabaseColumn(dbname);
    }

    @Override
    @DS(ENV_TST)
    public List<Map<String, String>> selectTstDatabaseIndex(String dbname) {
        return dBCmpareMapper.selectDatabaseIndex(dbname);
    }

    @Override
    @DS(ENV_TST)
    public void updateTstDatabase(String sql) {
        dBCmpareMapper.updateDatabase(sql);
    }

    @Override
    @DS(ENV_UAT)
    public List<String> selectUatDatabase() {
        return dBCmpareMapper.selectDatabase();
    }

    @Override
    @DS(ENV_UAT)
    public List<String> selectUatDatabaseTable(String dbname) {
        return dBCmpareMapper.selectDatabaseTable(dbname);
    }

    @Override
    @DS(ENV_UAT)
    public List<Map<String, String>> selectUatDatabaseColumn(String dbname) {
        return dBCmpareMapper.selectDatabaseColumn(dbname);
    }

    @Override
    @DS(ENV_UAT)
    public List<Map<String, String>> selectUatDatabaseIndex(String dbname) {
        return dBCmpareMapper.selectDatabaseIndex(dbname);
    }

    @Override
    @DS(ENV_UAT)
    public void updateUatDatabase(String sql) {
        dBCmpareMapper.updateDatabase(sql);
    }

    @Override
    @DS(ENV_PRD)
    public List<String> selectPrdDatabase() {
        return dBCmpareMapper.selectDatabase();
    }

    @Override
    @DS(ENV_PRD)
    public List<String> selectPrdDatabaseTable(String dbname) {
        return dBCmpareMapper.selectDatabaseTable(dbname);
    }

    @Override
    @DS(ENV_PRD)
    public List<Map<String, String>> selectPrdDatabaseColumn(String dbname) {
        return dBCmpareMapper.selectDatabaseColumn(dbname);
    }

    @Override
    @DS(ENV_PRD)
    public List<Map<String, String>> selectPrdDatabaseIndex(String dbname) {
        return dBCmpareMapper.selectDatabaseIndex(dbname);
    }

    @Override
    @DS(ENV_PRD)
    public void updatePrdDatabase(String sql) {
        dBCmpareMapper.updateDatabase(sql);
    }

}
