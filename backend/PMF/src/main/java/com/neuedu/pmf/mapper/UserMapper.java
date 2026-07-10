package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.ArrayList;

@Mapper//标记接口为MyBatis的Mapper映射器
public interface UserMapper {

    //查询所有用户，放回用户列表
    ArrayList<User> userList();

    //按ID查找用户，用@Param绑定mysql中的user_id
    User findById(@Param("user_id") Integer userId);

    //按用户名查找用户
    User findByUsername(@Param("username") String username);

    //按用户名和密码查找用户，登录时用
    User findByUsernameAndPassword(@Param("username") String username,
                                   @Param("password") String password);

    //新增用户
    int save(User user);

    //更新用户信息
    int update(User user);

    //根据ID删除用户
    int deleteById(@Param("user_id") Integer userId);
}
