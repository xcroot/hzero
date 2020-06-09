package org.hzero.generator.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hzero.generator.service.IDBDiffService;
import org.hzero.generator.util.GeneratorUtils;
import org.hzero.generator.util.Result;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO 改造支持导出多种初始化数据
 */
@Controller
@RequestMapping("/sys/db")
public class DBDiffController {

    @Autowired
    private IDBDiffService dBDiffService;

    /**
     * 
     * 查询数据库
     * 
     * @param request
     * @param env
     * @return
     * @throws IOException
     */
    @ResponseBody
    @RequestMapping("/database")
    public Map<String, List<String>> database(HttpServletRequest request, @RequestParam String env) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        map.put("database", dBDiffService.selectDatabase(env));
        return map;
    }

    /**
     * 
     * 下载数据库对比脚本
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/diff")
    public void dbDiff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sourceEnv = request.getParameter("sourceEnv");
        String sourceDB = request.getParameter("sourceDB");
        String targetEnv = request.getParameter("targetEnv");
        String targetDB = request.getParameter("targetDB");
        if (StringUtils.isBlank(sourceEnv) || StringUtils.isBlank(sourceDB) || StringUtils.isBlank(targetEnv)
                        || StringUtils.isBlank(targetDB)) {
            return;
        }
        Document document = dBDiffService.compareDiff(sourceEnv, sourceDB, targetEnv, targetDB);
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());// 设置文本格式
        String result = outputter.outputString(document);
        byte[] data = result.getBytes(GeneratorUtils.DEFAULT_CHARACTER_SET);
        response.setHeader("Content-Disposition", "attachment; filename=" + targetEnv + "-" + targetDB + ".xml");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        IOUtils.write(data, response.getOutputStream());
    }

    /**
     * 
     * 执行更新数据库脚本
     *
     *
     * @param
     * @param
     * @param
     * @return
     */
    @ResponseBody
    @RequestMapping("/update")
    public Result dbUpdate(@RequestParam(value = "file", required = false) MultipartFile xmlFile,
                    HttpServletRequest request) {
        String updateEnv = request.getParameter("updateEnv");
        String updateDB = request.getParameter("updateDB");
        if (StringUtils.isBlank(updateEnv) || StringUtils.isBlank(updateDB) || xmlFile.isEmpty()) {
            return Result.error();
        }
        try {
            dBDiffService.dbUpdateImport(updateEnv, updateDB, xmlFile);
            return Result.ok();
        } catch (Exception e) {
            return Result.error();
        }

    }

}
