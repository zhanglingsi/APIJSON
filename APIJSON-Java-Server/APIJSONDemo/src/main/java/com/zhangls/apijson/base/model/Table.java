package com.zhangls.apijson.base.model;


import com.zhangls.apijson.annotation.MethodAccess;

/**
 * 数据库表(增删改)
 *
 * @author zhangls
 */
@MethodAccess(POST = {}, PUT = {}, DELETE = {})
public class Table {
    public static final String TAG = "tables";

}
