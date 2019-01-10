package com.zhangls.apijson.base.service;


import com.zhangls.apijson.annotation.NotNull;

/**
 * SQL相关创建器
 *
 * @author Lemon
 */
public interface SqlCreator {

    @NotNull
    SqlConfig createSQLConfig();

    @NotNull
    SqlExecutor createSQLExecutor();
}
