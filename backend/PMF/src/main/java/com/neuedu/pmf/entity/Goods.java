package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods {
    private Integer goods_id;
    private String goods_code;
    private String category;
    private String goods_name;
    private String storage_location;
    private String storage_condition;
    private LocalDate expiry_date;
    private String batch_no;
    private String description;
}
