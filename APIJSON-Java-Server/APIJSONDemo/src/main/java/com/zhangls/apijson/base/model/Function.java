package com.zhangls.apijson.base.model;

import com.zhangls.apijson.annotation.MethodAccess;

import java.io.Serializable;

/**
 * 远程函数
 *
 * 增删改权限回收，不允许增删改此表
 *
 * @author zhangls
 */
@MethodAccess(
        POST = {},
        PUT = {},
        DELETE = {}
)
public class Function implements Serializable {
    private static final long serialVersionUID = 1L;
}