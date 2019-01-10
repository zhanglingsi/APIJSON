package com.zhangls.apijson.base.exception;

/**
 * 未登录
 *
 * @author Lemon
 */
public class NotLoggedInException extends Exception {
    private static final long serialVersionUID = 1L;

    public NotLoggedInException(String msg, Throwable t) {
        super(msg, t);
    }

    public NotLoggedInException(String msg) {
        super(msg);
    }

    public NotLoggedInException(Throwable t) {
        super(t);
    }

}
