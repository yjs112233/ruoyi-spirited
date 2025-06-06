package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface TransferSessionMapper {

    @Select("select count(1) from transfer_session")
    Integer sessionTotal();

    @Select("select count(distinct transfer_id) from transfer_session")
    Integer transferTotal();

    @Select("select user_id, source_drive_id, destination_drive_id from transfer_session where create_time >= #{startTime} and create_time <= #{endTime} group by transfer_id")
    List<Map<String, Object>> statisticFromTo(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
