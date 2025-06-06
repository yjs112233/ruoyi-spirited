package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.qianclouds.domain.UserPayGiftEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface GiftMapper {

    @Select("select start_time as startTime, end_time as endTime from pay_gift where user_id = #{userId} union select start_time as startTime, end_time as endTime from once_gift where user_id = #{userId}")
    List<UserPayGiftEntity> get(String userId);
}
