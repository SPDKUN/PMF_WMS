package com.neuedu.pmf.common;

import java.util.HashMap;

public class ResultData extends HashMap<String, Object> {
    private int code;

    private String msg;

    private Object data;

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

    public static ResultData success( Object data) {
        return new ResultData(ResultCode.SUCCESS, data);
    }


    public static ResultData success() {
        return success( null);
    }

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

