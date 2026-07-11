package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.entity.Goods;
import com.neuedu.pmf.exception.BusinessException;
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
        Goods goods = goodsMapper.findById(id);
        if (goods == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (goods.getQuantity() != null && goods.getQuantity() > 0) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "该货物数量不为0，不能删除");
        }
        return goodsMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Goods goods) {
        goods.setQuantity(0);

        java.util.List<Integer> existingIds = goodsMapper.findAllIds();
        int newId = 1;
        for (int id : existingIds) {
            if (id == newId) {
                newId++;
            } else if (id > newId) {
                break;
            }
        }
        goods.setGoods_id(newId);

        return goodsMapper.save(goods) > 0;
    }

    @Override
    public boolean update(Goods goods) {
        return goodsMapper.update(goods) > 0;
    }
}
