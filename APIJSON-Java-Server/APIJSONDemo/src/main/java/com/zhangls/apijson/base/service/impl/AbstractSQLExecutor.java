package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.SqlExecutor;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * executor for query(read) or update(write) MySQL database
 *
 * @author Lemon
 */
@Slf4j
public abstract class AbstractSQLExecutor implements SqlExecutor {
    private static final String TAG = "SqlExecutor";

    /**
     * 缓存map
     */
    protected Map<String, Map<Integer, JSONObject>> cacheMap = Maps.newHashMap();


    /**
     * 保存缓存
     *
     * @param sql
     * @param map
     * @param isStatic
     */
    @Override
    public synchronized void putCache(String sql, Map<Integer, JSONObject> map, boolean isStatic) {
        if (sql == null || map == null) {
            return;
        }
        cacheMap.put(sql, map);
    }

    /**
     * 移除缓存
     *
     * @param sql
     * @param isStatic
     */
    @Override
    public synchronized void removeCache(String sql, boolean isStatic) {
        if (sql == null) {
            return;
        }

        cacheMap.remove(sql);
    }

    /**
     * 获取缓存
     *
     * @param sql
     * @param position
     * @param isStatic
     * @return
     */
    @Override
    public JSONObject getCache(String sql, int position, boolean isStatic) {
        Map<Integer, JSONObject> map = /** isStatic ? staticCacheMap.get(sql) : */cacheMap.get(sql);
        //只要map不为null，则如果 map.get(position) == null，则返回 {} ，避免再次SQL查询
        if (map == null) {
            return null;
        }
        JSONObject result = map.get(position);
        return result != null ? result : new JSONObject();
    }

    /**
     * 关闭连接，释放资源
     */
    @Override
    public void close() {
        cacheMap.clear();
        cacheMap = null;
    }

