package com.zhangls.apijson.base.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.model.RequestMethod;

/**
 * 解析器
 *
 * @author Lemon
 */
public interface Parser<T> {

    /**
     * 设置默认最大查询记录数
     */
    Integer MAX_QUERY_COUNT = 100;

    /**
     * 设置默认最大更新记录数
     */
    Integer MAX_UPDATE_COUNT = 10;

    /**
     * 用户好友列表，访问者列表
     * @return
     */
    @NotNull
    Visitor<T> getVisitor();

    /**
     * 设置访问者列表
     * @param visitor
     * @return
     */
    Parser<T> setVisitor(@NotNull Visitor<T> visitor);

    /**
     * 请求方法 GET POST等
     * @return
     */
    @NotNull
    RequestMethod getMethod();

    Parser<T> setMethod(@NotNull RequestMethod method);

    /**
     * 请求JSon字符串转换的JSONObject对象
     * @return
     */
    JSONObject getRequest();

    Parser<T> setRequest(JSONObject request);


    /**
     * 是否验证请求
     * @return
     */
    Boolean isNoVerify();

    Parser<T> setNoVerify(Boolean noVerify);


    /**
     * 是否验证登陆
     * @return
     */
    Boolean isNoVerifyLogin();

    Parser<T> setNoVerifyLogin(Boolean noVerifyLogin);

    /**
     * 是否验证角色
     * @return
     */
    Boolean isNoVerifyRole();

    Parser<T> setNoVerifyRole(Boolean noVerifyRole);

    /**
     * 是否验证内容
     * @return
     */
    Boolean isNoVerifyContent();

    Parser<T> setNoVerifyContent(Boolean noVerifyContent);

    /**
     * 创建验证器
     * @return
     */
    @NotNull
    Verifier<T> createVerifier();

    /**
     * 创建sql配置器
     * @return
     */
    @NotNull
    SqlConfig createSQLConfig();

    /**
     * 创建sql执行器
     * @return
     */
    @NotNull
    SqlExecutor createSQLExecutor();


    /**
     * 解析请求方法
     * @param request
     * @return
     */
    String parse(String request);

    /**
     * 解析请求方法
     * @param request
     * @return
     */
    String parse(JSONObject request);

    /**
     * 解析请求方法
     * @param request
     * @return
     */
    JSONObject parseResponse(String request);

    /**
     * 解析请求方法
     * @param request
     * @return
     */
    JSONObject parseResponse(JSONObject request);

    JSONObject parseCorrectRequest() throws Exception;

    JSONObject parseCorrectRequest(JSONObject target) throws Exception;

    JSONObject parseCorrectResponse(String table, JSONObject response) throws Exception;

    JSONObject getStructure(String table, String key, String value, Integer version) throws Exception;

    JSONObject onObjectParse(JSONObject request, String parentPath, String name, SqlConfig arrayConfig) throws Exception;

    JSONArray onArrayParse(JSONObject request, String parentPath, String name) throws Exception;

    /**
     * 解析远程函数
     *
     * @param object
     * @param function
     * @return
     * @throws Exception
     */
    Object onFunctionParse(JSONObject object, String function) throws Exception;

    ObjectParser createObjectParser(JSONObject request, String parentPath, String name, SqlConfig arrayConfig) throws Exception;

    Integer getMaxQueryCount();

    Integer getMaxUpdateCount();

    /**
     * 依赖引用关系
     * 将已获取完成的object的内容替换requestObject里对应的值
     * @param path
     * @param result
     */
    void putQueryResult(String path, Object result);

    /**
     * 依赖引用关系-根据路径获取值
     * @param valuePath
     * @return
     */
    Object getValueByPath(String valuePath);

    JSONObject executeSQL(SqlConfig config) throws Exception;

}
