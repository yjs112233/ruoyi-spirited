package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.qianclouds.domain.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface UserMapper {

    @Select("select count(1) from user where create_time > #{timeStr}")
    int todayNewUsers(String timeStr);

    @Select("select id from user where create_time >= #{param1} and create_time <= #{param2}")
    List<String> newUsers(String startTime, String endTime);

    @Select("select id from user where create_time >= #{param1} and create_time <= #{param2} and id not in (select user_id from user_order where pay_status = 'Success' and create_time >= #{param1} and create_time <= #{param2})")
    List<String> newUsersNotVip(String startTime, String endTime);

    @Select("select count(1) from user")
    Integer all();

    @Select("select * from user where id in ${ids}")
    List<UserEntity> searchAccount(String ids);

}
