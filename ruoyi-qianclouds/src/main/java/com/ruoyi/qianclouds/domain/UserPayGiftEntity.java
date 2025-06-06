package com.ruoyi.qianclouds.domain;

import lombok.Data;

@Data
public class UserPayGiftEntity extends BaseGiftEntity{

    /**
     *  订单数据
     */
    private String orderId;

    /**
     *  套餐类型
     */
    private String giftId;

    /**
     *  套餐生效开始时间
     */
    private String startTime;

    /**
     *  套餐生效结束时间
     */
    private String endTime;
}
