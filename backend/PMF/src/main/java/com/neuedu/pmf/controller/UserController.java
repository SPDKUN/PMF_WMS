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
    
    //返回整用户列表
    @GetMapping("/personnel")
    public ResultData userList() {
        ArrayList<User> users = userService.list();

        return ResultData.success(users);
    }


    //创建新的用户
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

    //根据id查找用户
    @GetMapping("/{userId}")
    public ResultData getUser(@PathVariable("userId") Integer id) {
        User user = userService.getById(id);
        return ResultData.success(user);
    }

    //根据id删除用户
    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag =  userService.delete(id);
        if(flag) {
            return ResultData.success();
        }
        return ResultData.fail(ResultCode.FAILED);
    }

    //导出excel表格
    @GetMapping("/excel")
    public void excel(HttpServletResponse response) {
        ArrayList<User> users = userService.list();
        excelExportUtil.exportExcel(response, users, User.class, "用户列表", "用户列表1");
    }

    //更新用户信息
    @PutMapping
    public ResultData update(@RequestBody User user) {
        boolean flag =  userService.update(user);
        if(flag) {
            return ResultData.success();
        }
        return ResultData.fail(ResultCode.FAILED);
    }

    //更新个人信息（含密码修改验证）
    @PutMapping("/profile")
    public ResultData updateProfile(@RequestBody java.util.Map<String, Object> params) {
        User updatedUser = userService.updateProfile(params);
        if (updatedUser == null) {
            return ResultData.fail(700, "旧密码错误或用户不存在");
        }
        return ResultData.success(updatedUser);
    }



}

