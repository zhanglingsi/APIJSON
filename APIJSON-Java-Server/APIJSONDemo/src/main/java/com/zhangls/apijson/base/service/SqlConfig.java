package com.zhangls.apijson.base.service;


import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.model.RequestRole;
import com.zhangls.apijson.base.service.impl.Join;

import java.util.List;
import java.util.Map;

/**
 * SQL配置
 *
 * @author Lemon
 */
public interface SqlConfig {

    String DATABASE_MYSQL = "MySQL";
    String DATABASE_POSTGRESQL = "PostgreSQL";

    String SCHEMA_INFORMATION = "information_schema";
    String TABLE_SCHEMA = "table_schema";
    String TABLE_NAME = "table_name";

    Integer TYPE_CHILD = 0;
    Integer TYPE_ITEM = 1;
    Integer TYPE_ITEM_CHILD_0 = 2;
    //////////////////////////////////////////////////客户端实现/////////////////////////////////////////////////
    /**
     * 获取数据库地址
     *
     * @return
     */
    String getDBUri();

    /**
     * 获取数据库账号
     *
     * @return
     */
    String getDBAccount();

    /**
     * 获取数据库密码
     *
     * @return
     */
    String getDBPassword();

    /**
     * 获取SQL语句
     *
     * @return
     * @throws Exception
     */
    String getSQL(Boolean prepared) throws Exception;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 测试
     *
     * @return
     */
    boolean isTest();

    SqlConfig setTest(boolean test);

    /**
     * @return
     */
    boolean isCacheStatic();

    SqlConfig setCacheStatic(boolean cacheStatic);

    /**
     * @return
     */
    Integer getType();

    SqlConfig setType(Integer type);

    /**
     * @return
     */
    Integer getCount();

    SqlConfig setCount(Integer count);

    /**
     * @return
     */
    Integer getPage();

    SqlConfig setPage(Integer page);

    /**
     * @return
     */
    Integer getQuery();

    SqlConfig setQuery(Integer query);

    /**
     * @return
     */
    Integer getPosition();

    SqlConfig setPosition(Integer position);

    /**
     * @return
     */
    RequestMethod getMethod();

    SqlConfig setMethod(RequestMethod method);

    /**
     * @return
     */
    Object getId();

    SqlConfig setId(Object id);

    /**
     * @return
     */
    RequestRole getRole();

    SqlConfig setRole(RequestRole role);

    /**
     * @return
     */
    String getDatabase();

    SqlConfig setDatabase(String database);

    /**
     * 根据数据库类型取关键字分隔符 mysql用`schema`
     * 其他数据库用       "schema"
     *
     * @return
     */
    String getQuote();

    /**
     * @return
     */
    String getSchema();

    SqlConfig setSchema(String schema);

    /**
     * 请求传进来的Table名
     *
     * @return
     * @see {@link #getSQLTable()}
     */
    String getTable();

    SqlConfig setTable(String table);

    /**
     * 数据库里的真实Table名
     *
     * @return
     */
    String getSQLTable();

    String getTablePath();


    String getGroup();

    SqlConfig setGroup(String group);

    String getHaving();

    SqlConfig setHaving(String having);

    String getOrder();

    SqlConfig setOrder(String order);

    List<String> getColumn();

    SqlConfig setColumn(List<String> column);

    List<List<Object>> getValues();

    SqlConfig setValues(List<List<Object>> values);

    Map<String, Object> getContent();

    SqlConfig setContent(Map<String, Object> content);

    Map<String, Object> getWhere();

    SqlConfig setWhere(Map<String, Object> where);

    Map<String, List<String>> getCombine();

    SqlConfig setCombine(Map<String, List<String>> combine);

    Object getWhere(String key);

    Object getWhere(String key, Boolean exactMatch);

    SqlConfig putWhere(String key, Object value, Boolean prior);

    Boolean isPrepared();

    SqlConfig setPrepared(Boolean prepared);

    Boolean isMain();

    SqlConfig setMain(Boolean main);

    List<Object> getPreparedValueList();

    SqlConfig setPreparedValueList(List<Object> preparedValueList);

    List<Join> getJoinList();

    SqlConfig setJoinList(List<Join> joinList);

    Boolean hasJoin();

    String getAlias();

    SqlConfig setAlias(String alias);

    String getWhereString(Boolean hasPrefix) throws Exception;

    Boolean isKeyPrefix();

    SqlConfig setKeyPrefix(Boolean keyPrefix);
}
