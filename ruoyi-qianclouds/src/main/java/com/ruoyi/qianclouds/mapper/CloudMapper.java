package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.qianclouds.domain.CloudEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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

    @Select("select count(drive_type) as count, drive_type as driveType from drive_token where create_time >= #{startTime} and create_time <= #{endTime} group by drive_type order by count desc")
    List<Map<String, Object>> listDrives(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select count(drive_type) as count, drive_type as driveType from drive_token where user_id in (select user_id from user_order where pay_status = 'Success' and create_time >= #{startTime} and create_time <= #{endTime}) group by drive_type order by count desc")
    List<Map<String, Object>> listDrivesInVip(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select drive_type from drive_token where id = #{sourceDriveId} or id = #{destDriveId}")
    List<String> selectTypeById(@Param("sourceDriveId") String source, @Param("destDriveId") String dest);
}
