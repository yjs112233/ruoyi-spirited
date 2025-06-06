package com.ruoyi.qianclouds.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ruoyi.qianclouds.domain.DateDTO;
import com.ruoyi.qianclouds.mapper.CloudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DriveService {
    @Autowired
    private CloudMapper cloudMapper;

    public List<Map<String, Object>> statisticByType(Integer days){
        DateDTO dateDTO = build(days);
        return cloudMapper.listDrives(dateDTO.getStartTime(), dateDTO.getEndTime());
    }

    public List<Map<String, Object>> statisticVipByType(Integer days){
        DateDTO dateDTO = build(days);
        return cloudMapper.listDrivesInVip(dateDTO.getStartTime(), dateDTO.getEndTime());
    }

    public static DateDTO build(Integer days){
        String startTime = null;
        String endTime = DateUtil.formatDateTime(new Date());
        if (days == null || days < 0){
            startTime =  "2024-01-01 00:00:00";
        }else {
            DateTime dateTime = DateUtil.offsetDay(new Date(), days * -1);
            startTime = DateUtil.formatDateTime(dateTime);
        }
        return new DateDTO(startTime, endTime);
    }


}
