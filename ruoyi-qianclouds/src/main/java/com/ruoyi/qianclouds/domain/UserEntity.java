package com.ruoyi.qianclouds.domain;

import lombok.Data;

@Data
public class UserEntity extends BasicEntity {

    private String id;

    private String nickname;

    /**
     *  账号
     */
    private String account;

    private String password;

    /**
     *  登录类型{@see pine.user.enums.LoginTypeEnum}
     */
    private String loginType;

    /**
     *  绑定手机号
     */
    private String phone;

    private String salt;

    /**
     *  头像
     */
    private String headImage;

    private String createTime;

}
