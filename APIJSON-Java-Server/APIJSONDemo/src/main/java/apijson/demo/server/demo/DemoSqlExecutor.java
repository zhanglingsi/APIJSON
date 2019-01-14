package apijson.demo.server.demo;

import apijson.demo.server.dao.UserDAO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.SqlExecutor;
import com.zhangls.apijson.base.service.impl.Join;
import com.zhangls.apijson.base.service.impl.ParserHelper;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangls on 2019/1/14.
 *
 * @author zhangls
 */
@Slf4j
@Component
public class DemoSqlExecutor implements SqlExecutor {

    @Override
    public void putCache(String sql, Map<Integer, JSONObject> map, boolean isStatic) {

    }

    @Override
    public void removeCache(String sql, boolean isStatic) {

    }

    @Override
    public JSONObject getCache(String sql, int position, boolean isStatic) {
        return null;
    }

    @Override
    public ResultSet executeQuery(SqlConfig config) throws Exception {
        return null;
    }

    @Override
    public int executeUpdate(SqlConfig config) throws Exception {
        return 0;
    }

    @Override
    public void close() {

    }

    @Autowired
    private UserDAO dao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

        switch (config.getMethod()) {
            case HEAD:
            case HEADS:
                int countNum = dao.updateAll(sql);
                result = countNum > 0 ? ParserHelper.newSuccessResult() : ParserHelper.newErrorResult(new SQLException("数据库错误, rs.next() 失败！"));
                result.put(JsonApiResponse.KEY_COUNT, countNum);
                return result;
            case POST:
            case PUT:
            case DELETE:
                int delNum = dao.updateAll(sql);

                result = ParserHelper.newResult(delNum > 0 ? JsonApiResponse.CODE_SUCCESS : JsonApiResponse.CODE_NOT_FOUND
                        , delNum > 0 ? JsonApiResponse.MSG_SUCCEED : "没权限访问或对象不存在！");

                //id,id{}至少一个会有，一定会返回，不用抛异常来阻止关联写操作时前面错误导致后面无条件执行！
                if (null != config.getId()) {
                    result.put(JsonApiResponse.KEY_ID, config.getId());
                } else {
                    result.put(JsonApiResponse.KEY_ID + "[]", config.getWhere(JsonApiResponse.KEY_ID_IN, true));
                }

                result.put(JsonApiResponse.KEY_COUNT, delNum);
                return result;

            case GET:
            case GETS:
                break;

            default:
                return null;
        }


//        final int position = config.getPosition();
//        //获取缓存中的sql查询结果
//        result = getCache(sql, position, config.isCacheStatic());

//        if (result != null) {
//            log.info("【执行的SQL使用缓存中的结果】：{}", result);
//
//            return result;
//        }

        List<JSONObject> ls = jdbcTemplate.query(sql, new RowMapper<JSONObject>() {
            @Override
            public JSONObject mapRow(ResultSet rs, int num) throws SQLException {
                JSONObject result = null;
                int index = -1;

                ResultSetMetaData resultSetMetaData = rs.getMetaData();
                final int length = resultSetMetaData.getColumnCount();

                Map<String, JSONObject> childMap = Maps.newHashMap();


                boolean hasJoin = config.hasJoin();
                int viceColumnStart = length + 1;

                while (rs.next()) {
                    index++;
                    result = new JSONObject(true);

                    for (int i = 1; i <= length; i++) {

                        if (hasJoin && viceColumnStart > length && !config.getSQLTable().equalsIgnoreCase(resultSetMetaData.getTableName(i))) {
                            viceColumnStart = i;
                        }

                        result = onPutColumn(config, rs, resultSetMetaData, index, result, i, hasJoin && i >= viceColumnStart ? childMap : null);
                    }
                }

                return result;
            }
        });

        Map<Integer, JSONObject> resultMap = Maps.newLinkedHashMap();
        for (int i = 0; i < ls.size(); ++i) {
            resultMap.put(i, ls.get(i));

        }

//        Map<Integer, JSONObject> resultMap =  ls.forEach( (json) -> {
//
//        });


        long endTime = System.currentTimeMillis();
        log.info("【执行SQL语句结束时间】：{}", endTime);
        log.info("【执行SQL语句耗时】：{}", endTime - startTime);
        log.info("☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀☀");
        return resultMap.get(1);
    }

    protected JSONObject onPutColumn(@NotNull SqlConfig config, @NotNull ResultSet rs, @NotNull ResultSetMetaData rsmd
            , final int tablePosition, @NotNull JSONObject table, final int columnIndex, Map<String, JSONObject> childMap) throws SQLException {

        // 字段名称
        String name = rsmd.getColumnName(columnIndex);

        // 字段别名
        String lable = rsmd.getColumnLabel(columnIndex);

        //表名
        String tableName = rsmd.getTableName(columnIndex);

        String childTable = childMap == null ? null : tableName;

        JSONObject finalTable = null;
        String childSql = null;
        SqlConfig childConfig = null;

        if (childTable == null) {
            finalTable = table;
        } else {
            List<Join> joinList = config.getJoinList();
            if (joinList != null) {
                for (Join j : joinList) {
                    //这里用config改了getSQL后再还原很麻烦，所以提前给一个config2更好
                    childConfig = j.isAppJoin() ? null : j.getCacheConfig();

                    if (childConfig != null && childTable.equalsIgnoreCase(childConfig.getSQLTable())) {

                        childConfig.putWhere(j.getKey(), table.get(j.getTargetKey()), false);
                        try {
                            childSql = childConfig.getSQL(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (StringUtil.isEmpty(childSql, true)) {
                            return table;
                        }

                        finalTable = childMap.get(childSql);
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

        //数据库查出来的null和empty值都有意义，去掉会导致 Moment:{ @column:"content" } 部分无结果及中断数组查询！
        if (value != null) {
            if (value instanceof Timestamp) {
                value = value.toString();
            } else if (value instanceof String && isJSONType(rsmd, columnIndex)) {
                value = JSON.parse((String) value);
            }
        }

        finalTable.put(lable, value);

        return table;
    }


    /**
     * @param rsmd
     * @param position
     * @return
     */
    @Override
    public boolean isJSONType(ResultSetMetaData rsmd, int position) {
        return false;
    }
}
