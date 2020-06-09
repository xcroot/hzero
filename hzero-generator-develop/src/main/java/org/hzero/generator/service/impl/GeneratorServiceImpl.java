package org.hzero.generator.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.hzero.generator.dto.GeneratorEntity;
import org.hzero.generator.mapper.GeneratorMapper;
import org.hzero.generator.service.IGeneratorService;
import org.hzero.generator.util.GenByDDDUtils;
import org.hzero.generator.util.GenByMVCUtils;
import org.hzero.generator.util.GenDBScriptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 代码生成器服务实现类 description
 *
 * @author xianzhi.chen@hand-china.com 2018年6月19日下午3:23:26
 */
@Service
public class GeneratorServiceImpl implements IGeneratorService {

    @Autowired
    private GeneratorMapper generatorMapper;

    @Override
    public List<Map<String, Object>> queryList(Map<String, Object> map) {
        return generatorMapper.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return generatorMapper.queryTotal(map);
    }

    @Override
    public Map<String, String> queryTable(String tableName) {
        return generatorMapper.queryTable(tableName);
    }

    @Override
    public List<Map<String, String>> queryColumns(String tableName) {
        return generatorMapper.queryColumns(tableName);
    }

    @Override
    public List<Map<String, String>> queryIndexs(String tableName) {
        return generatorMapper.queryIndexs(tableName);
    }

    @Override
    public void executeDDL(String sql) {
        generatorMapper.executeDDL(sql);
    }

    /**
     * DDD模型代码生成
     * 
     * @param info
     * @return
     */
    @Override
    public byte[] generatorCodeByDDD(GeneratorEntity info) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : info.getTableNames()) {
            // 查询表信息
            Map<String, String> table = queryTable(tableName);
            // 查询列信息
            List<Map<String, String>> columns = queryColumns(tableName);
            // 生成代码
            GenByDDDUtils.generatorCode(info, table, columns, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * MVC模型代码生成
     * 
     * @param info
     * @return
     */
    @Override
    public byte[] generatorCodeByMVC(GeneratorEntity info) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : info.getTableNames()) {
            // 查询表信息
            Map<String, String> table = queryTable(tableName);
            // 查询列信息
            List<Map<String, String>> columns = queryColumns(tableName);
            // 生成代码
            GenByMVCUtils.generatorCode(info, table, columns, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    @Override
    public byte[] generatorDBScript(GeneratorEntity info) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : info.getTableNames()) {
            // 查询表信息
            Map<String, String> table = queryTable(tableName);
            // 查询列信息
            List<Map<String, String>> columns = queryColumns(tableName);
            // 查询索引信息
            List<Map<String, String>> indexs = queryIndexs(tableName);
            // 生成脚本文件
            GenDBScriptUtils.generatorCode(info, table, columns,indexs, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

}
