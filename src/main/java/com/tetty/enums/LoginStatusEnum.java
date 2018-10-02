package com.tetty.enums;

/**
 * @Author: cfh
 * @Date: 2018/10/2 15:13
 * @Description:
 */
public enum  LoginStatusEnum {
    NOT_IN_WHITE_TABLES((byte)-2, "IP地址不在白名单中"),
    REPEAT_LOGIN((byte)-1, "重复登录"),
    NORMAL_LOGIN((byte)0, "登录成功")
    ;

    byte code;
    String msg;

    LoginStatusEnum(byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public byte getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
