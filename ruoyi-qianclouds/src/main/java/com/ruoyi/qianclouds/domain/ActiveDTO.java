package com.ruoyi.qianclouds.domain;

import cn.hutool.core.date.DateTime;
import lombok.Data;

@Data
public class ActiveDTO {

    private String dateTime;

    private String activeToday;

    private String day2Count;

    private String day7Count;

    private String day30Count;

    private String amount;
}
