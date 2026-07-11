package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.User;
import java.util.ArrayList;

public interface UserService {
    ArrayList<User> list();

    User getById(Integer id);

    boolean delete(Integer id);

    boolean save(User user);

    boolean update(User user);

    User login(String username, String password);

    /**
     * 更新用户个人信息（含密码修改验证）
     * @param params 包含 user_id, real_name, phone, department, position, old_password, new_password
     * @return 更新后的用户信息，null表示旧密码验证失败
     */
    User updateProfile(java.util.Map<String, Object> params);
}
