package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Zone {
    private Integer zone_id;
    private String zone_name;
    private Integer warehouse_id;
    private Integer total_slots;
    private Integer available_slots;
    private String description;
}
