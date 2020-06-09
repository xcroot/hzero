package org.hzero.generator.controller;


import org.hzero.generator.service.ServiceUpgradeService;
import org.hzero.generator.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author liguo.wnag
 */
@RestController
@RequestMapping("/sys/service-upgrade")
public class ServiceUpgradeController {

    @Autowired
    private ServiceUpgradeService serviceUpgradeService;

    @GetMapping("/list")
    public List<String> list(){
        return serviceUpgradeService.listServiceUpgrade();
    }

    @PostMapping("/upgrade")
    public Result upgrade(@RequestParam("version") String version){
        serviceUpgradeService.serviceUpgrade(version);
        return Result.ok();
    }

    @PostMapping("/data-upgrade")
    public Result dataUpdate(@RequestParam("version") String version){
        serviceUpgradeService.dataUpdate(version);
        return Result.ok();
    }
}

