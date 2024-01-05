package com.ruoyi.qianclouds.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import com.ruoyi.qianclouds.domain.LoginSessionEntity;
import com.ruoyi.qianclouds.domain.OrderEntity;
import com.ruoyi.qianclouds.mapper.LoginSessionMapper;
import com.ruoyi.qianclouds.mapper.OrderMapper;
import com.ruoyi.qianclouds.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MainService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginSessionMapper sessionMapper;
    @Autowired
    private OrderMapper orderMapper;

    public HashMap<String, Object> collect(){

        Date target = new Date();
        HashMap<String, Object> map = statistic(target);

        // 本月收入
        String firstDayOfMonth = DateUtil.format(DateUtil.beginOfMonth(DateUtil.date()), "yyyy-MM-dd HH:mm:ss");
        List<OrderEntity> monthOrderList = orderMapper.timeCreated(firstDayOfMonth);
        List<OrderEntity> monthOrderSuccess = monthOrderList.stream().filter(orderEntity -> {
            return  "Alipay".equals(orderEntity.getPay_platform()) &&
                    "Success".equals(orderEntity.getPay_status());
        }).collect(Collectors.toList());
        double monthMoney = 0;
        for (OrderEntity orderSuccess : monthOrderSuccess) {
            monthMoney += Double.valueOf(orderSuccess.getPay_money());
        }
        map.put("monthMoney", monthMoney);
        map.put("statistic30", history());
        return map;
    }

    private List<String> getUserIds(Date target ,int offset){
        DateTime lastDay = DateUtil.offsetDay(target, offset);
        List<String> users = userMapper.newUsers(DateUtil.formatDateTime(DateUtil.beginOfDay(lastDay)), DateUtil.formatDateTime(DateUtil.endOfDay(lastDay)));
        return users;
    }

    private String history(){
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < 30 ; i++) {
            DateTime day = DateUtil.offsetDay(new Date(), i * -1);
            builder.append("<br>");
            builder.append("日期：").append(DateUtil.formatDateTime(day)).append("<br>");
            HashMap<String, Object> map = statistic(day);
            builder.append("日活===").append(map.get("activeToday")).append("|||||");
            builder.append("次留===").append(map.get("day2Count")).append("|||||");
            builder.append("7日留存===").append(map.get("day7Count")).append("|||||");
            builder.append("月活===").append(map.get("day30Count")).append("|||||");
            builder.append("新用户===").append(map.get("newUsers")).append("|||||");
            builder.append("订单创建量===").append(map.get("todayOrderCreated")).append("|||||");
            builder.append("订单成交量===").append(map.get("todayOrderSuccessCount")).append("|||||");
            builder.append("订单成交额===").append(map.get("todayMoney")).append("|||||");
        }
        return builder.toString();
    }

    private HashMap<String, Object> statistic(Date target){
        HashMap<String, Object> map = new HashMap<>();
        // 今日活跃量
        long time = DateUtil.beginOfDay(target).getTime();
        List<String> todayUsers = sessionMapper.todayActive(time);
        int activeToday = todayUsers.size();
        map.put("activeToday", activeToday);
        // 次留量
        List<String> lastUsers = getUserIds(target,-1);
        List<String> day2List = new ArrayList<>(todayUsers);
        day2List.retainAll(lastUsers);
        map.put("day2Count", day2List.size());
        // 7日留存
        List<String> day7CompluteList = new ArrayList<>(todayUsers);
        List<String> day3List = getUserIds(target,-2);
        List<String> day4List = getUserIds(target,-3);
        List<String> day5List = getUserIds(target,-4);
        List<String> day6List = getUserIds(target,-5);
        List<String> day7List = getUserIds(target,-6);
        day7CompluteList.retainAll(day2List);
        day7CompluteList.retainAll(day3List);
        day7CompluteList.retainAll(day4List);
        day7CompluteList.retainAll(day5List);
        day7CompluteList.retainAll(day6List);
        day7CompluteList.retainAll(day7List);
        map.put("day7Count", day7CompluteList.size());
        // 月活跃量
        DateTime day30before = DateUtil.offsetDay(target, -30);
        long day30Time = DateUtil.beginOfDay(day30before).getTime();
        int day30Count = sessionMapper.todayActive(day30Time).size();
        map.put("day30Count", day30Count);

        String todayStart = DateUtil.formatDateTime(DateUtil.beginOfDay(target));
        // 今日新增用户量
        int newUsers = userMapper.todayNewUsers(todayStart);
        map.put("newUsers", newUsers);
        // 用户总量
        int userTotal = userMapper.all();
        map.put("userTotal", userTotal);

        // 今日订单生成量
        List<OrderEntity> list = orderMapper.timeCreated(todayStart);
        int todayOrderCreated = list.size();
        map.put("todayOrderCreated", todayOrderCreated);
        // 今日订单成交量
        List<OrderEntity> todayOrderSuccess = list.stream().filter(orderEntity -> {
            return  "Alipay".equals(orderEntity.getPay_platform()) &&
                    "Success".equals(orderEntity.getPay_status());
        }).collect(Collectors.toList());
        int todayOrderSuccessCount = todayOrderSuccess.size();
        map.put("todayOrderSuccessCount", todayOrderSuccessCount);
        // 今日订单成交额
        double todayMoney = 0;
        for (OrderEntity orderSuccess : todayOrderSuccess) {
            todayMoney += Double.valueOf(orderSuccess.getPay_money());
        }
        map.put("todayMoney", todayMoney);
        return map;
    }
}
