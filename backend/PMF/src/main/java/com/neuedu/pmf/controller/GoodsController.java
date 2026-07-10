package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Goods;
import com.neuedu.pmf.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(goodsService.list());
    }

    @GetMapping("/{goodsId}")
    public ResultData getById(@PathVariable Integer goodsId) {
        return ResultData.success(goodsService.getById(goodsId));
    }

    @PostMapping
    public ResultData save(@RequestBody Goods goods) {
        boolean flag = goodsService.save(goods);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody Goods goods) {
        boolean flag = goodsService.update(goods);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable Integer id) {
        boolean flag = goodsService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
