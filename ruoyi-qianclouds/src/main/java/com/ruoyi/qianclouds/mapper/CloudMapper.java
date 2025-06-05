package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.qianclouds.domain.CloudEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface CloudMapper {

    @Select("select count(distinct user_id) from drive_token where create_time >= #{param1} and create_time <= #{param2}")
    Integer getValidUser(String startTime, String endTime);

    @Select("select id, drive_type as driveType,user_id as userId from drive_token where user_id in (${userIds})")
    List<CloudEntity> loadValidUser(String userIds);

    @Select("select id, drive_type as driveType from drive_token where user_id = #{userId}")
    List<CloudEntity> getUserDrives(String userId);
}
