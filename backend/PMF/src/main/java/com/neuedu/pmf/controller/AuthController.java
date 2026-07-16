package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.User;
import com.neuedu.pmf.service.AuthService;
import com.neuedu.pmf.util.AesUtil;
import com.neuedu.pmf.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthController {
    @Autowired
    AuthService authService;

    @Autowired
    JwtUtil jwtUtil;

    /**
     * 登录接口（POST + AES 加密传输）
     * 前端将密码用 AES-256-CBC 加密后以 Base64 传输，
     * 后端先 AES 解密，再 BCrypt 验证。
     */
    @PostMapping("/login")
    public ResultData login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String encryptedPassword = body.get("password");

        if (username == null || encryptedPassword == null) {
            return ResultData.fail(ResultCode.USER_AND_PASSWORD_ERROR);
        }

        // AES 解密前端传来的密码
        String password = AesUtil.decrypt(encryptedPassword);
        if (password == null) {
            return ResultData.fail(ResultCode.USER_AND_PASSWORD_ERROR);
        }

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
