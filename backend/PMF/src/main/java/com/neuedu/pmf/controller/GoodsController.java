package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Goods;
import com.neuedu.pmf.service.GoodsService;
import com.neuedu.pmf.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ExcelExportUtil excelExportUtil;

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

    @GetMapping("/excel")
    public void excel(HttpServletResponse response) {
        ArrayList<Goods> list = goodsService.list();
        excelExportUtil.exportExcel(response, list, Goods.class, "货物列表", "货物列表");
    }
}
