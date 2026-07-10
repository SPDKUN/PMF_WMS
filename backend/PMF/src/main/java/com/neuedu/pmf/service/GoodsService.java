package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Goods;
import java.util.ArrayList;

public interface GoodsService {
    ArrayList<Goods> list();

    Goods getById(Integer id);

    boolean delete(Integer id);

    boolean save(Goods goods);

    boolean update(Goods goods);
}
