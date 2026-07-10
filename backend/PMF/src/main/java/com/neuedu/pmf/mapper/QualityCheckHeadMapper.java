package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.QualityCheckHead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface QualityCheckHeadMapper {
    ArrayList<QualityCheckHead> list();
    int save(QualityCheckHead qualityCheckHead);
    QualityCheckHead findById(@Param("quality_check_no") String qualityCheckNo);
    int update(QualityCheckHead qualityCheckHead);
    int deleteById(@Param("quality_check_no") String qualityCheckNo);
}
