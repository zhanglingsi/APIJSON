package com.zhangls.apijson.base.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.model.RequestMethod;


import java.util.Map;

/**
 * 简化Parser，getObject和getArray(getArrayConfig)都能用
 *
 * @author Lemon
 */
public interface ObjectParser {


    /**
     * 解析成员
     * response重新赋值
     *
     * @return null or this
     * @throws Exception
     */
    ObjectParser parse() throws Exception;

    /**
     * 解析 @correct 校正
     *
     * @throws Exception
     */
    ObjectParser parseCorrect() throws Exception;

    /**
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject parseResponse(@NotNull JsonApiRequest request) throws Exception;


    /**
     * 解析普通成员
     *
     * @param key
     * @param value
     * @return whether parse succeed
     */
    Boolean onParse(@NotNull String key, @NotNull Object value) throws Exception;

    /**
     * 解析子对象
     *
     * @param index
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    JSON onChildParse(Integer index, String key, JSONObject value) throws Exception;

    /**
     * 解析赋值引用
     *
     * @param path
     * @return
     */
    Object onReferenceParse(@NotNull String path);

    //TODO 改用 MySQL json_add,json_remove,json_contains 等函数！

    /**
     * PUT key:[]
     *
     * @param key
     * @param array
     * @throws Exception
     */
    void onPUTArrayParse(@NotNull String key, @NotNull JSONArray array) throws Exception;


    /**
     * SQL查询，for single object
     *
     * @return {@link #executeSQL(Integer, Integer, Integer)}
     * @throws Exception
     */
    ObjectParser executeSQL() throws Exception;

    /**
     * SQL查询，for array item
     *
     * @param count
     * @param page
     * @param position
     * @return this
     * @throws Exception
     */
    ObjectParser executeSQL(Integer count, Integer page, Integer position) throws Exception;

    /**
     * @return
     * @throws Exception
     */
    JSONObject onSQLExecute() throws Exception;


    /**
     * @return response
     * @throws Exception
     */
    JSONObject response() throws Exception;

    void onFunctionResponse(String type) throws Exception;

    void onChildResponse() throws Exception;


    SqlConfig newSQLConfig() throws Exception;

    /**
     * response has the final value after parse (and query if isTableKey)
     */
    void onComplete();


    /**
     * 回收内存
     */
    void recycle();


    ObjectParser setMethod(RequestMethod method);

    RequestMethod getMethod();


    Boolean isTable();

    String getPath();

    String getTable();

    SqlConfig getArrayConfig();

    SqlConfig getSQLConfig();

    JSONObject getResponse();

    JSONObject getSqlRequest();

    JSONObject getSqlReponse();

    Map<String, Object> getCustomMap();

    Map<String, Map<String, String>> getFunctionMap();

    Map<String, JSONObject> getChildMap();


}