    /**
     * 执行SQL
     *
     * @param config
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject execute(SqlConfig config) throws Exception {

        if (config == null) {
            return null;
        }

        final String sql = config.getSQL(false);

        config.setPrepared(config.isPrepared());

        log.info("【执行SQL，SqlConfig】：{}", JsonApi.toJSONString(config));

        if (StringUtil.isEmpty(sql, true)) {
            return null;
        }

        JSONObject result = null;

        long startTime = System.currentTimeMillis();
        log.info("☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀");
        log.info("【执行SQL语句开始时间】：{}", startTime);

        ResultSet rs = null;
        switch (config.getMethod()) {
            case HEAD:
            case HEADS:
                rs = executeQuery(config);

                result = rs.next() ? AbstractParser.newSuccessResult() : AbstractParser.newErrorResult(new SQLException("数据库错误, rs.next() 失败！"));
                result.put(JsonApiResponse.KEY_COUNT, rs.getLong(1));

                rs.close();
                return result;

            case POST:
            case PUT:
            case DELETE:
                long updateCount = executeUpdate(config);

                result = AbstractParser.newResult(updateCount > 0 ? JsonApiResponse.CODE_SUCCESS : JsonApiResponse.CODE_NOT_FOUND
                        , updateCount > 0 ? JsonApiResponse.MSG_SUCCEED : "没权限访问或对象不存在！");

                //id,id{}至少一个会有，一定会返回，不用抛异常来阻止关联写操作时前面错误导致后面无条件执行！
                if (config.getId() != null) {
                    result.put(JsonApiResponse.KEY_ID, config.getId());
                } else {
                    result.put(JsonApiResponse.KEY_ID + "[]", config.getWhere(JsonApiResponse.KEY_ID_IN, true));
                }
                //返回修改的记录数
                result.put(JsonApiResponse.KEY_COUNT, updateCount);
                return result;

            case GET:
            case GETS:
                break;

            default:
                return null;
        }


        final int position = config.getPosition();
        //获取缓存中的sql查询结果
        result = getCache(sql, position, config.isCacheStatic());

        if (result != null) {
            log.info("【执行的SQL使用缓存中的结果】：{}", result);

            return result;
        }

        rs = executeQuery(config);

        Map<Integer, JSONObject> resultMap = Maps.newHashMap();

        int index = -1;

        ResultSetMetaData rsmd = rs.getMetaData();
        final int length = rsmd.getColumnCount();

        Map<String, JSONObject> childMap = Maps.newHashMap();


        boolean hasJoin = config.hasJoin();
        int viceColumnStart = length + 1;
        while (rs.next()) {
            index++;
            result = new JSONObject(true);

            for (int i = 1; i <= length; i++) {

                if (hasJoin && viceColumnStart > length && config.getSQLTable().equalsIgnoreCase(rsmd.getTableName(i)) == false) {
                    viceColumnStart = i;
                }

                result = onPutColumn(config, rs, rsmd, index, result, i, hasJoin && i >= viceColumnStart ? childMap : null);
            }

            resultMap = onPutTable(config, rs, rsmd, resultMap, index, result);
        }

        rs.close();


        //TODO @ APP JOIN 查询副表并缓存到 childMap <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        executeAppJoin(config, resultMap, childMap);

        //TODO @ APP JOIN 查询副表并缓存到 childMap >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        //子查询 SELECT Moment.*, Comment.id 中的 Comment 内字段
        Set<Entry<String, JSONObject>> set = childMap.entrySet();

        //<sql, Table>
        for (Entry<String, JSONObject> entry : set) {
            Map<Integer, JSONObject> m = Maps.newHashMap();
            m.put(0, entry.getValue());
            putCache(entry.getKey(), m, false);
        }


        putCache(sql, resultMap, config.isCacheStatic());

        long endTime = System.currentTimeMillis();
        log.info("【执行SQL语句结束时间】：{}", endTime);
        log.info("【执行SQL语句耗时】：{}", endTime - startTime);
        log.info("☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀");
        return resultMap.get(position);
    }


    /**
     * TODO @ APP JOIN 查询副表并缓存到 childMap
     *
     * @param config
     * @param resultMap
     * @param childMap
     * @throws Exception
     */
    protected void executeAppJoin(SqlConfig config, Map<Integer, JSONObject> resultMap, Map<String, JSONObject> childMap) throws Exception {
        List<Join> joinList = config.getJoinList();
        if (joinList != null) {

            SqlConfig jc;
            SqlConfig cc;

            for (Join j : joinList) {
                if (j.isAppJoin() == false) {
                    continue;
                }

                jc = j.getJoinConfig();
                cc = j.getCacheConfig(); //这里用config改了getSQL后再还原很麻烦，所以提前给一个config2更好

                //取出 "id@": "@/User/userId" 中所有 userId 的值
                List<Object> targetValueList = new ArrayList<>();
                JSONObject mainTable;
                Object targetValue;

                for (int i = 0; i < resultMap.size(); i++) {
                    mainTable = resultMap.get(i);
                    targetValue = mainTable == null ? null : mainTable.get(j.getTargetKey());

                    if (targetValue != null && targetValueList.contains(targetValue) == false) {
                        targetValueList.add(targetValue);
                    }
                }


                //替换为 "id{}": [userId1, userId2, userId3...]
                jc.putWhere(j.getOriginKey(), null, false);
                jc.putWhere(j.getKey() + "{}", targetValueList, false);

                jc.setMain(true).setPreparedValueList(new ArrayList<>());

                boolean prepared = jc.isPrepared();
                final String sql = jc.getSQL(false);
                jc.setPrepared(prepared);

                if (StringUtil.isEmpty(sql, true)) {
                    throw new NullPointerException(TAG + ".executeAppJoin  StringUtil.isEmpty(sql, true) >> return null;");
                }

                long startTime = System.currentTimeMillis();

                //执行副表的批量查询 并 缓存到 childMap
                ResultSet rs = executeQuery(jc);

                int index = -1;

                ResultSetMetaData rsmd = rs.getMetaData();
                final int length = rsmd.getColumnCount();

                JSONObject result;
                String cacheSql;
                while (rs.next()) { //FIXME 同时有 @ APP JOIN 和 < 等 SQL JOIN 时，next = false 总是无法进入循环，导致缓存失效，可能是连接池或线程问题
                    index++;

                    result = new JSONObject(true);

                    for (int i = 1; i <= length; i++) {

                        result = onPutColumn(jc, rs, rsmd, index, result, i, null);
                    }

                    //每个 result 都要用新的 SQL 来存 childResultMap = onPutTable(config, rs, rsmd, childResultMap, index, result);


                    //缓存到 childMap
                    cc.putWhere(j.getKey(), result.get(j.getKey()), false);
                    cacheSql = cc.getSQL(false);
                    childMap.put(cacheSql, result);

                }

                rs.close();


                long endTime = System.currentTimeMillis();

            }
        }

    }


