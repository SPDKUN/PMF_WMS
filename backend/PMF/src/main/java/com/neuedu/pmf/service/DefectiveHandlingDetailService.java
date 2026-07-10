package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.DefectiveHandlingDetail;
import java.util.ArrayList;

public interface DefectiveHandlingDetailService {
    ArrayList<DefectiveHandlingDetail> list();

    DefectiveHandlingDetail getById(Integer id);

    boolean delete(Integer id);

    boolean save(DefectiveHandlingDetail defectiveHandlingDetail);

    boolean update(DefectiveHandlingDetail defectiveHandlingDetail);
}
