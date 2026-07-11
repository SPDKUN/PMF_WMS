package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Warehouse {
    private Integer warehouse_id;
    private String warehouse_name;
    private String warehouse_type;
    private String warehouse_size;
    private Integer total_slots;
    private Integer available_slots;
    private String location;
    private String status;
    private String description;
}
