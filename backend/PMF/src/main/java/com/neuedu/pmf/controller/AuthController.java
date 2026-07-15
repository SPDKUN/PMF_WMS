package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.User;
import com.neuedu.pmf.service.AuthService;
import com.neuedu.pmf.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/login")
    public ResultData login(@RequestParam("username") String username, @RequestParam("password") String password) {
        //调用Service验证用户
        User user = authService.login(username, password);
        if (user != null) {
            //用户存在，生成令牌
            String token = jwtUtil.generateToken(user.getUser_id().longValue(), username);
            //生成成功消息
            ResultData resultData = ResultData.success(user);
            //将令牌塞入成功消息
            resultData.put("token", token);
            return resultData;
        }
        //返回失败消息
        return ResultData.fail(ResultCode.USER_AND_PASSWORD_ERROR);
    }
}
