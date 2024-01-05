package com.ruoyi.qianclouds.domain;

import lombok.Data;

@Data
public class OrderEntity {

    private String id;

    /**用户ID*/
    private String userId;

    /**用户昵称*/
    private String nickname;

    /**套餐ID*/
    private String giftId;

    /**套餐名称*/
    private String giftName;

    /**套餐编号*/
    private String giftNumber;

    /**套餐类型*/
    private String giftType;

    /**增加时长*/
    private Integer giftAddTime;

    /**增加流量*/
    private Long giftAddSize;

    /**套餐生效开始时间*/
    private String giftStartTime;

    /**套餐生效结束时间*/
    private String giftEndTime;

    /**支付状态 @See {pine.gift.enums.PayStatusEnum}*/
    private String pay_status;

    /**支付金额*/
    private String pay_money;

    /**开始支付时间*/
    private String payCreateTime;

    /**支付时间*/
    private String payTime;

    /**折扣码*/
    private String discountCode;

    /**支付平台 @See {pine.gift.enums.PayPlatformEnum}*/
    private String pay_platform;

    /**支付平台元数据*/
    private String payOdata;

    /**支付平台订单id*/
    private String payOrderId;

    /**支付平台订单源数据*/
    private String payOrderOdata;

    /**订单创建时间*/
    private String createTime;

    /**订单结束时间*/
    private String finishedTime;
}
