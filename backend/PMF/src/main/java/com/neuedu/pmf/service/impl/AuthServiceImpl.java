package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.User;
import com.neuedu.pmf.mapper.UserMapper;
import com.neuedu.pmf.service.AuthService;
import com.neuedu.pmf.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsernameAndPassword(username, password);
        if (user != null) {
            try {
                String redisKey = "username_" + username;
                redisUtil.set(redisKey, user, 30, TimeUnit.MINUTES);
            } catch (Exception e) {
                // Redis unavailable, log only, do not affect login
                System.err.println("Redis cache failed (does not affect login): " + e.getMessage());
            }
            return user;
        }
        return null;
    }
}
