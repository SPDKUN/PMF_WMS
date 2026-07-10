package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.User;
import com.neuedu.pmf.mapper.UserMapper;
import com.neuedu.pmf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserServicelmpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ArrayList<User> list() {
        return userMapper.userList();
    }

    @Override
    public User getById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(User user) {
        return userMapper.save(user) > 0;
    }

    @Override
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    public User login(String username, String password) {
        return userMapper.findByUsernameAndPassword(username, password);
    }
}
