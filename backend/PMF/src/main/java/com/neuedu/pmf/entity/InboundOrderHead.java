package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboundOrderHead {
    private String inbound_no;
    private String inbound_type;
    private String order_status;
    private Integer operator_id;
    private LocalDateTime inbound_time;
    private String remark;
}
