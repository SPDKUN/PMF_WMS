package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.DefectiveHandlingDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface DefectiveHandlingDetailMapper {
    ArrayList<DefectiveHandlingDetail> list();
    int save(DefectiveHandlingDetail defectiveHandlingDetail);
    DefectiveHandlingDetail findById(@Param("detail_id") Integer detailId);
    int update(DefectiveHandlingDetail defectiveHandlingDetail);
    int deleteById(@Param("detail_id") Integer detailId);
    ArrayList<DefectiveHandlingDetail> findByHandlingNo(@Param("handling_no") String handlingNo);
    int deleteByHandlingNo(@Param("handling_no") String handlingNo);
}
