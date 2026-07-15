package com.neuedu.pmf.common;

import java.util.HashMap;

//统一返回格式
//{
//        "code": 200,       // 状态码：200=成功，1001=用户名密码错误，1002=令牌过期
//        "msg": "操作成功",   // 提示信息
//        "data": { ... },   // 真正的数据
//        "token": "xxx"     // 登录时才会有的令牌
//        }
public class ResultData extends HashMap<String, Object> {
    private int code;

    private String msg;

    private Object data;

    //初始化
    public ResultData(ResultCode resultCode, Object data) {


        this.code = resultCode.getCode();
        this.msg = resultCode.getMessage();
        this.data = data;

        this.put("code", code);
        this.put("msg", msg);
        this.put("data", data);
    }

    public ResultData(ResultCode resultCode) {
        this(resultCode, null);
    }

    //成功时调用
    public static ResultData success( Object data) {
        return new ResultData(ResultCode.SUCCESS, data);
    }


    public static ResultData success() {
        return success( null);
    }

    //失败时调用
    public static ResultData fail(ResultCode resultCode, Object data) {
        return new ResultData(resultCode, data);
    }

    public static ResultData fail(ResultCode resultCode) {
        return new ResultData(resultCode);
    }

    public ResultData() {
    }

    public static ResultData fail(int code, String msg) {
        ResultData result = new ResultData();
        result.code = code;
        result.msg = msg;
        result.put("code", code);
        result.put("msg", msg);
        result.put("data", null);
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

