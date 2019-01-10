package com.zhangls.apijson.base.service;

import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.annotation.NotNull;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * executor for query(read) or update(write) MySQL database
 *
 * @author Lemon
 */
public interface SqlExecutor {

    /**
     * 保存缓存
     *
     * @param sql
     * @param map
     * @param isStatic
     */
    void putCache(String sql, Map<Integer, JSONObject> map, boolean isStatic);

    /**
     * 移除缓存
     *
     * @param sql
     * @param isStatic
     */
    void removeCache(String sql, boolean isStatic);

    /**
     * 获取缓存
     *
     * @param sql
     * @param position
     * @param isStatic
     * @return
     */
    JSONObject getCache(String sql, int position, boolean isStatic);


    /**
     * 执行SQL
     *
     * @param config
     * @return
     * @throws Exception
     */
    JSONObject execute(SqlConfig config) throws Exception;

    //executeQuery和executeUpdate这两个函数因为返回类型不同，所以不好合并

    /**
     * 执行查询
     *
     * @param config
     * @return
     * @throws SQLException
     */
    ResultSet executeQuery(@NotNull SqlConfig config) throws Exception;

    /**
     * 执行增、删、改
     *
     * @param config
     * @return
     * @throws SQLException
     */
    int executeUpdate(@NotNull SqlConfig config) throws Exception;


    /**
     * 判断是否为JSON类型
     *
     * @param rsmd
     * @param position
     * @return
     */
    boolean isJSONType(ResultSetMetaData rsmd, int position);

    /**
     * 关闭连接，释放资源
     */
    void close();

}
