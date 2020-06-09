package org.hzero.generator.controller;

import org.hzero.generator.export.constants.Constants;
import org.hzero.generator.scan.domain.UiComponent;
import org.hzero.generator.scan.domain.vo.ServiceRouteVO;
import org.hzero.generator.service.*;
import org.hzero.generator.util.GeneratorUtils;
import org.hzero.generator.util.ZipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

/**
 * 前端扫描
 *
 * @author fanghan.liu 2020/02/11 17:08
 */
@Controller
@RequestMapping("/sys/ui-scans")
public class UiScanController {

    @Autowired
    private IUiScanService uiScanService;

    @Autowired
    private PromptExportService promptExportService;

    @Autowired
    private LovExportService lovExportService;

    @Autowired
    private MenuPermissionExportService menuPermissionExportService;

    @Autowired
    private IPermissionSetService permissionSetService;

    @PostMapping("/button")
    public ResponseEntity scanButton(@RequestBody List<ServiceRouteVO> routes, String resourceDir, Integer version) {
        return ResponseEntity.ok(uiScanService.scanButton(resourceDir, routes, version));
    }

    @PostMapping("/lov")
    public ResponseEntity scanLov(@RequestBody List<ServiceRouteVO> routes, String resourceDir, Integer version) {
        return ResponseEntity.ok(uiScanService.scanLov(resourceDir, routes, version));
    }

    @PostMapping("/prompt")
    public ResponseEntity scanPrompt(@RequestBody List<ServiceRouteVO> routes, String resourceDir, Integer version) {
        return ResponseEntity.ok(uiScanService.scanPrompt(resourceDir, routes, version));
    }

    @PostMapping("/api")
    public ResponseEntity scanApi(@RequestBody List<ServiceRouteVO> routes, String resourceDir, Integer version) {
        return ResponseEntity.ok(uiScanService.scanApi(resourceDir, routes, version));
    }

    @GetMapping("/router")
    @ResponseBody
    public List<ServiceRouteVO> getRouter(String level) {
        return uiScanService.getRoutersDetail(level);
    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@RequestBody List<ServiceRouteVO> routes) {
        permissionSetService.refreshPermissionSet(routes);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/export/permission")
    public void exportApi(@RequestBody List<ServiceRouteVO> routes, String version, HttpServletResponse response) throws IOException {
        List<String> results = uiScanService.listButton(routes);
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        menuPermissionExportService.exportMenuPermission(results, version);
        // 遍历列表生成Excel文件
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @PostMapping("/export/lov")
    public void exportLov(@RequestBody List<ServiceRouteVO> routes, HttpServletResponse response) throws IOException {
        List<UiComponent> results = uiScanService.listLov(routes);
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        lovExportService.exportLov(results);
        // 遍历列表生成Excel文件
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @PostMapping("/export/prompt")
    public void exportPrompt(@RequestBody List<ServiceRouteVO> routes, HttpServletResponse response) throws IOException {
        List<UiComponent> results = uiScanService.listPrompt(routes);
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        promptExportService.exportPrompt(results);
        // 遍历列表生成Excel文件
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @PostMapping("/export/new/prompt")
    public void exportNewPrompt(@RequestBody List<ServiceRouteVO> routes, HttpServletResponse response) throws IOException {
        List<UiComponent> results = uiScanService.listPrompt(routes);
        List<UiComponent> components = uiScanService.listNewPrompt(results);
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        promptExportService.exportNewPrompt(components);
        // 遍历列表生成Excel文件
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @PostMapping("/export/all/new/prompt")
    public void exportAllNewPrompt(HttpServletResponse response) throws IOException {
        List<UiComponent> results = uiScanService.listAllPrompt();
        List<UiComponent> components = uiScanService.listNewPrompt(results);
        File fileSource = new File(Constants.BASE_OUTPUT_PATH);
        promptExportService.exportNewPrompt(components);
        // 遍历列表生成Excel文件
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=dbScript");
        response.setContentType("application/octet-stream; charset=" + GeneratorUtils.DEFAULT_CHARACTER_SET);
        // 将文件保留目录结构打包成zip输出到前端
        ZipUtils.toZip(fileSource.getPath(), response.getOutputStream(), true);
        // 删除文件
        Files.walk(Paths.get(fileSource.getPath())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

}
