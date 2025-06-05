package com.ruoyi.web.controller.qianclouds;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ruoyi.qianclouds.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/qianclouds")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping("task/newUser/{offser}")
    public Map<String, Object> info(@PathVariable Integer offser){
        return taskService.newUserTasks(offser);
    }
}
