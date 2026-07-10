package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.ArrayList;

@Mapper
public interface UserMapper {

    ArrayList<User> userList();

    User findById(@Param("user_id") Integer userId);

    User findByUsername(@Param("username") String username);

    User findByUsernameAndPassword(@Param("username") String username,
                                   @Param("password") String password);

    int save(User user);

    int update(User user);

    int deleteById(@Param("user_id") Integer userId);
}
