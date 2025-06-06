package com.ruoyi.qianclouds.service;

import com.ruoyi.qianclouds.domain.UserPayGiftEntity;
import com.ruoyi.qianclouds.mapper.GiftMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GifyService {

    @Autowired
    private GiftMapper giftMapper;

    public boolean isV(String userId){
        List<UserPayGiftEntity> all = giftMapper.get(userId);
        long current = System.currentTimeMillis();
        // all
        for (UserPayGiftEntity pay : all) {
            if (Long.valueOf(pay.getEndTime()) > current){
                return true;
            }
        }
        return false;
    }
}
