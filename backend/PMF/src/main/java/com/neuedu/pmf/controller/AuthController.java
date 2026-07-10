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
        User user = authService.login(username, password);
        if (user != null) {
            String token = jwtUtil.generateToken(user.getUser_id().longValue(), username);
            ResultData resultData = ResultData.success(user);
            resultData.put("token", token);
            return resultData;
        }
        return ResultData.fail(ResultCode.USER_AND_PASSWORD_ERROR);
    }
}
