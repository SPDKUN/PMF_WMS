package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Batch {
    private String batch_id;
    private Integer goods_id;
    private LocalDate production_date;
    private LocalDate expiry_date;
    private Integer initial_quantity;
    private Integer remaining_quantity;
    private String batch_status;
}
