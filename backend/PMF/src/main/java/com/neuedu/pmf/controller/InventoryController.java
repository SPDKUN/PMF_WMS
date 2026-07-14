package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.service.InventoryService;
import com.neuedu.pmf.util.ExcelExportUtil;
import com.neuedu.pmf.vo.InventoryVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ExcelExportUtil excelExportUtil;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(inventoryService.list());
    }

    @GetMapping("/listWithDetails")
    public ResultData listWithDetails(
            @RequestParam(required = false) String batchId) {
        return ResultData.success(inventoryService.listWithDetails(batchId));
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(inventoryService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody Inventory inventory) {
        boolean flag = inventoryService.save(inventory);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody Inventory inventory) {
        boolean flag = inventoryService.update(inventory);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = inventoryService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @GetMapping("/excel")
    public void excel(HttpServletResponse response) {
        ArrayList<InventoryVO> list = inventoryService.listWithDetails(null);
        excelExportUtil.exportExcel(response, list, InventoryVO.class, "库存明细", "库存明细");
    }
}
