package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.qianclouds.domain.LoginSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface LoginSessionMapper {

    @Select("select DISTINCT user_id from login_session where login_time >= #{param1} and login_time <= #{param2}")
    List<String> todayActive(long startTime, long endTime);

    @Select("SELECT DATE(FROM_UNIXTIME(login_time / 1000)) AS date_format,    \n" +
            "       COUNT(DISTINCT user_id) AS unique_user_count    \n" +
            "FROM login_session    \n" +
            "WHERE login_time >= #{time} \n" +
            "GROUP BY date_format    \n" +
            "ORDER BY date_format; ")
    List<HashMap<String, Integer>> day30Active(long time);
}
