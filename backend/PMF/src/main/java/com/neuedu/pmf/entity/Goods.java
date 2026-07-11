package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods {
    private Integer goods_id;
    private String goods_code;
    private String category;
    private String goods_name;
    private String storage_condition;
    private Integer quantity;
    private String unit;
}
