package com.ruoyi.web.controller.qianclouds;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ruoyi.qianclouds.domain.ActiveDTO;
import com.ruoyi.qianclouds.domain.DetailDTO;
import com.ruoyi.qianclouds.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/qianclouds")
public class MainController {

    @Autowired
    MainService mainService;

    @RequestMapping("collect")
    public HashMap<String, Object> collect(){
        return mainService.collect();
    }

    @RequestMapping("info/{date}")
    public HashMap<String, Object> info(@PathVariable String date){
        DateTime target = !date.equals("0") ? DateUtil.parse(date) : DateUtil.date();
        return mainService.users(target);
    }

    @RequestMapping("details/list/{date}")
    public List<DetailDTO> detailsList(@PathVariable String date){
        DateTime target = !date.equals("0") ? DateUtil.parse(date) : DateUtil.date();
        return mainService.detailsList(target);
    }

    @RequestMapping("active/{count}")
    public Map<String, List<String>> active(@PathVariable int count){
        List<ActiveDTO> list = mainService.active(count);
        List<String> dates = list.stream().map(ActiveDTO::getDateTime).collect(Collectors.toList());
        List<String> activeToday = list.stream().map(ActiveDTO::getActiveToday).collect(Collectors.toList());
        List<String> day2Count = list.stream().map(ActiveDTO::getDay2Count).collect(Collectors.toList());
        List<String> day7Count = list.stream().map(ActiveDTO::getDay7Count).collect(Collectors.toList());
        List<String> day30Count = list.stream().map(ActiveDTO::getDay30Count).collect(Collectors.toList());
        List<String> amount = list.stream().map(ActiveDTO::getAmount).collect(Collectors.toList());
        Map<String, List<String>> map = new HashMap<>();
        map.put("dates", dates);
        map.put("activeToday", activeToday);
        map.put("day2Count", day2Count);
        map.put("day7Count", day7Count);
        map.put("day30Count", day30Count);
        map.put("amount", amount);
        return map;
    }

    @RequestMapping("total")
    public Map<String, Object> total(){
      return mainService.total();
    }
}
