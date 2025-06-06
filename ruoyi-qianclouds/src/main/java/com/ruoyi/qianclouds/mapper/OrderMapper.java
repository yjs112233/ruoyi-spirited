package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.qianclouds.domain.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface OrderMapper {


    @Select("select * from user_order where create_time > #{param1} and create_time < #{param2} and (pay_platform is null or pay_platform != 'System')")
    List<OrderEntity> timeCreated(String startTime, String endTime);

    @Select("select * from user_order where pay_status = 'Success' and pay_platform != 'System'")
    List<OrderEntity> allSuccess();

    @Select("select * from user_order where pay_status = 'Success' and user_id = #{userId}")
    List<OrderEntity> getUserOrder(String userId);
}
