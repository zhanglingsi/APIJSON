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

    Integer MAX_QUERY_COUNT = 100;

    Integer MAX_UPDATE_COUNT = 10;
    
    @NotNull
    Visitor<T> getVisitor();

    Parser<T> setVisitor(@NotNull Visitor<T> visitor);

    @NotNull
    RequestMethod getMethod();

    Parser<T> setMethod(@NotNull RequestMethod method);

    JSONObject getRequest();

    Parser<T> setRequest(JSONObject request);

    Boolean isNoVerify();

    Parser<T> setNoVerify(Boolean noVerify);

    Boolean isNoVerifyLogin();

    Parser<T> setNoVerifyLogin(Boolean noVerifyLogin);

    Boolean isNoVerifyRole();

    Parser<T> setNoVerifyRole(Boolean noVerifyRole);

    Boolean isNoVerifyContent();

    Parser<T> setNoVerifyContent(Boolean noVerifyContent);

    @NotNull
    Verifier<T> createVerifier();

    @NotNull
    SqlConfig createSQLConfig();

    @NotNull
    SqlExecutor createSQLExecutor();


    String parse(String request);

    String parse(JSONObject request);

    JSONObject parseResponse(String request);

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

    void putQueryResult(String path, Object result);


    Object getValueByPath(String valuePath);


    JSONObject executeSQL(SqlConfig config) throws Exception;

}
