package com.ruoyi.qianclouds.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.qianclouds.domain.CloudEntity;
import com.ruoyi.qianclouds.domain.DateDTO;
import com.ruoyi.qianclouds.domain.DetailDTO;
import com.ruoyi.qianclouds.mapper.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class TaskService {

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
    @Autowired
    private GifyService gifyService;
    @Autowired
    private MainService mainService;
    @Value("${test.user}")
    private String testUser;
    @Autowired
    private ThreadPoolTaskExecutor qiancloudsPool;

    public Map<String, Long> statisticFromToByCount(Integer days, Boolean isV){
        DateDTO dateDTO = DriveService.build(days);
        List<Map<String, Object>> list = transferSessionMapper.statisticFromTo(dateDTO.getStartTime(), dateDTO.getEndTime());
        List<Result> resultList = getResult(list);

        if (isV != null){
            resultList = resultList.stream().filter(result -> result.getIsVip() == isV).collect(Collectors.toList());
        }

        Map<String, Long> routeCountMap = resultList.stream()
                .collect(Collectors.groupingBy(
                        Result::getRoute,  // 按route字段分组
                        Collectors.counting()  // 统计每组的数量
                ));
        Map<String, Long> sortedMap = routeCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return sortedMap;
    }

    public Map<String, Long> statisticFromToByUser(Integer days, Boolean isV){
        DateDTO dateDTO = DriveService.build(days);
        List<Map<String, Object>> list = transferSessionMapper.statisticFromTo(dateDTO.getStartTime(), dateDTO.getEndTime());
        List<Result> resultList = getResult(list);

        if (isV != null){
            resultList = resultList.stream().filter(result -> result.getIsVip() == isV).collect(Collectors.toList());
        }

        List<Result> ditinctList = new ArrayList<>();
        for (Result result : resultList) {
            boolean isExist = ditinctList.stream().anyMatch(r -> r.getRoute().equals(result.getRoute()) && r.getUserId().equals(result.getUserId()));
            if (!isExist){
                ditinctList.add(result);
            }
        }

        Map<String, Long> routeCountMap = ditinctList.stream()
                .collect(Collectors.groupingBy(
                        Result::getRoute,  // 按route字段分组
                        Collectors.counting()  // 统计每组的数量
                ));
        Map<String, Long> sortedMap = routeCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return sortedMap;
    }


    private List<Result> getResult(List<Map<String, Object>> list){
        List<Future> futureList = new ArrayList<>();
        List<Result> resultList = new Vector<>();
        for (Map<String, Object> map : list) {
            String userId = (String) map.get("user_id");
            if (Arrays.asList(testUser.split(",")).contains(userId)){
                continue;
            }
            Future future = qiancloudsPool.submit(() ->{
                String sourceDriveId = (String) map.get("source_drive_id");
                String destDriveId = (String) map.get("destination_drive_id");
                List<String> types = cloudMapper.selectTypeById(sourceDriveId, destDriveId);
                String route = types.get(0) + "-" +types.get(types.size() - 1);
                Boolean isV = gifyService.isV(userId);
                Result result = new Result(route, userId, isV);
                resultList.add(result);
            });
            futureList.add(future);
        }
        for (Future future : futureList) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

    @Data
    @AllArgsConstructor
    private static class Result{
        String route;
        String userId;
        Boolean isVip;
    }

    /**
     *
     * @param offset
     * @return
     */
    public Map<String, Object> newUserTasks(Integer offset){
        Map<String, Object> map = new HashMap<>();
        List<DetailDTO> result = new ArrayList<>();
        DateTime endTime = DateUtil.endOfDay(new Date());
        DateTime startTime = DateUtil.offsetDay(endTime, offset);
        List<String> newUsersNotVipIds = userMapper.newUsersNotVip(startTime.toDateStr(), endTime.toDateStr());
        List<CloudEntity> userClouds = cloudMapper.loadValidUser(String.join(",", newUsersNotVipIds));
        Map<String, List<CloudEntity>> userCloudMap = userClouds.stream().collect(Collectors.groupingBy(CloudEntity::getUserId));
        AtomicInteger singleCloudUserCount = new AtomicInteger();
        AtomicInteger multiCloudUserCount = new AtomicInteger();
        userCloudMap.forEach((key, value) -> {
            if (value.size() == 1) {
                singleCloudUserCount.getAndIncrement();
            }else if (value.size() > 1){
                multiCloudUserCount.getAndIncrement();
            }
        });

        int validCloudUserSize = userClouds.stream().map(CloudEntity::getUserId).distinct().collect(Collectors.toList()).size();
        int taskUserSize = 0;
        for (int i = offset; i <0 ; i++) {
            DateTime target = DateUtil.offsetDay(endTime, i);
            List<DetailDTO> dtos = mainService.detailsList(target);
            for (DetailDTO dto : dtos) {
                if (newUsersNotVipIds.contains(dto.getUserId()) && dto.getIsNew()){
                    if (!dto.getTasks().isEmpty()){
                        taskUserSize++;
                    }
                    dto.setDate(target.toDateStr());
                    result.add(dto);
                }
            }
        }
        map.put("data", result);

        map.put("statistic", String.format("未下单的新用户数：%s, 云盘用户数：%s, 多云盘用户数：%s, 任务创建用户数：%s", newUsersNotVipIds.size(), singleCloudUserCount.get() + multiCloudUserCount.get(), multiCloudUserCount.get(), taskUserSize));
        return map;
    }
}
