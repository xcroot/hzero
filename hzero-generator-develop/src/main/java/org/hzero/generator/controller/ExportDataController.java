package org.hzero.generator.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.hzero.generator.dto.ServiceDTO;
import org.hzero.generator.export.constants.Constants;
import org.hzero.generator.service.ExportDataService;
import org.hzero.generator.service.MenuPermissionExportService;
import org.hzero.generator.util.GeneratorUtils;
import org.hzero.generator.util.ZipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author wanshun.zhang@hand-china.com
 * @date 2019.11.01
 */
@Controller
@RequestMapping("/sys/export-datas")
public class ExportDataController {

    @Autowired
    private ExportDataService initDataService;
    @Autowired
    private MenuPermissionExportService menuPermissionExportService;

    @ResponseBody
    @GetMapping("/services")
    public List<ServiceDTO> getExportServices(@RequestParam String env, @RequestParam String version) {
        List<ServiceDTO> exportServices = initDataService.getExportServices(env, version);
        return exportServices;
    }

    /**
     * 导出数据
     *
     * @param serviceList 服务列表
     * @param env         环境
     * @param response    response
     * @throws IOException IOException
     */
    @RequestMapping("/export")
    @ResponseBody
    public void exportInitData(@RequestBody List<ServiceDTO> serviceList, @RequestParam String env, HttpServletResponse response) throws IOException {
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        // 遍历列表生成Excel文件
        initDataService.exportInitData(env, serviceList);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    /**
     * 导出数据
     *
     * @param serviceList 服务列表
     * @param env         环境
     * @param response    response
     * @throws IOException IOException
     */
    @RequestMapping("/virtual-export")
    @ResponseBody
    public void virtualExportInitData(@RequestBody List<ServiceDTO> serviceList, @RequestParam String env, HttpServletResponse response) throws IOException {
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        // 遍历列表生成Excel文件
        initDataService.virtualExportInitData(env, serviceList);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    /**
     * 导出数据
     *
     * @param serviceList 服务列表
     * @param env         环境
     * @param response    response
     * @throws IOException IOException
     */
    @RequestMapping("/diff-export")
    @ResponseBody
    public void diffExportData(@RequestBody List<ServiceDTO> serviceList, @RequestParam String env, @RequestParam String dir, HttpServletResponse response) throws IOException {
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        // 遍历列表生成Excel文件
        initDataService.diffExportInitData(env, dir, serviceList);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

}