    /**
     * table.put(rsmd.getColumnLabel(i), rs.getObject(i));
     *
     * @param config
     * @param rs
     * @param rsmd
     * @param tablePosition 从0开始
     * @param table
     * @param columnIndex   从1开始
     * @param childMap
     * @return result
     * @throws Exception
     */
    protected JSONObject onPutColumn(@NotNull SqlConfig config, @NotNull ResultSet rs, @NotNull ResultSetMetaData rsmd
            , final int tablePosition, @NotNull JSONObject table, final int columnIndex, Map<String, JSONObject> childMap) throws Exception {

        if (rsmd.getColumnName(columnIndex).startsWith("_")) {
            return table;
        }

        //已改为  rsmd.getTableName(columnIndex) 支持副表不传 @column ， 但如何判断是副表？childMap != null
        //		String lable = rsmd.getColumnLabel(columnIndex);
        //		int dotIndex = lable.indexOf(".");
        String lable = rsmd.getColumnLabel(columnIndex);//dotIndex < 0 ? lable : lable.substring(dotIndex + 1);

        String childTable = childMap == null ? null : rsmd.getTableName(columnIndex); //dotIndex < 0 ? null : lable.substring(0, dotIndex);

        JSONObject finalTable = null;
        String childSql = null;
        SqlConfig childConfig = null;

        if (childTable == null) {
            finalTable = table;
        } else {
            //			lable = column;

            //<sql, Table>

            List<Join> joinList = config.getJoinList();
            if (joinList != null) {
                for (Join j : joinList) {
                    childConfig = j.isAppJoin() ? null : j.getCacheConfig(); //这里用config改了getSQL后再还原很麻烦，所以提前给一个config2更好

                    if (childConfig != null && childTable.equalsIgnoreCase(childConfig.getSQLTable())) {

                        childConfig.putWhere(j.getKey(), table.get(j.getTargetKey()), false);
                        childSql = childConfig.getSQL(false);

                        if (StringUtil.isEmpty(childSql, true)) {
                            return table;
                        }

                        finalTable = (JSONObject) childMap.get(childSql);
                        break;
                    }
                }
            }

            if (finalTable == null) {
                finalTable = new JSONObject(true);
                childMap.put(childSql, finalTable);
            }
        }

        Object value = rs.getObject(columnIndex);
        //					Log.d(TAG, "name:" + rsmd.getColumnName(i));
        //					Log.d(TAG, "lable:" + rsmd.getColumnLabel(i));
        //					Log.d(TAG, "type:" + rsmd.getColumnType(i));
        //					Log.d(TAG, "typeName:" + rsmd.getColumnTypeName(i));

        //				Log.i(TAG, "select  while (rs.next()) { >> for (int i = 0; i < length; i++) {"
        //						+ "\n  >>> value = " + value);

        if (value != null) { //数据库查出来的null和empty值都有意义，去掉会导致 Moment:{ @column:"content" } 部分无结果及中断数组查询！
            if (value instanceof Timestamp) {
                value = ((Timestamp) value).toString();
            } else if (value instanceof String && isJSONType(rsmd, columnIndex)) { //json String
                value = JSON.parse((String) value);
            }
        }

        finalTable.put(lable, value);

        return table;
    }

    /**
     * resultMap.put(position, table);
     *
     * @param config
     * @param rs
     * @param rsmd
     * @param resultMap
     * @param position
     * @param table
     * @return resultMap
     */
    protected Map<Integer, JSONObject> onPutTable(@NotNull SqlConfig config, @NotNull ResultSet rs, @NotNull ResultSetMetaData rsmd
            , @NotNull Map<Integer, JSONObject> resultMap, int position, @NotNull JSONObject table) {

        resultMap.put(position, table);
        return resultMap;
    }


    /**
     * 判断是否为JSON类型
     *
     * @param rsmd
     * @param position
     * @return
     */
    @Override
    public boolean isJSONType(ResultSetMetaData rsmd, int position) {
        try {
            //TODO CHAR和JSON类型的字段，getColumnType返回值都是1	，如果不用CHAR，改用VARCHAR，则可以用上面这行来提高性能。
            //return rsmd.getColumnType(position) == 1;
            return rsmd.getColumnTypeName(position).toLowerCase().contains("json");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
