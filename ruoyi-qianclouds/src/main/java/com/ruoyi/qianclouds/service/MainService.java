package com.ruoyi.qianclouds.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrBuilder;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qianclouds.domain.*;
import com.ruoyi.qianclouds.mapper.*;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class MainService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CloudMapper cloudMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private LoginSessionMapper sessionMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TransferSessionMapper transferSessionMapper;

    public HashMap<String, Object> collect(){

        Date target = new Date();
        HashMap<String, Object> map = statistic(target);
        map.put("statistic30", history());
        return map;
    }

    private List<String> getUserIds(Date target ,int offset){
        DateTime lastDay = DateUtil.offsetDay(target, offset);
        List<String> users = userMapper.newUsers(DateUtil.formatDateTime(DateUtil.beginOfDay(lastDay)), DateUtil.formatDateTime(DateUtil.endOfDay(lastDay)));
        return users;
    }

    @SneakyThrows
    public List<ActiveDTO> active(int count){
        DateTime start = null;
        DateTime end = null;
        if (count > 0){
            DateTime tatget = DateUtil.offsetMonth(DateUtil.date(), count * -1);
            start = DateUtil.beginOfMonth(tatget);
            end = DateUtil.endOfMonth(tatget);
        }else {
            start = DateUtil.beginOfMonth(new Date());
            end = DateUtil.date();
        }

        List<DateTime> list = new ArrayList<>();
        DateTime index = start;
        while (!index.isAfter(end)){
            list.add(index);
            index = index.offsetNew(DateField.DAY_OF_MONTH, 1);
        }

        List<ActiveDTO> result = new Vector<>();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        for (DateTime dateTime : list) {
            executorService.execute(() ->{
                ActiveDTO activeDTO = BeanUtil.mapToBean(statistic(dateTime), ActiveDTO.class, true);
                activeDTO.setDateTime(dateTime.toDateStr());
                result.add(activeDTO);
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        return result.stream().sorted((a,b) ->{
            return (int)(DateUtil.parse(a.getDateTime()).getTime() - DateUtil.parse(b.getDateTime()).getTime());
        }).collect(Collectors.toList());
    }

    private String history(){
        StringBuilder builder = new StringBuilder();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<HashMap<String, Object>>> list = new ArrayList<>();
        for (int k = 1; k < 30 ; k++) {
            DateTime day = DateUtil.offsetDay(new Date(), k * -1);
            list.add(executorService.submit(() -> statistic(day)));
        }
        for (int i = 1; i < 30 ; i++) {
            DateTime day = DateUtil.offsetDay(new Date(), i * -1);
            builder.append("<br>");
            builder.append("<br>");
            builder.append("日期：").append(DateUtil.formatDate(day)).append("===========");
            HashMap<String, Object> map = null;
            try {
                map = list.get(i - 1).get();
            } catch (Exception e) {
                builder.append("程序报错").append("=====");
            }
            builder.append("日活===").append(map.get("activeToday")).append("===========");
            builder.append("次留===").append(map.get("day2Count")).append("===========");
            builder.append("7日留存===").append(map.get("day7Count")).append("===========");
            builder.append("月活===").append(map.get("day30Count")).append("===========");
            builder.append("新用户===").append(map.get("newUsers")).append("===========");
            builder.append("订单创建量===").append(map.get("todayOrderCreated")).append("===========");
            builder.append("订单成交量===").append(map.get("todayOrderSuccessCount")).append("===========");
            builder.append("订单成交额===").append(map.get("todayMoney"));
        }
        return builder.toString();
    }

    private HashMap<String, Object> statistic(Date target){
        HashMap<String, Object> map = new HashMap<>();
        // 今日活跃量
        long startTime = DateUtil.beginOfDay(target).getTime();
        long endTime = DateUtil.endOfDay(target).getTime();
        List<String> todayUsers = sessionMapper.todayActive(startTime, endTime);
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
        long day30StartTime = DateUtil.beginOfDay(day30before).getTime();
        long targetEndTime = DateUtil.endOfDay(target).getTime();
        int day30Count = sessionMapper.todayActive(day30StartTime, targetEndTime).size();
        map.put("day30Count", day30Count);

        String startTimeStr = DateUtil.formatDateTime(DateUtil.beginOfDay(target));
        String endTimeStr = DateUtil.formatDateTime(DateUtil.endOfDay(target));
        List<OrderEntity> list = orderMapper.timeCreated(startTimeStr, endTimeStr);
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
        map.put("amount", todayMoney);
        return map;
    }

    public List<DetailDTO> detailsList(Date target){
        List<DetailDTO> result = new ArrayList<>();
        List<String> todayUserIds = sessionMapper.todayActive(DateUtil.beginOfDay(target).getTime(), DateUtil.endOfDay(target).getTime());
        List<UserEntity> userEntities = userMapper.searchAccount(String.format("(%s)", String.join(",", todayUserIds)));
        String startTimeStr = DateUtil.formatDateTime(DateUtil.beginOfDay(target));
        String endTimeStr = DateUtil.formatDateTime(DateUtil.endOfDay(target));
        List<String> newUserIds = userMapper.newUsers(startTimeStr, endTimeStr);
        for (String userId : todayUserIds) {
            DetailDTO dto = new DetailDTO();
            dto.setUserId(userId);
            dto.setIsNew(newUserIds.contains(userId));
            UserEntity entity = userEntities.stream().filter(userEntity -> userEntity.getId().equals(userId)).findAny().get();
            if (entity.getAccount().startsWith("test")){
                continue;
            }
            dto.setAccount(entity.getAccount());
            List<CloudEntity> drives = cloudMapper.getUserDrives(userId);
            dto.setDrives(drives.stream().map(CloudEntity::getDriveType).collect(Collectors.toList()));
            List<OrderEntity> orders = orderMapper.getUserOrder(userId);
            List<String> moneys=  orders.stream().map(OrderEntity::getPay_money).collect(Collectors.toList());
            dto.setOrderMoney(String.join(",", moneys));
            List<DetailDTO.Task> tasks = taskMapper.getAllTask(userId);
            for (DetailDTO.Task task : tasks) {
                CloudEntity cloud1 = drives.stream().filter(cloudEntity -> cloudEntity.getId().equals(task.getFrom())).findAny().get();
                task.setFrom(cloud1.getDriveType());
                CloudEntity cloud2 = drives.stream().filter(cloudEntity -> cloudEntity.getId().equals(task.getTo())).findAny().get();
                task.setTo(cloud2.getDriveType());
                if (StringUtils.isNotEmpty(task.getSize())){
                    task.setSize(convert(Double.valueOf(task.getSize())));
                }

            }
            dto.setTasks(tasks);
            result.add(dto);
        }
        return result;
    }

    public HashMap<String, Object> users(Date target){
        HashMap<String, Object> map = new HashMap<>();
        String startTimeStr = DateUtil.formatDateTime(DateUtil.beginOfDay(target));
        String endTimeStr = DateUtil.formatDateTime(DateUtil.endOfDay(target));
        // 今日活跃量
        List<String> todayUsers = sessionMapper.todayActive(DateUtil.beginOfDay(target).getTime(), DateUtil.endOfDay(target).getTime());
        int activeToday = todayUsers.size();
        map.put("activeToday", activeToday);
        // 今日新增用户量
        int newUsers = userMapper.newUsers(startTimeStr, endTimeStr).size();
        map.put("newUsers", newUsers);
        // 今日云添加有效人数
        int cloudUsers = cloudMapper.getValidUser(startTimeStr, endTimeStr);
        map.put("cloudUsers", cloudUsers);
        // 今天任务创建有效人数
        int taskUsers = taskMapper.getValidUser(startTimeStr, endTimeStr);
        map.put("taskUsers", taskUsers);
        // 今日订单生成量
        List<OrderEntity> list = orderMapper.timeCreated(startTimeStr, endTimeStr);
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
        // 本月
        String firstDayOfMonth = DateUtil.format(DateUtil.beginOfMonth(target), "yyyy-MM-dd HH:mm:ss");
        String endDayOfMonth = DateUtil.format(DateUtil.endOfMonth(target), "yyyy-MM-dd HH:mm:ss");
        List<OrderEntity> monthOrderList = orderMapper.timeCreated(firstDayOfMonth, endDayOfMonth);
        List<OrderEntity> monthOrderSuccess = monthOrderList.stream().filter(orderEntity -> {
            return  "Alipay".equals(orderEntity.getPay_platform()) &&
                    "Success".equals(orderEntity.getPay_status());
        }).collect(Collectors.toList());
        double monthMoney = 0;
        for (OrderEntity orderSuccess : monthOrderSuccess) {
            monthMoney += Double.valueOf(orderSuccess.getPay_money());
        }
        map.put("monthMoney", (int) monthMoney);

        long startTime = DateUtil.beginOfMonth(target).getTime();
        long endTime = DateUtil.endOfMonth(target).getTime();
        int monthActives = sessionMapper.todayActive(startTime, endTime).size();
        DecimalFormat df = new DecimalFormat("#.00");
        String formattedResult = df.format((monthOrderSuccess.size() / (monthActives * 1.00) * 100));
        map.put("monthActiveRate",formattedResult);
        return map;
    }

    public Map<String, Object> total(){
        Map<String, Object> map = new HashMap<>();
        // 总用户
        int userTotal = userMapper.all();
        map.put("userTotal", userTotal);
        // 总交易e
        List<OrderEntity> orders = orderMapper.allSuccess();
        double all = 0;
        for (OrderEntity order : orders) {
            all += Double.valueOf(order.getPay_money());
        }
        map.put("amountTotal", (int)all);
        // 总任数
        int tasktotal = transferSessionMapper.transferTotal();
        map.put("taskTotal", tasktotal);
        return map;
    }


    public static final double KB = 1024.0;
    public static String convert(double fileLength) {
        if (fileLength / KB >= 1 && fileLength / KB <= 1024) {
            double len = fileLength / KB;
            len = Math.round(len * 100.00) / 100.00;
            return len + "KB";
        } else if (fileLength / KB / KB >= 1 && fileLength / KB / KB <= 1024) {
            double len = fileLength / KB / KB;
            len = Math.round(len * 100.00) / 100.00;
            return len + "M";
        } else if (fileLength >= 0 && fileLength <= 1024) {
            return fileLength + "b";
        } else if (fileLength / KB / KB / KB >= 1 && fileLength / KB / KB / KB <= 1024) {
            double len = fileLength / KB / KB / KB;
            len = Math.round(len * 100.00) / 100.00;
            return len + "G";
        } else if (fileLength / KB / KB / KB / KB >= 1 && fileLength / KB / KB / KB / KB <= 1024) {
            double len = fileLength / KB / KB / KB / KB;
            len = Math.round(len * 100.00) / 100.00;
            return len + "T";
        }
        return fileLength + "B";
    }
}
