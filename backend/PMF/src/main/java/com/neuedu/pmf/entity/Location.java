package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private Integer location_id;
    private String location_name;
    private Integer zone_id;
    private Integer goods_id;
    private Integer is_occupied;
    private String lock_status;
    private String lock_purpose;
    private String description;
}
