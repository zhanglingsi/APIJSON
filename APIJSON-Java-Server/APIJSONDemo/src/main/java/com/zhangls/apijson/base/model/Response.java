package com.zhangls.apijson.base.model;

import com.zhangls.apijson.annotation.MethodAccess;

import java.io.Serializable;

/**
 * 结果处理
 *
 * @author zhangls
 */
@MethodAccess(
        POST = {},
        PUT = {},
        DELETE = {}
)
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
}
