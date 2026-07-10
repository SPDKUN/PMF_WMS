package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.AdjustmentOrderHead;
import java.util.ArrayList;

public interface AdjustmentOrderHeadService {
    ArrayList<AdjustmentOrderHead> list();

    AdjustmentOrderHead getById(String id);

    boolean delete(String id);

    boolean save(AdjustmentOrderHead adjustmentOrderHead);

    boolean update(AdjustmentOrderHead adjustmentOrderHead);
}
