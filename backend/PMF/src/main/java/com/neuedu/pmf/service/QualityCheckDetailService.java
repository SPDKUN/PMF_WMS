package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.QualityCheckDetail;
import java.util.ArrayList;

public interface QualityCheckDetailService {
    ArrayList<QualityCheckDetail> list();

    QualityCheckDetail getById(Integer id);

    boolean delete(Integer id);

    boolean save(QualityCheckDetail qualityCheckDetail);

    boolean update(QualityCheckDetail qualityCheckDetail);

    QualityCheckDetail getByCheckNo(String qualityCheckNo);
}
