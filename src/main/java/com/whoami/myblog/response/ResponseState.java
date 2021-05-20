package com.whoami.myblog.response;

public enum  ResponseState {

    SUCCESS(20000, true, "操作成功"),
    LOGIN_SUCCESS(20001,true, "登录成功"),
    JOIN_IN__SUCCESS(20003,true, "注册成功"),
    FAILED(40000, false, "操作失败"),
    PARAMS_ILL(40001, false, "参数错误"),
    PERMISSION_DENIED(40002, false, "权限不够"),
    ACCOUNT_BANNED(40003, false, "当前账号被禁止"),
    NOT_LOGIN(40004, false, "账号未登录"),
    ERROR_403(40005, false, "权限不够"),
    ERROR_404(40006, false, "页面丢失"),
    ERROR_504(40007, false, "系统繁忙，请稍后再试"),
    ERROR_505(40008, false, "服务器不支持请求中使用的 HTTP 版本"),
    WAITING_FOR_SCAN(40009, false, "等待扫描"),
    QR_CODE_DEPRECATE(40010, false, "二维码已过期")
    ;

    private int code;
    private String message;
    private boolean success;

    ResponseState(int code, boolean success, String message) {
        this.code = code;
        this.success = success;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
