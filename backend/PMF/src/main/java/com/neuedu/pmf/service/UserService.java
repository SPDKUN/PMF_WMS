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
}
