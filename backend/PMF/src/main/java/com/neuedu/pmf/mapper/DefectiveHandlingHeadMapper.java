package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.DefectiveHandlingHead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface DefectiveHandlingHeadMapper {
    ArrayList<DefectiveHandlingHead> list();
    int save(DefectiveHandlingHead defectiveHandlingHead);
    DefectiveHandlingHead findById(@Param("handling_no") String handlingNo);
    int update(DefectiveHandlingHead defectiveHandlingHead);
    int deleteById(@Param("handling_no") String handlingNo);
}
