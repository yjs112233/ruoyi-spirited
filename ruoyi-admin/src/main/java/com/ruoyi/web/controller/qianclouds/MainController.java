package com.ruoyi.web.controller.qianclouds;

import com.ruoyi.qianclouds.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;


@RestController
@RequestMapping("/qianclouds")
public class MainController {

    @Autowired
    MainService mainService;

    @RequestMapping("collect")
    public HashMap<String, Object> collect(){

        return mainService.collect();
    }

}
