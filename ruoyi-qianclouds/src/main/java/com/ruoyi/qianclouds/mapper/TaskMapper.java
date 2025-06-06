package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.qianclouds.domain.DetailDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface TaskMapper {

    @Select("select count(distinct user_id) from transfer where create_time >= #{param1} and create_time <= #{param2}")
    Integer getValidUser(String startTime, String endTime);

    @Select("select s.transfer_id, s.create_time , s.id as sid, s.source_drive_id as `from`, s.destination_drive_id as `to`, IFNULL(d.sub_status, \n" +
            "CASE\n" +
            "            WHEN s.is_deleted = 1 THEN 'done_canceled'\n" +
            "\t\t\t\t\t\tELSE s.status \n" +
            "        END\n" +
            ") as `status`, d.total_file as count, d.total_size as size from transfer_session s left join  transfer_session_done d on s.session_done_id = d.id where s.user_id = #{userId} order by s.create_time desc")
    List<DetailDTO.Task> getAllTask(String userId);

    @Select("select count(1) from transfer where create_time >= #{startTime} and create_time <= #{endTime}")
    Integer count(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
