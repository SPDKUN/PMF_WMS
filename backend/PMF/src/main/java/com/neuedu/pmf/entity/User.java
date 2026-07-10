package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer user_id; //用户id
    private String username; //用户名
    private String real_name; //用户真实姓名
    private String phone; //电话号码
    private String department; //所属部门
    private String position; //职位
    private String password; //密码
    private Integer status; //账号状态（1为激活，0为冻结）
}
