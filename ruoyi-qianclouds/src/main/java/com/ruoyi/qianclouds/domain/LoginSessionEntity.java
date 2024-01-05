package com.ruoyi.qianclouds.domain;

import lombok.Data;

@Data
public class LoginSessionEntity {

    private String id;

    private String userId;

    /**
     *  登录时间（时间戳）
     */
    private String loginTime;

    private String ip;

    private String timezone;

    private String city;
}
