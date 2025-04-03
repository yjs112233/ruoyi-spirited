package com.ruoyi.qianclouds.domain;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class TransferSessionEntity extends BasicEntity {

    private String id;

    private String userId;

    private String name;

    private String sourceDriveId;

    private String destinationDriveId;

    private String transferId;

    /**
     *  执行策略
     */
    private String action;

    /**
     *  定时数据
     */
    private String timer;

    private String startTime;

    private String endTime;

    /**
     *  任务状态 {@see pine.task.common.enums.TaskStatusEnum}
     */
    private String status;

    /**
     *  完成结果
     */
    private String sessionDoneId;

    /**
     * 创建时间
     */
    private Timestamp createTime;

}
