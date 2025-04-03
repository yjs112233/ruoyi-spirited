package com.ruoyi.qianclouds.mapper;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
@DataSource(value = DataSourceType.SLAVE)
public interface TransferSessionMapper {

    @Select("select count(1) from transfer_session")
    Integer sessionTotal();

    @Select("select count(distinct transfer_id) from transfer_session")
    Integer transferTotal();
}
