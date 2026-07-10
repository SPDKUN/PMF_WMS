package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.AdjustmentOrderDetail;
import java.util.ArrayList;

public interface AdjustmentOrderDetailService {
    ArrayList<AdjustmentOrderDetail> list();

    AdjustmentOrderDetail getById(Integer id);

    boolean delete(Integer id);

    boolean save(AdjustmentOrderDetail adjustmentOrderDetail);

    boolean update(AdjustmentOrderDetail adjustmentOrderDetail);
}
