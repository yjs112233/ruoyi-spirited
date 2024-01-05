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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        HashMap<String, Object> map = new HashMap<>();

        // 今日活跃量
        long time = DateUtil.beginOfDay(new Date()).getTime();
        int activeToday = sessionMapper.todayActive(time).size();
        map.put("activeToday", activeToday);

        // 30日活跃量
        DateTime day30before = DateUtil.offsetDay(new Date(), -30);
        long day30Time = DateUtil.beginOfDay(day30before).getTime();
        List<HashMap<String, Integer>> day30Count = sessionMapper.day30Active(day30Time);

        StringBuilder builder = new StringBuilder();
        for (int i = day30Count.size() - 1; i >= 0 ; i--) {
            HashMap<String, Integer> m = day30Count.get(i);
            builder.append("<br>").append(m.get("date_format")).append("---------").append(m.get("unique_user_count"));
        }
        map.put("day30Count", builder.toString());

        String todayStart = DateUtil.formatDateTime(DateUtil.beginOfDay(new Date()));
        // 今日新增用户量
        int newUsers = userMapper.todayNewUsers(todayStart);
        map.put("newUsers", newUsers);

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

        return map;
    }
}
