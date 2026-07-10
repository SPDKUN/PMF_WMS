package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.User;
import com.neuedu.pmf.mapper.UserMapper;
import com.neuedu.pmf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserServicelmpl implements UserService {

    //自动创建userMapper实例
    @Autowired
    private UserMapper userMapper;

    //@Override强调这是一个覆盖父类的方法，写错也不会报错
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
