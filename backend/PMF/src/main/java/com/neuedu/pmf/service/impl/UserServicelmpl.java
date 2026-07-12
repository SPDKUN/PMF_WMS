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

    @Override
    public User updateProfile(java.util.Map<String, Object> params) {
        Integer userId = (Integer) params.get("user_id");
        if (userId == null) {
            return null;
        }

        // 检查是否需要修改密码
        String oldPassword = (String) params.get("old_password");
        String newPassword = (String) params.get("new_password");

        if (oldPassword != null && !oldPassword.isEmpty()
                && newPassword != null && !newPassword.isEmpty()) {
            // 验证旧密码
            User verified = userMapper.findByIdAndPassword(userId, oldPassword);
            if (verified == null) {
                return null; // 旧密码错误
            }
        }

        // 构建更新的User对象
        User user = new User();
        user.setUser_id(userId);

        String username = (String) params.get("username");
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }

        String realName = (String) params.get("real_name");
        if (realName != null && !realName.isEmpty()) {
            user.setReal_name(realName);
        }

        String phone = (String) params.get("phone");
        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }

        String department = (String) params.get("department");
        if (department != null && !department.isEmpty()) {
            user.setDepartment(department);
        }

        String position = (String) params.get("position");
        if (position != null && !position.isEmpty()) {
            user.setPosition(position);
        }

        // 如果需要修改密码
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }

        userMapper.update(user);
        return userMapper.findById(userId);
    }

    @Override
    public ArrayList<User> listByPosition(String position) {
        return userMapper.findByPosition(position);
    }
}
