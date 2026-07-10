package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.QualityCheckHead;
import java.util.ArrayList;

public interface QualityCheckHeadService {
    ArrayList<QualityCheckHead> list();

    QualityCheckHead getById(String id);

    boolean delete(String id);

    boolean save(QualityCheckHead qualityCheckHead);

    boolean update(QualityCheckHead qualityCheckHead);
}
