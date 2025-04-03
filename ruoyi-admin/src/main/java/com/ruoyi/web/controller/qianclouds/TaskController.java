package com.ruoyi.web.controller.qianclouds;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ruoyi.qianclouds.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping("info/{date}")
    public void info(@PathVariable String date){
        DateTime target = !date.equals("0") ? DateUtil.parse(date) : DateUtil.date();
        taskService.newUserTasks(target);
    }
}
