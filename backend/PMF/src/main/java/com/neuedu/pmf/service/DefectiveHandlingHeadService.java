package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.DefectiveHandlingHead;
import java.util.ArrayList;

public interface DefectiveHandlingHeadService {
    ArrayList<DefectiveHandlingHead> list();

    DefectiveHandlingHead getById(String id);

    boolean delete(String id);

    boolean save(DefectiveHandlingHead defectiveHandlingHead);

    boolean update(DefectiveHandlingHead defectiveHandlingHead);
}
