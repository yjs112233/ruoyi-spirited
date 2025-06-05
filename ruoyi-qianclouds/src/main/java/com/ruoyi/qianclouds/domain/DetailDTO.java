package com.ruoyi.qianclouds.domain;

import lombok.Data;

import java.util.List;

@Data
public class DetailDTO {

    private String date;

    private String userId;

    private String account;

    private Boolean isNew;

    private List<String> drives;

    private List<Task> tasks;

    private String orderMoney;

    @Data
    public static class Task{

        private String from;

        private String to;

        private String status;

        private String count;

        private String size;
    }
}
