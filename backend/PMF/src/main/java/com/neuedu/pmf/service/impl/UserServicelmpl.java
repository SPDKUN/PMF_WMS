package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.User;
import com.neuedu.pmf.mapper.UserMapper;
import com.neuedu.pmf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserServicelmpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
        user.setPassword(encoder.encode(user.getPassword()));
        return userMapper.save(user) > 0;
    }

    @Override
    public boolean update(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(user.getPassword()));
        }
        return userMapper.update(user) > 0;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && encoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public User updateProfile(java.util.Map<String, Object> params) {
        Integer userId = (Integer) params.get("user_id");
        if (userId == null) {
            return null;
        }

        String oldPassword = (String) params.get("old_password");
        String newPassword = (String) params.get("new_password");

        if (oldPassword != null && !oldPassword.isEmpty()
                && newPassword != null && !newPassword.isEmpty()) {
            User currentUser = userMapper.findById(userId);
            if (currentUser == null || !encoder.matches(oldPassword, currentUser.getPassword())) {
                return null;
            }
        }

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

        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(encoder.encode(newPassword));
        }

        userMapper.update(user);
        return userMapper.findById(userId);
    }

    @Override
    public ArrayList<User> listByPosition(String position) {
        return userMapper.findByPosition(position);
    }

    @Override
    public int migratePasswords() {
        ArrayList<User> users = userMapper.userList();
        int count = 0;
        for (User user : users) {
            String pwd = user.getPassword();
            if (pwd != null && !pwd.startsWith("$2")) {
                user.setPassword(encoder.encode(pwd));
                userMapper.update(user);
                count++;
            }
        }
        return count;
    }
}
