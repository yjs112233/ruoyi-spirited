package com.ruoyi.qianclouds.domain;
import lombok.Data;

import java.sql.Timestamp;

@Data
public abstract class BaseGiftEntity {

    private String id;

    /**
     *  套餐类型
     */
    private String type;

    private String userId;

    /**
     *  当前总量
     */
    private Long totalSize;

    /**
     *  已用量
     */
    private Long usedSize;

    private Timestamp createTime;

    private Timestamp updateTime;
}
