package com.ruoyi.qianclouds.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ruoyi.qianclouds.domain.CloudEntity;
import com.ruoyi.qianclouds.domain.DetailDTO;
import com.ruoyi.qianclouds.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private MainService mainService;

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
