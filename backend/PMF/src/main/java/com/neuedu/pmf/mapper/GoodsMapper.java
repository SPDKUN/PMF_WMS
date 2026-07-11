package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface GoodsMapper {
    ArrayList<Goods> list();
    int save(Goods goods);
    Goods findById(@Param("goods_id") Integer goodsId);
    int update(Goods goods);
    int deleteById(@Param("goods_id") Integer goodsId);
    java.util.List<Integer> findAllIds();
}
