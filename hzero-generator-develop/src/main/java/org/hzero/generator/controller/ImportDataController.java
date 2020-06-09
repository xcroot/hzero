package org.hzero.generator.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.hzero.generator.entity.Mapping;
import org.hzero.generator.service.ImportDataService;
import org.hzero.generator.util.Result;
import org.hzero.generator.util.XmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 数据导入
 * @Date 2019/12/16 14:53
 * @Author wanshun.zhang@hand-china.com
 */
@RestController
@RequestMapping("/sys/import-datas")
public class ImportDataController {

    @Autowired
    ImportDataService importDataService;

    @GetMapping("/data-services")
    public List<Mapping> getDataServices(@RequestParam String dir, @RequestParam String env){
        List<Mapping> services = importDataService.getDataServices(dir, env);
        if (CollectionUtils.isEmpty(services)){
            return null;
        }
        return services;
    }

    @GetMapping("/groovy-services")
    public List<Mapping> getGroovyServices(@RequestParam String dir, @RequestParam String env){
        List<Mapping> services = importDataService.getGroovyServices(dir, env);
        if (CollectionUtils.isEmpty(services)){
            return null;
        }
        return services;
    }

    @RequestMapping("/init-data")
    public Result importData(@RequestBody List<String> services, @RequestParam String dir, @RequestParam String env){
        importDataService.importData(services,dir,env);
        return Result.ok();
    }

    @RequestMapping("/update-groovy")
    public Result updateGroovy(@RequestBody List<String> services, @RequestParam String dir, @RequestParam String env){
        importDataService.updateGroovy(services,dir,env);
        return Result.ok();
    }
}
