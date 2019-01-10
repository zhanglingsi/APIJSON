package com.zhangls.apijson.base.model;

import com.zhangls.apijson.annotation.MethodAccess;

import java.io.Serializable;

/**
 * 请求处理
 *
 * 增删改权限回收，不允许增删改此表
 * @author Lemon
 */
@MethodAccess(
        POST = {},
        PUT = {},
        DELETE = {}
)
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
}
