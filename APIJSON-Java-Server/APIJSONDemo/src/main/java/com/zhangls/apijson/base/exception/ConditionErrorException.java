package com.zhangls.apijson.base.exception;

/**
 * 条件错误
 *
 * @author Lemon
 */
public class ConditionErrorException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConditionErrorException(String msg) {
        super(msg);
    }

    public ConditionErrorException(Throwable t) {
        super(t);
    }

    public ConditionErrorException(String msg, Throwable t) {
        super(msg, t);
    }

}
