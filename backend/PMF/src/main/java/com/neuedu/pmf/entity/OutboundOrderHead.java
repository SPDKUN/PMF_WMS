package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutboundOrderHead {
    private String outbound_no;
    private String outbound_type;
    private String order_status;
    private Integer operator_id;
    private LocalDateTime outbound_time;
    private String remark;
}
