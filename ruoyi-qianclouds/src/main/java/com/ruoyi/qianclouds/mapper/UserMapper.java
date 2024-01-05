package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface UserMapper {


    @Select("select count(1) from user where create_time > #{timeStr}")
    int todayNewUsers(String timeStr);

    @Select("select count(1) from user")
    int all();
}
