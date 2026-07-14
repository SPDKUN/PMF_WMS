package com.neuedu.pmf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    List<Map<String, Object>> weeklyInbound(@Param("startDate") LocalDate startDate);

    List<Map<String, Object>> weeklyOutbound(@Param("startDate") LocalDate startDate);

    List<Map<String, Object>> latestTempHumidityPerWarehouse();
}
