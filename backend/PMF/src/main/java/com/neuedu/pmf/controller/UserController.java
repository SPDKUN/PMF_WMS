package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.User;
import com.neuedu.pmf.service.UserService;
import com.neuedu.pmf.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExcelExportUtil excelExportUtil;
    

    @GetMapping("/personnel")
    public ResultData userList() {
        ArrayList<User> users = userService.list();

        return ResultData.success(users);
    }


    //    请求的路径和请求的方式：来确定来访问哪个方法
    @PostMapping()
    public ResultData save(@RequestBody User user) {
        boolean flag = userService.save(user);
        if(flag) {
            //ResultData resultData = new ResultData(200, "操作成功", null);
            return ResultData.success();
        }
        //ResultData resultData = new ResultData(700, "保存失败", null);
        return ResultData.fail(ResultCode.FAILED);
    }

    @GetMapping("/{userId}")
    public ResultData getUser(@PathVariable("userId") Integer id) {
        User user = userService.getById(id);
        return ResultData.success(user);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag =  userService.delete(id);
        if(flag) {
            return ResultData.success();
        }
        return ResultData.fail(ResultCode.FAILED);
    }

    @GetMapping("/excel")
    public void excel(HttpServletResponse response) {
        ArrayList<User> users = userService.list();
        excelExportUtil.exportExcel(response, users, User.class, "用户列表", "用户列表1");
    }


    @PutMapping
    public ResultData update(@RequestBody User user) {
        boolean flag =  userService.update(user);
        if(flag) {
            return ResultData.success();
        }
        return ResultData.fail(ResultCode.FAILED);
    }



}

