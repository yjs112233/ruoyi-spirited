package com.ruoyi.qianclouds.domain;

import lombok.Data;

@Data
public class BasicEntity {

    private String id;

    /**
     *  默认false
     */
    private Boolean isDeleted = Boolean.FALSE;
}
