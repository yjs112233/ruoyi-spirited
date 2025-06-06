package com.ruoyi.web.controller.qianclouds;

import com.ruoyi.qianclouds.service.DriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qianclouds")
public class DriveController {

    @Autowired
    private DriveService driveService;

    @RequestMapping("drive/statisticByType")
    public List<Map<String, Object>> statisticByType(Integer days){
        return driveService.statisticByType(days);
    }

    @RequestMapping("drive/statisticVipByType")
    public List<Map<String, Object>> statisticVipByType(Integer days){
        return driveService.statisticVipByType(days);
    }
}
