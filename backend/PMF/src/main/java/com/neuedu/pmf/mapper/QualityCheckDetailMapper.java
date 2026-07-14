package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.QualityCheckDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface QualityCheckDetailMapper {
    ArrayList<QualityCheckDetail> list();
    int save(QualityCheckDetail qualityCheckDetail);
    QualityCheckDetail findById(@Param("detail_id") Integer detailId);
    int update(QualityCheckDetail qualityCheckDetail);
    int deleteById(@Param("detail_id") Integer detailId);
    int deleteByBatchId(@Param("batch_id") String batchId);
    QualityCheckDetail findByCheckNo(@Param("quality_check_no") String qualityCheckNo);
}
