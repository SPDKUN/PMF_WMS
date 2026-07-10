package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.User;

public interface AuthService {
    User login(String username, String password);
}
