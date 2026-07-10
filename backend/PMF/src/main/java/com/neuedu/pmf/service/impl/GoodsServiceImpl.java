package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Goods;
import com.neuedu.pmf.mapper.GoodsMapper;
import com.neuedu.pmf.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public ArrayList<Goods> list() {
        return goodsMapper.list();
    }

    @Override
    public Goods getById(Integer id) {
        return goodsMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return goodsMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Goods goods) {
        return goodsMapper.save(goods) > 0;
    }

    @Override
    public boolean update(Goods goods) {
        return goodsMapper.update(goods) > 0;
    }
}
