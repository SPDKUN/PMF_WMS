package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemperatureHumidityRecord {
    private Integer record_id;
    private Integer warehouse_id;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private LocalDateTime recorded_time;
}
