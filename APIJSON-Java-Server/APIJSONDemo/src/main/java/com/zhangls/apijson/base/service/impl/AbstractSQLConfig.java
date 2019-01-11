package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiObject;
import com.zhangls.apijson.base.SqlOper;
import com.zhangls.apijson.base.exception.NotExistException;
import com.zhangls.apijson.base.model.Column;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.model.RequestRole;
import com.zhangls.apijson.base.model.Table;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * config sql for JSON Request
 *
 * @author Lemon
 */
@Slf4j
public abstract class AbstractSQLConfig implements SqlConfig {
    private static final String TAG = "SqlConfig";


    /**
     * 表名映射，隐藏真实表名，对安全要求很高的表可以这么做
     */
    public static final Map<String, String> TABLE_KEY_MAP;

    static {
        TABLE_KEY_MAP = Maps.newHashMap();
        TABLE_KEY_MAP.put(Table.class.getSimpleName(), Table.TAG);
        TABLE_KEY_MAP.put(Column.class.getSimpleName(), Column.TAG);
    }


    /**
     * 表主键ID
     */
    private Object id;

    /**
     * 操作方法 http请求类型
     */
    private RequestMethod method;

    /**
     * 预编译
     */
    private Boolean prepared = Boolean.TRUE;
    private Boolean main = Boolean.TRUE;
    /**
     * 发送请求的用户的角色 TODO 被关联的表通过就忽略关联的表？(这个不行 User:{"sex@":"/Comment/toId"})
     */
    private RequestRole role;

    /**
     * 表所在的数据库类型
     */
    private String database; //表所在的数据库类型
    private String schema; //表所在的数据库名
    private String table; //表名
    private String alias; //表别名
    private String group; //分组方式的字符串数组，','分隔
    private String having; //聚合函数的字符串数组，','分隔
    private String order; //排序方式的字符串数组，','分隔
    private List<String> column; //表内字段名(或函数名，仅查询操作可用)的字符串数组，','分隔
    private List<List<Object>> values; //对应表内字段的值的字符串数组，','分隔
    private Map<String, Object> content; //Request内容，key:value形式，column = content.keySet()，values = content.values()
    private Map<String, Object> where; //筛选条件，key:value形式
    private Map<String, List<String>> combine; //条件组合，{ "&":[key], "|":[key], "!":[key] }


    private Integer count; //Table数量
    private Integer page; //Table所在页码
    private Integer position; //Table在[]中的位置
    private Integer query; //JSONRequest.query
    private Integer type; //ObjectParser.type
    private List<Join> joinList; //joinList

    private boolean test; //测试
    private boolean cacheStatic; //静态缓存

    public AbstractSQLConfig(RequestMethod method) {
        setMethod(method);
    }

    public AbstractSQLConfig(RequestMethod method, String table) {
        this(method);
        setTable(table);
    }

    public AbstractSQLConfig(RequestMethod method, Integer count, Integer page) {
        this(method);
        setCount(count);
        setPage(page);
    }

    @NotNull
    @Override
    public RequestMethod getMethod() {
        if (method == null) {
            method = RequestMethod.GET;
        }
        return method;
    }

    @Override
    public AbstractSQLConfig setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    @Override
    public Boolean isPrepared() {
        return prepared;
    }

    @Override
    public AbstractSQLConfig setPrepared(Boolean prepared) {
        this.prepared = prepared;
        return this;
    }

    @Override
    public Boolean isMain() {
        return main;
    }

    @Override
    public AbstractSQLConfig setMain(Boolean main) {
        this.main = main;
        return this;
    }


    @Override
    public Object getId() {
        return id;
    }

    @Override
    public AbstractSQLConfig setId(Object id) {
        this.id = id;
        return this;
    }

    @Override
    public RequestRole getRole() {
        //不能 @NotNull , AbstractParser#getSQLObject 内当getRole() == null时填充默认值
        return role;
    }

    public AbstractSQLConfig setRole(String roleName) throws Exception {
        return setRole(RequestRole.get(roleName));
    }

    @Override
    public AbstractSQLConfig setRole(RequestRole role) {
        this.role = role;
        return this;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public SqlConfig setDatabase(String database) {
        this.database = database;
        return this;
    }

    @Override
    public String getQuote() {
        return DATABASE_POSTGRESQL.equalsIgnoreCase(getDatabase()) ? "\"" : "`";
    }

    @Override
    public String getSchema() {
        String sqlTable = getSQLTable();
        if (StringUtil.isEmpty(schema, true) && (Table.TAG.equals(sqlTable) || Column.TAG.equals(sqlTable))) {
            return SCHEMA_INFORMATION;
        }
        return schema;
    }

    @Override
    public AbstractSQLConfig setSchema(String schema) {
        if (schema != null) {
            String quote = getQuote();
            String s = schema.startsWith(quote) && schema.endsWith(quote) ? schema.substring(1, schema.length() - 1) : schema;
            if (StringUtil.isName(s) == false) {
                throw new IllegalArgumentException("@schema:value 中value必须是1个单词！");
            }
        }
        this.schema = schema;
        return this;
    }

    /**
     * 请求传进来的Table名
     *
     * @return
     * @see {@link #getSQLTable()}
     */
    @Override
    public String getTable() {
        return table;
    }

    /**
     * 数据库里的真实Table名
     * 通过 {@link #TABLE_KEY_MAP} 映射
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public String getSQLTable() {
        String t = TABLE_KEY_MAP.containsKey(table) ? TABLE_KEY_MAP.get(table) : table;
        return DATABASE_POSTGRESQL.equalsIgnoreCase(getDatabase()) ? t.toLowerCase() : t;
    }

    @JSONField(serialize = false)
    @Override
    public String getTablePath() {
        String q = getQuote();
        return q + getSchema() + q + "." + q + getSQLTable() + q + (isKeyPrefix() ? " AS " + getAlias() : "");
    }

    @Override
    public AbstractSQLConfig setTable(String table) { //Table已经在Parser中校验，所以这里不用防SQL注入
        this.table = table;
        return this;
    }

    @Override
    public String getAlias() {
        if (StringUtil.isEmpty(alias, true)) {
            alias = getTable();
        }
        String q = getQuote();
        //getTable 不能小写，因为Verifier用大小写敏感的名称判断权限
        return q + (DATABASE_POSTGRESQL.equalsIgnoreCase(getDatabase()) ? alias.toLowerCase() : alias) + q;
    }

    @Override
    public AbstractSQLConfig setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public AbstractSQLConfig setGroup(String... keys) {
        return setGroup(StringUtil.getString(keys));
    }

    @Override
    public AbstractSQLConfig setGroup(String group) {
        this.group = group;
        return this;
    }

    @JSONField(serialize = false)
    public String getGroupString() {
        //TODO 加上子表的group

        group = StringUtil.getTrimedString(group);
        if (group.isEmpty()) {
            return "";
        }

        String[] keys = StringUtil.split(group);
        if (keys == null || keys.length <= 0) {
            return "";
        }

        for (Integer i = 0; i < keys.length; i++) {
            //不能通过 ? 来代替，因为SQLExecutor statement.setString后 GROUP BY 'userId' 有单引号，只能返回一条数据，必须去掉单引号才行！
            if (isPrepared()) {
                if (StringUtil.isName(keys[i]) == false) {
                    throw new IllegalArgumentException("@group:value 中 value里面用 , 分割的每一项都必须是1个单词！并且不要有空格！");
                }
            }

            keys[i] = getKey(keys[i]);
        }

        return " GROUP BY " + StringUtil.getString(keys);
    }

    @Override
    public String getHaving() {
        return having;
    }

    public AbstractSQLConfig setHaving(String... conditions) {
        return setHaving(StringUtil.getString(conditions));
    }

    @Override
    public AbstractSQLConfig setHaving(String having) {
        this.having = having;
        return this;
    }

    /**
     * @return HAVING conditoin0 SqlOper.AND condition1 SqlOper.OR condition2 ...
     */
    @JSONField(serialize = false)
    public String getHavingString() {
        having = StringUtil.getTrimedString(having);
        if (having.isEmpty()) {
            return "";
        }

        String[] keys = StringUtil.split(having, ";");
        if (keys == null || keys.length <= 0) {
            return "";
        }

        String expression;
        String method;
        //暂时不允许 String prefix;
        String suffix;

        //fun0(arg0,arg1,...);fun1(arg0,arg1,...)
        for (Integer i = 0; i < keys.length; i++) {

            //fun(arg0,arg1,...)
            expression = keys[i];

            Integer start = expression.indexOf("(");
            if (start < 0) {
                if (isPrepared() && PATTERN_HAVING.matcher(expression).matches() == false) {
                    throw new UnsupportedOperationException("字符串 " + expression + " 不合法！"
                            + "预编译模式下 @having:\"column?value;function(arg0,arg1,...)?value...\""
                            + " 中 column?value 必须符合正则表达式 ^[A-Za-z0-9%!=<>]+$ ！不允许空格！");
                }
                continue;
            }

            Integer end = expression.indexOf(")");
            if (start >= end) {
                throw new IllegalArgumentException("字符 " + expression + " 不合法！"
                        + "@having:value 中 value 里的 SQL函数必须为 function(arg0,arg1,...) 这种格式！");
            }

            method = expression.substring(0, start);

            if (StringUtil.isName(method) == false) {
                throw new IllegalArgumentException("字符 " + method + " 不合法！"
                        + "预编译模式下 @having:\"column?value;function(arg0,arg1,...)?value...\""
                        + " 中SQL函数名 function 必须符合正则表达式 ^[0-9a-zA-Z_]+$ ！");
            }

            suffix = expression.substring(end + 1, expression.length());

            if (isPrepared() && PATTERN_HAVING_SUFFIX.matcher((String) suffix).matches() == false) {
                throw new UnsupportedOperationException("字符串 " + suffix + " 不合法！"
                        + "预编译模式下 @having:\"column?value;function(arg0,arg1,...)?value...\""
                        + " 中 ?value 必须符合正则表达式 ^[0-9%!=<>]+$ ！不允许空格！");
            }

            String[] ckeys = StringUtil.split(expression.substring(start + 1, end));

            if (ckeys != null) {
                for (Integer j = 0; j < ckeys.length; j++) {

                    if (isPrepared() && (StringUtil.isName(ckeys[j]) == false || ckeys[j].startsWith("_"))) {
                        throw new IllegalArgumentException("字符 " + ckeys[j] + " 不合法！"
                                + "预编译模式下 @having:\"column?value;function(arg0,arg1,...)?value...\""
                                + " 中所有 arg 都必须是1个不以 _ 开头的单词！并且不要有空格！");
                    }

                    ckeys[j] = getKey(ckeys[j]);
                }
            }

            keys[i] = method + "(" + StringUtil.getString(ckeys) + ")" + suffix;
        }

        return " HAVING " + StringUtil.getString(keys, SqlOper.AND); //TODO 支持 SqlOper.OR, SqlOper.NOT 参考 @combine:"&key0,|key1,!key2"
    }

    @Override
    public String getOrder() {
        return order;
    }

    public AbstractSQLConfig setOrder(String... conditions) {
        return setOrder(StringUtil.getString(conditions));
    }

    @Override
    public AbstractSQLConfig setOrder(String order) {
        this.order = order;
        return this;
    }

    @JSONField(serialize = false)
    public String getOrderString() {
        //TODO 加上子表的order

        order = StringUtil.getTrimedString(order);
        if (order.isEmpty()) {
            return "";
        }
        if (order.contains("+")) {//replace没有包含的replacement会崩溃
            order = order.replaceAll("\\+", " ASC ");
        }
        if (order.contains("-")) {
            order = order.replaceAll("-", " DESC ");
        }

        //TODO  column, order, group 都改用 List<String> 存储！！！，并且每个字段都要加 Table. 前缀！
        String[] keys = StringUtil.split(order);
        if (keys == null || keys.length <= 0) {
            return "";
        }

        String origin;
        String sort;
        Integer index;
        for (Integer i = 0; i < keys.length; i++) {
            index = keys[i].trim().endsWith(" ASC") ? keys[i].lastIndexOf(" ASC") : -1; //StringUtil.split返回数组中，子项不会有null
            if (index < 0) {
                index = keys[i].trim().endsWith(" DESC") ? keys[i].lastIndexOf(" DESC") : -1;
                sort = index <= 0 ? "" : " DESC ";
            } else {
                sort = " ASC ";
            }
            origin = index < 0 ? keys[i] : keys[i].substring(0, index);

            if (isPrepared()) { //不能通过 ? 来代替，SELECT 'id','name' 返回的就是 id:"id", name:"name"，而不是数据库里的值！
                //这里既不对origin trim，也不对 ASC/DESC ignoreCase，希望前端严格传没有任何空格的字符串过来，减少传输数据量，节约服务器性能
                if (StringUtil.isName(origin) == false) {
                    throw new IllegalArgumentException("预编译模式下 @order:value 中 value里面用 , 分割的每一项"
                            + " column+ / column- 中 column必须是1个单词！并且不要有多余的空格！");
                }
            }
            keys[i] = getKey(origin) + sort;
        }

        return " ORDER BY " + StringUtil.getString(keys);
    }

    @Override
    public List<List<Object>> getValues() {
        return values;
    }

    @Override
    public List<String> getColumn() {
        return column;
    }

    @Override
    public AbstractSQLConfig setColumn(List<String> column) {
        this.column = column;
        return this;
    }

    @JSONField(serialize = false)
    public String getColumnString() throws Exception {
        switch (getMethod()) {
            case HEAD:
            case HEADS:
                if (isPrepared() && column != null) {
                    for (String c : column) {
                        if (StringUtil.isName(c) == false) {
                            throw new IllegalArgumentException("HEAD请求: @column:value 中 value里面用 , 分割的每一项都必须是1个单词！");
                        }
                    }
                }
                return SqlOper.count(column != null && column.size() == 1 ? column.get(0) : "*");
            case POST:
                if (column == null || column.isEmpty()) {
                    throw new IllegalArgumentException("POST 请求必须在Table内设置要保存的 key:value ！");
                }

                if (isPrepared()) {
                    for (String c : column) {
                        if (StringUtil.isName(c) == false) {
                            throw new IllegalArgumentException("POST请求: 每一个 key:value 中的key都必须是1个单词！");
                        }
                    }
                }

                return "(" + StringUtil.getString(column.toArray()) + ")";
            case GET:
            case GETS:
                Boolean isQuery = RequestMethod.isQueryMethod(method);
                String joinColumn = "";
                if (isQuery && joinList != null) {
                    SqlConfig c;
                    Boolean first = true;
                    for (Join j : joinList) {
                        if (j.isAppJoin()) {
                            continue;
                        }

                        c = j.getJoinConfig();
                        c.setAlias(c.getTable());
                        joinColumn += (first ? "" : ", ") + ((AbstractSQLConfig) c).getColumnString();

                        first = false;
                    }
                }

                String tableAlias = getAlias();

                String[] keys = column == null ? null : column.toArray(new String[]{});

                if (keys == null || keys.length <= 0) {
                    return isKeyPrefix() == false ? "*" : (tableAlias + ".*" + (StringUtil.isEmpty(joinColumn, true) ? "" : ", " + joinColumn));
                }

                String expression;
                String method = null;

                for (Integer i = 0; i < keys.length; i++) {
                    expression = keys[i];

                    Integer start = expression.indexOf("(");
                    Integer end = 0;
                    if (start >= 0) {
                        end = expression.indexOf(")");
                        if (start >= end) {
                            throw new IllegalArgumentException("字符 " + expression + " 不合法！"
                                    + "@having:value 中 value 里的 SQL函数必须为 function(arg0,arg1,...) 这种格式！");
                        }

                        method = expression.substring(0, start);

                        if (StringUtil.isName(method) == false) {
                            throw new IllegalArgumentException("字符 " + method + " 不合法！"
                                    + "预编译模式下 @column:\"column0,column1:alias;function0(arg0,arg1,...);function1(...):alias...\""
                                    + " 中SQL函数名 function 必须符合正则表达式 ^[0-9a-zA-Z_]+$ ！");
                        }
                    }

                    Boolean isColumn = start < 0;

                    String[] ckeys = StringUtil.split(isColumn ? expression : expression.substring(start + 1, end));
                    String quote = getQuote();

                    if (ckeys != null && ckeys.length > 0) {

                        String origin;
                        String alias;
                        Integer index;
                        for (Integer j = 0; j < ckeys.length; j++) {
                            index = ckeys[j].lastIndexOf(":");
                            origin = index < 0 ? ckeys[j] : ckeys[j].substring(0, index);
                            alias = index < 0 ? null : ckeys[j].substring(index + 1);

                            if (isPrepared()) {
                                if (isColumn) {
                                    if (StringUtil.isName(origin) == false || (alias != null && StringUtil.isName(alias) == false)) {
                                        throw new IllegalArgumentException("GET请求: 预编译模式下 @column:value 中 value里面用 , 分割的每一项"
                                                + " column:alias 中 column 必须是1个单词！如果有alias，则alias也必须为1个单词！并且不要有多余的空格！");
                                    }
                                } else {
                                    if ((StringUtil.isName(ckeys[j]) == false || ckeys[j].startsWith("_"))) {
                                        throw new IllegalArgumentException("字符 " + ckeys[j] + " 不合法！"
                                                + "预编译模式下 @column:\"column0,column1:alias;function0(arg0,arg1,...);function1(...):alias...\""
                                                + " 中所有 arg 都必须是1个不以 _ 开头的单词！并且不要有空格！");
                                    }
                                }
                            }

                            origin = quote + origin + quote;
                            if (isKeyPrefix()) {
                                ckeys[j] = tableAlias + "." + origin;
                                if (isColumn && StringUtil.isEmpty(alias, true) == false) {
                                    ckeys[j] += " AS " + quote + alias + quote;
                                }
                            } else {
                                ckeys[j] = origin + (StringUtil.isEmpty(alias, true) ? "" : " AS " + quote + alias + quote);
                            }
                        }

                    }

                    if (isColumn) {
                        keys[i] = StringUtil.getString(ckeys);
                    } else {
                        String suffix = expression.substring(end + 1, expression.length());
                        String alias = suffix.startsWith(":") ? suffix.substring(1) : null;

                        if (StringUtil.isEmpty(alias, true)) {
                            if (suffix.isEmpty() == false) {
                                throw new IllegalArgumentException("GET请求: 预编译模式下 @column:value 中 value里面用 ; 分割的每一项"
                                        + " function(arg0,arg1,...):alias 中 alias 如果有就必须是1个单词！并且不要有多余的空格！");
                            }
                        } else {
                            if (StringUtil.isEmpty(alias, true) == false && StringUtil.isName(alias) == false) {
                                throw new IllegalArgumentException("GET请求: 预编译模式下 @column:value 中 value里面用 ; 分割的每一项"
                                        + " function(arg0,arg1,...):alias 中 alias 必须是1个单词！并且不要有多余的空格！");
                            }
                        }


                        String origin = method + "(" + StringUtil.getString(ckeys) + ")";
                        keys[i] = origin + (StringUtil.isEmpty(alias, true) ? "" : " AS " + quote + alias + quote);
                    }

                }

                String c = StringUtil.getString(keys);

                return (!c.contains(":") ? c : c.replaceAll(":", " AS ")) + (StringUtil.isEmpty(joinColumn, true) ? "" : ", " + joinColumn);

            default:
                throw new UnsupportedOperationException(
                        "服务器内部错误：getColumnString 不支持 " + RequestMethod.getName(getMethod())
                                + " 等 [GET,GETS,HEAD,HEADS,POST] 外的ReuqestMethod！"
                );
        }
    }


    @JSONField(serialize = false)
    public String getValuesString() {
        String s = "";
        if (values != null && values.size() > 0) {
            Object[] items = new Object[values.size()];
            List<Object> vs;
            for (Integer i = 0; i < values.size(); i++) {
                vs = values.get(i);
                if (vs == null) {
                    continue;
                }

                items[i] = "(";
                for (Integer j = 0; j < vs.size(); j++) {
                    items[i] += ((j <= 0 ? "" : ",") + getValue(vs.get(j)));
                }
                items[i] += ")";
            }
            s = StringUtil.getString(items);
        }
        return s;
    }

    @Override
    public AbstractSQLConfig setValues(List<List<Object>> valuess) {
        this.values = valuess;
        return this;
    }

    @Override
    public Map<String, Object> getContent() {
        return content;
    }

    @Override
    public AbstractSQLConfig setContent(Map<String, Object> content) {
        this.content = content;
        return this;
    }

    @Override
    public Integer getCount() {
        return count;
    }

    @Override
    public AbstractSQLConfig setCount(Integer count) {
        this.count = count;
        return this;
    }

    @Override
    public Integer getPage() {
        return page;
    }

    @Override
    public AbstractSQLConfig setPage(Integer page) {
        this.page = page;
        return this;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public AbstractSQLConfig setPosition(Integer position) {
        this.position = position;
        return this;
    }

    @Override
    public Integer getQuery() {
        return query;
    }

    @Override
    public AbstractSQLConfig setQuery(Integer query) {
        this.query = query;
        return this;
    }

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public AbstractSQLConfig setType(Integer type) {
        this.type = type;
        return this;
    }

    @Override
    public List<Join> getJoinList() {
        return joinList;
    }

    @Override
    public SqlConfig setJoinList(List<Join> joinList) {
        this.joinList = joinList;
        return this;
    }

    @Override
    public Boolean hasJoin() {
        return joinList != null && joinList.isEmpty() == false;
    }

    @Override
    public boolean isTest() {
        return test;
    }

    @Override
    public AbstractSQLConfig setTest(boolean test) {
        this.test = test;
        return this;
    }

    @Override
    public boolean isCacheStatic() {
        return cacheStatic;
    }

    @Override
    public AbstractSQLConfig setCacheStatic(boolean cacheStatic) {
        this.cacheStatic = cacheStatic;
        return this;
    }


    /**
     * 获取初始位置offset
     *
     * @return
     */
    @JSONField(serialize = false)
    public Integer getOffset() {
        return getOffset(getPage(), getCount());
    }

    /**
     * 获取初始位置offset
     *
     * @param page
     * @param count
     * @return
     */
    public static Integer getOffset(Integer page, Integer count) {
        return page * count;
    }

    /**
     * 获取限制数量
     *
     * @return
     */
    @JSONField(serialize = false)
    public String getLimitString() {
        return getLimitString(getPage(), getCount());
    }

    /**
     * 获取限制数量
     */
    public static String getLimitString(Integer page, Integer count) {
        return count <= 0 ? "" : " LIMIT " + count + " OFFSET " + getOffset(page, count);
    }


    @Override
    public Map<String, Object> getWhere() {
        return where;
    }

    @Override
    public AbstractSQLConfig setWhere(Map<String, Object> where) {
        this.where = where;
        return this;
    }

    @NotNull
    @Override
    public Map<String, List<String>> getCombine() {
        List<String> andList = combine == null ? null : combine.get("&");
        if (andList == null) {
            andList = where == null ? Lists.newArrayList() : Lists.newArrayList(where.keySet());
            if (combine == null) {
                combine = Maps.newHashMap();
            }
            combine.put("&", andList);
        }
        return combine;
    }

    @Override
    public AbstractSQLConfig setCombine(Map<String, List<String>> combine) {
        this.combine = combine;
        return this;
    }

    /**
     * noFunctionChar = false
     *
     * @param key
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public Object getWhere(String key) {
        return getWhere(key, false);
    }

    /**
     * @param key
     * @param exactMatch
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public Object getWhere(String key, Boolean exactMatch) {
        if (exactMatch) {
            return where == null ? null : where.get(key);
        }

        Set<String> set = key == null || where == null ? null : where.keySet();
        if (set != null) {
            synchronized (where) {
                if (where != null) {
                    Integer index;
                    for (String k : set) {
                        index = k.indexOf(key);
                        if (index >= 0 && StringUtil.isName(k.substring(index)) == false) {
                            return where.get(k);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public AbstractSQLConfig putWhere(String key, Object value, Boolean prior) {
        if (key != null) {
            if (where == null) {
                where = new LinkedHashMap<String, Object>();
            }
            if (value == null) {
                where.remove(key);
            } else {
                where.put(key, value);
            }

            combine = getCombine();
            List<String> andList = combine.get("&");
            if (value == null) {
                andList.remove(key);
            } else if (andList == null || andList.contains(key) == false) {
                Integer i = 0;
                if (andList == null) {
                    andList = new ArrayList<>();
                } else if (prior && andList.isEmpty() == false) {
                    if (andList.contains(JsonApiObject.KEY_ID)) {
                        i++;
                    }
                    if (andList.contains(JsonApiObject.KEY_ID_IN)) {
                        i++;
                    }
                    if (andList.contains(JsonApiObject.KEY_USER_ID)) {
                        i++;
                    }
                    if (andList.contains(JsonApiObject.KEY_USER_ID_IN)) {
                        i++;
                    }
                }

                if (prior) {
                    //userId的优先级不能比id高  0, key);
                    andList.add(i, key);
                } else {
                    //AbstractSQLExecutor.onPutColumn里getSQL，要保证缓存的SQL和查询的SQL里 where 的 key:value 顺序一致
                    andList.add(key);
                }
            }
            combine.put("&", andList);
        }
        return this;
    }

    /**
     * 获取WHERE
     *
     * @return
     * @throws Exception
     */
    @JSONField(serialize = false)
    @Override
    public String getWhereString(Boolean hasPrefix) throws Exception {
        return getWhereString(hasPrefix, getMethod(), getWhere(), getCombine(), getJoinList(), !isTest());
    }

    /**
     * 获取WHERE
     *
     * @param method
     * @param where
     * @return
     * @throws Exception
     */
    @JSONField(serialize = false)
    public String getWhereString(Boolean hasPrefix, RequestMethod method, Map<String, Object> where, Map<String, List<String>> combine, List<Join> joinList, Boolean verifyName) throws Exception {
        Set<Entry<String, List<String>>> combineSet = combine == null ? null : combine.entrySet();
        if (combineSet == null || combineSet.isEmpty()) {
            return "";
        }

        List<String> keyList;

        String whereString = "";

        Boolean isCombineFirst = true;
        Integer logic;

        Boolean isItemFirst;
        String c;
        String cs;

        for (Entry<String, List<String>> ce : combineSet) {
            keyList = ce == null ? null : ce.getValue();
            if (keyList == null || keyList.isEmpty()) {
                continue;
            }

            if ("|".equals(ce.getKey())) {
                logic = Logic.TYPE_OR;
            } else if ("!".equals(ce.getKey())) {
                logic = Logic.TYPE_NOT;
            } else {
                logic = Logic.TYPE_AND;
            }


            isItemFirst = true;
            cs = "";
            for (String key : keyList) {
                c = getWhereItem(key, where.get(key), method, verifyName);

                if (StringUtil.isEmpty(c, true)) {
                    continue;
                }

                cs += (isItemFirst ? "" : (Logic.isAnd(logic) ? SqlOper.AND : SqlOper.OR)) + "(" + c + ")";

                isItemFirst = false;
            }


            whereString += (isCombineFirst ? "" : SqlOper.AND) + (Logic.isNot(logic) ? SqlOper.NOT : "") + " (  " + cs + "  ) ";
            isCombineFirst = false;
        }


        if (joinList != null) {

            String newWs = "";
            String ws = "" + whereString;

            List<Object> newPvl = new ArrayList<>();
            List<Object> pvl = new ArrayList<>(preparedValueList);

            SqlConfig jc;
            String js;
            //各种 JOIN 没办法统一用 & | ！连接，只能按优先级，和 @combine 一样?
            for (Join j : joinList) {
                switch (j.getJoinType()) {
                    case "@": // APP JOIN
                        newWs = whereString; //解决 生成的 SQL 里 where = null
                        newPvl = preparedValueList;  //解决总是 preparedValueList = new ArrayList
                        break;

                    case "<": // LEFT JOIN
                    case ">": // RIGHT JOIN
                        break;

                    case "":  // FULL JOIN
                    case "|": // FULL JOIN  不支持 <>, [] ，避免太多符号
                    case "&": // INNER JOIN
                    case "!": // OUTTER JOIN
                    case "^": // SIDE JOIN
                    case "*": // CROSS JOIN
                        jc = j.getJoinConfig();
                        Boolean isMain = jc.isMain();
                        jc.setMain(false).setPrepared(isPrepared()).setPreparedValueList(new ArrayList<Object>());
                        js = jc.getWhereString(false);
                        jc.setMain(isMain);

                        if (StringUtil.isEmpty(js, true)) {
                            continue;
                        }

                        if (StringUtil.isEmpty(newWs, true) == false) {
                            newWs += SqlOper.AND;
                        }

                        if ("^".equals(j.getJoinType())) { // (A & ! B) | (B & ! A)
                            newWs += " (   ( " + ws + (StringUtil.isEmpty(ws, true) ? "" : SqlOper.AND + SqlOper.NOT) + " ( " + js + " ) ) "
                                    + SqlOper.OR
                                    + " ( " + js + SqlOper.AND + SqlOper.NOT + " ( " + ws + " )  )   ) ";

                            newPvl.addAll(pvl);
                            newPvl.addAll(jc.getPreparedValueList());
                            newPvl.addAll(jc.getPreparedValueList());
                            newPvl.addAll(pvl);
                        } else {
                            logic = Logic.getType(j.getJoinType());

                            newWs += " ( "
                                    + getCondition(
                                    Logic.isNot(logic),
                                    ws
                                            + (StringUtil.isEmpty(ws, true) ? "" : (Logic.isAnd(logic) ? SqlOper.AND : SqlOper.OR))
                                            + " ( " + js + " ) "
                            )
                                    + " ) ";

                            newPvl.addAll(pvl);
                            newPvl.addAll(jc.getPreparedValueList());
                        }

                        break;
                    default:
                        throw new UnsupportedOperationException("join:value 中 value 里的 " + j.getJoinType() + "/" + j.getPath() + "错误！不支持 " + j.getJoinType() + " 等 [@ APP, < LEFT, > RIGHT, | FULL, & INNER, ! OUTTER, ^ SIDE, * CROSS] 之外的JOIN类型 !");
                }
            }

            whereString = newWs;
            preparedValueList = newPvl;
        }

        String s = whereString.isEmpty() ? "" : (hasPrefix ? " WHERE " : "") + whereString;

        if (s.isEmpty() && RequestMethod.isQueryMethod(method) == false) {
            throw new UnsupportedOperationException("写操作请求必须带条件！！！");
        }

        return s;
    }

    /**
     * @param key
     * @param value
     * @param method
     * @param verifyName
     * @return
     * @throws Exception
     */
    private String getWhereItem(String key, Object value, RequestMethod method, Boolean verifyName) throws Exception {

        //避免筛选到全部	value = key == null ? null : where.get(key);
        //关键字||方法, +或-直接报错
        if (key == null || value == null || key.startsWith("@") || key.endsWith("()")) {
            return null;
        }
        if (key.endsWith("@")) {
            throw new IllegalArgumentException(TAG + ".getWhereItem: 字符 " + key + " 不合法！");
        }

        Integer keyType;
        if (key.endsWith("$")) {
            keyType = 1;
        } else if (key.endsWith("~") || key.endsWith("?")) {
            keyType = key.charAt(key.length() - 2) == '*' ? -2 : 2;
        } else if (key.endsWith("%")) {
            keyType = 3;
        } else if (key.endsWith("{}")) {
            keyType = 4;
        } else if (key.endsWith("<>")) {
            keyType = 5;
        } else { //else绝对不能省，避免再次踩坑！ keyType = 0; 写在for循环外面都没注意！
            keyType = 0;
        }
        key = getRealKey(method, key, false, true, verifyName, getQuote());

        switch (keyType) {
            case 1:
                return getSearchString(key, value);
            case -2:
            case 2:
                return getRegExpString(key, value, keyType < 0);
            case 3:
                return getBetweenString(key, value);
            case 4:
                return getRangeString(key, value);
            case 5:
                return getContainString(key, value);
            default: //TODO MySQL JSON类型的字段对比 key='[]' 会无结果！ key LIKE '[1, 2, 3]'  //TODO MySQL , 后面有空格！
                return getEqualString(key, value);
        }
    }


    @JSONField(serialize = false)
    public String getEqualString(String key, Object value) {
        if (value instanceof Collection<?>) {
            throw new IllegalArgumentException(key + ":value 中value不合法！非PUT请求只支持 [Boolean, Number, String] 内的类型 ！");
        }

        Boolean not = key.endsWith("!");
        if (not) {
            key = key.substring(0, key.length() - 1);
        }
        if (StringUtil.isName(key) == false) {
            throw new IllegalArgumentException(key + ":value 中key不合法！不支持 ! 以外的逻辑符 ！");
        }
        return getKey(key) + (not ? "!=" : "=") + getValue(value);
    }

    public String getKey(String key) {
        String q = getQuote();
        return (isKeyPrefix() ? getAlias() + "." : "") + q + key + q;
    }

    /**
     * 使用prepareStatement预编译，值为 ? ，后续动态set进去
     */
    private List<Object> preparedValueList = new ArrayList<>();

    private Object getValue(@NotNull Object value) {
        if (isPrepared()) {
            preparedValueList.add(value);
            return "?";
        }
        return "'" + value + "'";
    }

    @Override
    public List<Object> getPreparedValueList() {
        return preparedValueList;
    }

    @Override
    public AbstractSQLConfig setPreparedValueList(List<Object> preparedValueList) {
        this.preparedValueList = preparedValueList;
        return this;
    }


    /**
     * search key match value
     *
     * @param value
     * @return {@link #getSearchString(String, Object[], Integer)}
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getSearchString(String key, Object value) throws IllegalArgumentException {
        if (value == null) {
            return "";
        }

        Logic logic = new Logic(key);
        key = logic.getKey();

        JSONArray arr = newJSONArray(value);
        if (arr.isEmpty()) {
            return "";
        }
        return getSearchString(key, arr.toArray(), logic.getType());
    }

    /**
     * search key match values
     *
     * @param values
     * @return LOGIC [  key LIKE 'values[i]' ]
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getSearchString(String key, Object[] values, Integer type) throws IllegalArgumentException {
        if (values == null || values.length <= 0) {
            return "";
        }

        String condition = "";
        for (Integer i = 0; i < values.length; i++) {
            if (values[i] instanceof String == false) {
                throw new IllegalArgumentException(key + "$:value 中value的类型只能为String或String[]！");
            }
            condition += (i <= 0 ? "" : (Logic.isAnd(type) ? SqlOper.AND : SqlOper.OR)) + getLikeString(key, values[i]);
        }

        return getCondition(Logic.isNot(type), condition);
    }

    /**
     * WHERE key LIKE 'value'
     *
     * @param key
     * @param value
     * @return key LIKE 'value'
     */
    @JSONField(serialize = false)
    public String getLikeString(String key, Object value) {
        return getKey(key) + " LIKE " + getValue(value);
    }

    /**
     * search key match RegExp values
     *
     * @param key
     * @param value
     * @param ignoreCase
     * @return {@link #getRegExpString(String, Object[], Integer, Boolean)}
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getRegExpString(String key, Object value, Boolean ignoreCase) throws IllegalArgumentException {
        if (value == null) {
            return "";
        }

        Logic logic = new Logic(key);
        key = logic.getKey();

        JSONArray arr = newJSONArray(value);
        if (arr.isEmpty()) {
            return "";
        }
        return getRegExpString(key, arr.toArray(), logic.getType(), ignoreCase);
    }

    /**
     * search key match RegExp values
     *
     * @param key
     * @param values
     * @param type
     * @param ignoreCase
     * @return LOGIC [  key REGEXP 'values[i]' ]
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getRegExpString(String key, Object[] values, Integer type, Boolean ignoreCase) throws IllegalArgumentException {
        if (values == null || values.length <= 0) {
            return "";
        }

        String condition = "";
        for (Integer i = 0; i < values.length; i++) {
            if (values[i] instanceof String == false) {
                throw new IllegalArgumentException(key + "$:value 中value的类型只能为String或String[]！");
            }
            condition += (i <= 0 ? "" : (Logic.isAnd(type) ? SqlOper.AND : SqlOper.OR)) + getRegExpString(key, (String) values[i], ignoreCase);
        }

        return getCondition(Logic.isNot(type), condition);
    }

    /**
     * WHERE key REGEXP 'value'
     *
     * @param key
     * @param value
     * @param ignoreCase
     * @return key REGEXP 'value'
     */
    @JSONField(serialize = false)
    public String getRegExpString(String key, String value, Boolean ignoreCase) {
        return getKey(key) + " REGEXP " + (ignoreCase ? "" : "BINARY ") + getValue(value);
    }

    /**
     * WHERE key BETWEEN 'start' SqlOper.AND 'end'
     *
     * @param key
     * @param value 'start,end'
     * @return LOGIC [ key BETWEEN 'start' SqlOper.AND 'end' ]
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getBetweenString(String key, Object value) throws IllegalArgumentException {
        if (value == null) {
            return "";
        }

        Logic logic = new Logic(key);
        key = logic.getKey();

        JSONArray arr = newJSONArray(value);
        if (arr.isEmpty()) {
            return "";
        }
        return getBetweenString(key, arr.toArray(), logic.getType());
    }

    /**
     * WHERE key BETWEEN 'start' SqlOper.AND 'end'
     *
     * @param key
     * @param values 'start,end' TODO 在 '1,2' 和 ['1,2', '3,4'] 基础上新增支持 [1, 2] 和 [[1,2], [3,4]] ？
     * @return LOGIC [ key BETWEEN 'start' SqlOper.AND 'end' ]
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getBetweenString(String key, Object[] values, Integer type) throws IllegalArgumentException {
        if (values == null || values.length <= 0) {
            return "";
        }

        String condition = "";
        String[] vs;
        for (Integer i = 0; i < values.length; i++) {
            if (values[i] instanceof String == false) {
                throw new IllegalArgumentException(key + "%:value 中 value 的类型只能为 String 或 String[] ！");
            }

            vs = StringUtil.split((String) values[i]);
            if (vs == null || vs.length != 2) {
                throw new IllegalArgumentException(key + "%:value 中 value 不合法！类型为 String 时必须包括1个逗号 , 且左右两侧都有值！类型为 String[] 里面每个元素要符合前面类型为 String 的规则 ！");
            }

            condition += (i <= 0 ? "" : (Logic.isAnd(type) ? SqlOper.AND : SqlOper.OR)) + "(" + getBetweenString(key, vs[0], vs[1]) + ")";
        }

        return getCondition(Logic.isNot(type), condition);
    }

    /**
     * WHERE key BETWEEN 'start' SqlOper.AND 'end'
     *
     * @param key
     * @param start 'start,end' TODO 在 '1,2' 和 ['1,2', '3,4'] 基础上新增支持 [1, 2] 和 [[1,2], [3,4]] ？
     * @return key BETWEEN 'start' SqlOper.AND 'end'
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getBetweenString(String key, Object start, Object end) throws IllegalArgumentException {
        if (JsonApi.isBooleanOrNumberOrString(start) == false || JsonApi.isBooleanOrNumberOrString(end) == false) {
            throw new IllegalArgumentException(key + "%:value 中 value 不合法！类型为 String 时必须包括1个逗号 , 且左右两侧都有值！类型为 String[] 里面每个元素要符合前面类型为 String 的规则 ！");
        }
        return getKey(key) + " BETWEEN " + getValue(start) + SqlOper.AND + getValue(end);
    }


    //% between >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    //{} range <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    // * 和 / 不能同时出现，防止 /* */ 段注释！ # 和 -- 不能出现，防止行注释！ ; 不能出现，防止隔断SQL语句！空格不能出现，防止 CRUD,DROP,SHOW TABLES等语句！
    private static final Pattern PATTERN_RANGE;
    private static final Pattern PATTERN_HAVING;
    private static final Pattern PATTERN_HAVING_SUFFIX;

    static {
        PATTERN_RANGE = Pattern.compile("^[0-9%!=<>,]+$"); // ^[a-zA-Z0-9_*%!=<>(),"]+$ 导致 exists(select*from(Comment)) 通过！
        PATTERN_HAVING = Pattern.compile("^[A-Za-z0-9%!=<>]+$"); //TODO 改成更好的正则，校验前面为单词，中间为操作符，后面为值
        PATTERN_HAVING_SUFFIX = Pattern.compile("^[0-9%!=<>]+$"); // ^[a-zA-Z0-9_*%!=<>(),"]+$ 导致 exists(select*from(Comment)) 通过！
    }


    /**
     * WHERE key > 'key0' SqlOper.AND key <= 'key1' SqlOper.AND ...
     *
     * @param key
     * @param range "condition0,condition1..."
     * @return key condition0 SqlOper.AND key condition1 SqlOper.AND ...
     * @throws Exception
     */
    @JSONField(serialize = false)
    public String getRangeString(String key, Object range) throws Exception {
        if (range == null) {//依赖的对象都没有给出有效值，这个存在无意义。如果是客户端传的，那就能在客户端确定了。
            throw new NotExistException(TAG + "getRangeString(" + key + ", " + range
                    + ") range == null");
        }

        Logic logic = new Logic(key);
        key = logic.getKey();

        if (range instanceof List) {
            if (logic.isOr() || logic.isNot()) {
                return getKey(key) + getInString(key, ((List<?>) range).toArray(), logic.isNot());
            }
            throw new IllegalArgumentException(key + "{}\":[] 中key末尾的逻辑运算符只能用'|','!'中的一种 ！");
        }
        if (range instanceof String) {//非Number类型需要客户端拼接成 < 'value0', >= 'value1'这种
            if (isPrepared() && PATTERN_RANGE.matcher((String) range).matches() == false) {
                throw new UnsupportedOperationException("字符串 " + range + " 不合法！预编译模式下 key{}:\"condition\" 中 condition 必须符合正则表达式 ^[0-9%!=<>,]+$ ！不允许空格！");
            }

            String[] conditions = StringUtil.split((String) range);
            String condition = "";
            if (conditions != null) {
                Integer index;
                for (Integer i = 0; i < conditions.length; i++) {//对函数条件length(key)<=5这种不再在开头加key
                    index = conditions[i] == null ? -1 : conditions[i].indexOf("(");
                    condition += ((i <= 0 ? "" : (logic.isAnd() ? SqlOper.AND : SqlOper.OR))//连接方式
                            + (index >= 0 && index < conditions[i].indexOf(")") ? "" : getKey(key) + " ")//函数和非函数条件
                            + conditions[i]);//单个条件
                }
            }
            if (condition.isEmpty()) {
                return "";
            }

            return getCondition(logic.isNot(), condition);
        }

        throw new IllegalArgumentException(key + "{}:range 类型为" + range.getClass().getSimpleName()
                + "！range只能是 用','分隔条件的字符串 或者 可取选项JSONArray！");
    }

    /**
     * WHERE key IN ('key0', 'key1', ... )
     *
     * @param in
     * @return IN ('key0', 'key1', ... )
     * @throws NotExistException
     */
    @JSONField(serialize = false)
    public String getInString(String key, Object[] in, Boolean not) throws NotExistException {
        String condition = "";
        if (in != null) {//返回 "" 会导致 id:[] 空值时效果和没有筛选id一样！
            for (Integer i = 0; i < in.length; i++) {
                condition += ((i > 0 ? "," : "") + getValue(in[i]));
            }
        }
        if (condition.isEmpty()) {//条件如果存在必须执行，不能忽略。条件为空会导致出错，又很难保证条件不为空(@:条件)，所以还是这样好
            throw new NotExistException(TAG + ".getInString(" + key + ", [], " + not
                    + ") >> condition.isEmpty() >> IN()");
        }
        return (not ? SqlOper.NOT : "") + " IN (" + condition + ")";
    }
    //{} range >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    //<> contain <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /**
     * WHERE key contains value
     *
     * @param key
     * @param value
     * @return {@link #getContainString(String, Object[], Integer)}
     * @throws NotExistException
     */
    @JSONField(serialize = false)
    public String getContainString(String key, Object value) throws NotExistException {
        if (value == null) {
            return "";
        }

        Logic logic = new Logic(key);
        key = logic.getKey();

        return getContainString(key, newJSONArray(value).toArray(), logic.getType());
    }

    /**
     * WHERE key contains childs
     *
     * @param key
     * @param childs null ? "" : (empty ? no child : contains childs)
     * @param type   |, &, !
     * @return LOGIC [  ( key LIKE '[" + childs[i] + "]'  SqlOper.OR  key LIKE '[" + childs[i] + ", %'
     * SqlOper.OR  key LIKE '%, " + childs[i] + ", %'  SqlOper.OR  key LIKE '%, " + childs[i] + "]' )  ]
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getContainString(String key, Object[] childs, Integer type) throws IllegalArgumentException {
        Boolean not = Logic.isNot(type);
        String condition = "";
        if (childs != null) {
            for (Integer i = 0; i < childs.length; i++) {
                if (childs[i] != null) {
                    if (childs[i] instanceof JsonApi) {
                        throw new IllegalArgumentException(key + "<>:value 中value类型不能为JSON！");
                    }
                    if (childs[i] instanceof String) {
                        childs[i] = "\"" + childs[i] + "\"";
                    }
                    condition += (i <= 0 ? "" : (Logic.isAnd(type) ? SqlOper.AND : SqlOper.OR))
                            + "JSON_CONTAINS(" + getKey(key) + ", " + getValue(childs[i]) + ")";
                }
            }
            if (condition.isEmpty()) {
                condition = (SqlOper.isNull(key, true) + SqlOper.OR + getLikeString(key, "[]"));
            } else {
                condition = (SqlOper.isNull(key, false) + SqlOper.AND + "(" + condition + ")");
            }
        }
        if (condition.isEmpty()) {
            return "";
        }
        return getCondition(not, condition);
    }

    /**
     * 拼接条件
     *
     * @param not
     * @param condition
     * @return
     */
    private static String getCondition(Boolean not, String condition) {
        return not ? SqlOper.NOT + "(" + condition + ")" : condition;
    }


    /**
     * 转为JSONArray
     *
     * @param obj
     * @return
     */
    @NotNull
    public static JSONArray newJSONArray(Object obj) {
        JSONArray array = new JSONArray();
        if (obj != null) {
            if (obj instanceof Collection) {
                array.addAll((Collection<?>) obj);
            } else {
                array.add(obj);
            }
        }
        return array;
    }

    //WHERE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    //SET <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /**
     * 获取SET
     *
     * @return
     * @throws Exception
     */
    @JSONField(serialize = false)
    public String getSetString() throws Exception {
        return getSetString(getMethod(), getContent(), !isTest());
    }

    /**
     * 获取SET
     *
     * @param method
     * @param content
     * @return
     * @throws Exception
     */
    @JSONField(serialize = false)
    public String getSetString(RequestMethod method, Map<String, Object> content, Boolean verifyName) throws Exception {
        Set<String> set = content == null ? null : content.keySet();
        String setString = "";

        if (set != null && set.size() > 0) {
            String quote = getQuote();

            Boolean isFirst = true;
            Integer keyType = 0;// 0 - =; 1 - +, 2 - -
            Object value;

            for (String key : set) {
                //避免筛选到全部	value = key == null ? null : content.get(key);
                if (key == null || JsonApiObject.KEY_ID.equals(key)) {
                    continue;
                }

                if (key.endsWith("+")) {
                    keyType = 1;
                } else if (key.endsWith("-")) {
                    keyType = 2;
                }
                value = content.get(key);
                key = getRealKey(method, key, false, true, verifyName, quote);

                setString += (isFirst ? "" : ", ") + (key + "=" + (keyType == 1 ? getAddString(key, value) : (keyType == 2
                        ? getRemoveString(key, value) : getValue(value))));

                isFirst = false;
            }
        }

        if (setString.isEmpty()) {
            throw new IllegalArgumentException("PUT 请求必须在Table内设置要修改的 key:value ！");
        }
        return " SET " + setString;
    }

    /**
     * SET key = CONCAT (key, 'value')
     *
     * @param key
     * @param value
     * @return CONCAT (key, 'value')
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getAddString(String key, Object value) throws IllegalArgumentException {
        if (value instanceof Number) {
            return key + " + " + value;
        }
        if (value instanceof String) {
            return " CONCAT (" + key + ", " + getValue(value) + ") ";
        }
        throw new IllegalArgumentException(key + "+ 对应的值 " + value + " 不是Number,String,Array中的任何一种！");
    }

    /**
     * SET key = replace(key, 'value', '')
     *
     * @param key
     * @param value
     * @return REPLACE (key, 'value', '')
     * @throws IllegalArgumentException
     */
    @JSONField(serialize = false)
    public String getRemoveString(String key, Object value) throws IllegalArgumentException {
        if (value instanceof Number) {
            return key + " - " + value;
        }
        if (value instanceof String) {
            return SqlOper.replace(key, (String) getValue(value), "");
        }
        throw new IllegalArgumentException(key + "- 对应的值 " + value + " 不是Number,String,Array中的任何一种！");
    }
    //SET >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    /**
     * @return
     * @throws Exception
     */
    @JSONField(serialize = false)
    @Override
    public String getSQL(Boolean prepared) throws Exception {
        return getSQL(this.setPrepared(prepared));
    }

    public static String getSQL(AbstractSQLConfig config) throws Exception {
        String tablePath = config == null ? null : config.getTablePath();
        if (!StringUtil.isNotEmpty(tablePath, true)) {
            return null;
        }

        switch (config.getMethod()) {
            case POST:
                return "INSERT INTO " + tablePath + config.getColumnString() + " VALUES" + config.getValuesString();
            case PUT:
                return "UPDATE " + tablePath + config.getSetString() + config.getWhereString(true);
            case DELETE:
                return "DELETE FROM " + tablePath + config.getWhereString(true);
            default:
                config.setPreparedValueList(Lists.newArrayList());
                String column = config.getColumnString();
                return "SELECT " + column + " FROM " + getConditionString(column, tablePath, config);
        }
    }

    /**
     * 获取条件SQL字符串
     */
    private static String getConditionString(String column, String table, AbstractSQLConfig config) throws Exception {
        String where = config.getWhereString(true);

        String condition = table + config.getJoinString() + where + (
                !RequestMethod.isGetMethod(config.getMethod(), true) ?
                        "" : config.getGroupString() + config.getHavingString() + config.getOrderString()
        );

        return condition + config.getLimitString();
    }


    private Boolean keyPrefix;

    @Override
    public Boolean isKeyPrefix() {
        return keyPrefix;
    }

    @Override
    public AbstractSQLConfig setKeyPrefix(Boolean keyPrefix) {
        this.keyPrefix = keyPrefix;
        return this;
    }


    /**
     * "":  // FULL JOIN
     * "|": // FULL JOIN  不支持 <>, [] ，避免太多符号
     * "&": // INNER JOIN
     * "!": // OUTTER JOIN
     * "^": // SIDE JOIN
     *
     * @return
     * @throws Exception
     */
    public String getJoinString() throws Exception {
        String joinOns = "";

        if (joinList != null) {
            String quote = getQuote();

            String sql = null;
            SqlConfig jc;
            String jt;
            String tn;
            for (Join j : joinList) {
                if (j.isAppJoin()) {
                    continue;
                }

                //LEFT JOIN sys.apijson_user AS User ON User.id = Moment.userId， 都是用 = ，通过relateType处理缓存
                // <"INNER JOIN User ON User.id = Moment.userId", UserConfig>  TODO  AS 放 getSQLTable 内
                jc = j.getJoinConfig();
                jc.setPrepared(isPrepared());

                jt = jc.getTable();
                tn = j.getTargetName();
                if (DATABASE_POSTGRESQL.equalsIgnoreCase(getDatabase())) {
                    jt = jt.toLowerCase();
                    tn = tn.toLowerCase();
                }

                //TODO $ SELF JOIN
                switch (j.getJoinType()) {

                    case "<":
                    case ">":
                        jc.setMain(true).setKeyPrefix(false);
                        sql = (">".equals(j.getJoinType()) ? " RIGHT" : " LEFT") + " JOIN ( " + jc.getSQL(isPrepared()) + " ) AS "
                                + quote + jt + quote + " ON " + quote + jt + quote + "." + quote + j.getKey() + quote + " = "
                                + quote + tn + quote + "." + quote + j.getTargetKey() + quote;
                        jc.setMain(false).setKeyPrefix(true);

                        preparedValueList.addAll(jc.getPreparedValueList());
                        break;

                    case "":
                    case "|":
                    case "&":
                    case "!":
                    case "^":
                        //场景少且性能差，默认禁用	case "*": // CROSS JOIN
                        sql = ("*".equals(j.getJoinType()) ? " CROSS JOIN " : " INNER JOIN ") + jc.getTablePath()
                                + " ON " + quote + jt + quote + "." + quote + j.getKey() + quote + " = " + quote + tn + quote + "." + quote + j.getTargetKey() + quote;
                        break;
                    default:
                        throw new UnsupportedOperationException("join:value 中 value 里的 " + j.getJoinType() + "/" + j.getPath() + "错误！不支持 " + j.getJoinType() + " 等 [@ APP, < LEFT, > RIGHT, | FULL, & INNER, ! OUTTER, ^ SIDE, * CROSS] 之外的JOIN类型 !");
                }

                joinOns += "  \n  " + sql;
            }
        }

        return joinOns;
    }

    /**
     * 新建SQL配置
     *
     * @param table
     * @param request
     * @return
     * @throws Exception
     */
    public static AbstractSQLConfig newSQLConfig(RequestMethod method, String table, JSONObject request, List<Join> joinList, Callback callback) throws Exception {
        if (request == null) {
            throw new NullPointerException(TAG + ": newSQLConfig  request == null!");
        }
        AbstractSQLConfig config = callback.getSQLConfig(method, table);

        //放后面会导致主表是空对象时 joinList 未解析
        config = parseJoin(method, config, joinList, callback);

        if (request.isEmpty()) {
            return config;
        }

        Object idIn = request.get(JsonApiObject.KEY_ID_IN);

        if (method == RequestMethod.POST) {
            if (idIn != null) {
                if ((idIn instanceof List == false) || ((List<?>) idIn).isEmpty()) {
                    throw new IllegalArgumentException("POST请求，生成多条记录请用 id{}:[] ！ [] 类型为JSONArray且不能为空！");
                }
            } else if (request.get(JsonApiObject.KEY_ID) == null) {
                request.put(JsonApiObject.KEY_ID, callback.newId(method, table));
            }
        }

        //对id和id{}处理，这两个一定会作为条件
        Object id = request.get(JsonApiObject.KEY_ID);
        if (id != null) {
            if (id instanceof Number) {
                if (((Number) id).longValue() <= 0) {
                    throw new NotExistException(TAG + ": newSQLConfig " + table + ".id <= 0");
                }
            } else if (id instanceof String) {
                if (StringUtil.isEmpty(id, true)) {
                    throw new NotExistException(TAG + ": newSQLConfig StringUtil.isEmpty(" + table + ".id, true)");
                }
            } else {
                throw new IllegalArgumentException(JsonApiObject.KEY_ID + ":value 中 value 的类型只能是 Long 或 String ！");
            }

            if (idIn != null && idIn instanceof List) {
                if (idIn != null && ((List<?>) idIn).contains(id) == false) {//empty有效  BaseModel.isEmpty(idIn) == false) {
                    throw new NotExistException(TAG + ": newSQLConfig  idIn != null && ((JSONArray) idIn).contains(id) == false");
                }
            }
        }


        String role = request.getString(JsonApiObject.KEY_ROLE);
        String database = request.getString(JsonApiObject.KEY_DATABASE);
        String schema = request.getString(JsonApiObject.KEY_SCHEMA);
        String combine = request.getString(JsonApiObject.KEY_COMBINE);
        String column = request.getString(JsonApiObject.KEY_COLUMN);
        String group = request.getString(JsonApiObject.KEY_GROUP);
        String having = request.getString(JsonApiObject.KEY_HAVING);
        String order = request.getString(JsonApiObject.KEY_ORDER);

        //强制作为条件且放在最前面优化性能
        request.remove(JsonApiObject.KEY_ID);
        request.remove(JsonApiObject.KEY_ID_IN);
        //关键词
        request.remove(JsonApiObject.KEY_ROLE);
        request.remove(JsonApiObject.KEY_DATABASE);
        request.remove(JsonApiObject.KEY_SCHEMA);
        request.remove(JsonApiObject.KEY_COMBINE);
        request.remove(JsonApiObject.KEY_COLUMN);
        request.remove(JsonApiObject.KEY_GROUP);
        request.remove(JsonApiObject.KEY_HAVING);
        request.remove(JsonApiObject.KEY_ORDER);


        Map<String, Object> tableWhere = new LinkedHashMap<String, Object>();

        //已经remove了id和id{}，以及@key
        Set<String> set = request.keySet();
        if (method == RequestMethod.POST) {
            if (set != null && !set.isEmpty()) {
                List<Object> idList;
                if (id != null) {
                    if (idIn != null) {
                        throw new IllegalArgumentException("POST请求中 id 和 id{} 不能同时存在!");
                    }

                    idList = Lists.newArrayList(1);
                    idList.add(id);
                } else { //多条记录
                    idList = Lists.newArrayList((JSONArray) idIn);
                }

                //idIn不为空时，valuesString有多条，唯一的区别就是id
                String[] columns = set.toArray(new String[]{});

                Collection<Object> valueCollection = request.values();
                Object[] values = valueCollection == null ? null : valueCollection.toArray();

                if (values == null || values.length != columns.length) {
                    throw new Exception("服务器内部错误:" + TAG + " newSQLConfig  values == null || values.length != columns.length !");
                }
                column = JsonApiObject.KEY_ID + "," + StringUtil.getString(columns);
                final Integer size = columns.length + 1;

                List<List<Object>> valuess = new ArrayList<>(idList.size());
                List<Object> items; //(item0, item1, ...)
                for (Integer i = 0; i < idList.size(); i++) {
                    items = new ArrayList<>(size);
                    items.add(idList.get(i));
                    for (Integer j = 1; j < size; j++) {
                        items.add(values[j - 1]);
                    }
                    valuess.add(items);
                }
                config.setValues(valuess);
            }
        } else { //非POST操作
            final Boolean isWhere = method != RequestMethod.PUT;

            List<String> whereList = null;

            Map<String, List<String>> combineMap = new LinkedHashMap<>();
            List<String> andList = Lists.newArrayList();
            List<String> orList = Lists.newArrayList();
            List<String> notList = Lists.newArrayList();

            //强制作为条件且放在最前面优化性能
            if (id != null) {
                tableWhere.put(JsonApiObject.KEY_ID, id);
                andList.add(JsonApiObject.KEY_ID);
            }
            if (idIn != null) {
                tableWhere.put(JsonApiObject.KEY_ID_IN, idIn);
                andList.add(JsonApiObject.KEY_ID_IN);
            }

            String[] ws = StringUtil.split(combine);
            if (ws != null) {
                if (method == RequestMethod.DELETE || method == RequestMethod.GETS || method == RequestMethod.HEADS) {
                    throw new IllegalArgumentException("DELETE,GETS,HEADS 请求不允许传 @combine:\"conditons\" !");
                }
                whereList = new ArrayList<>();

                String w;
                //去除 &,|,! 前缀
                for (Integer i = 0; i < ws.length; i++) {
                    w = ws[i];
                    if (w != null) {
                        if (w.startsWith("&")) {
                            w = w.substring(1);
                            andList.add(w);
                        } else if (w.startsWith("|")) {
                            if (method == RequestMethod.PUT) {
                                throw new IllegalArgumentException(table + ":{} 里的 @combine:value 中的value里条件 " + ws[i] + " 不合法！"
                                        + "PUT请求的 @combine:\"key0,key1,...\" 不允许传 |key 或 !key !");
                            }
                            w = w.substring(1);
                            orList.add(w);
                        } else if (w.startsWith("!")) {
                            if (method == RequestMethod.PUT) {
                                throw new IllegalArgumentException(table + ":{} 里的 @combine:value 中的value里条件 " + ws[i] + " 不合法！"
                                        + "PUT请求的 @combine:\"key0,key1,...\" 不允许传 |key 或 !key !");
                            }
                            w = w.substring(1);
                            notList.add(w);
                        } else {
                            orList.add(w);
                        }

                        if (w.isEmpty()) {
                            throw new IllegalArgumentException(table + ":{} 里的 @combine:value 中的value里条件 " + ws[i] + " 不合法！不允许为空值！");
                        } else {
                            if (JsonApiObject.KEY_ID.equals(w) || JsonApiObject.KEY_ID_IN.equals(w) || JsonApiObject.KEY_USER_ID.equals(w) || JsonApiObject.KEY_USER_ID_IN.equals(w)) {
                                throw new UnsupportedOperationException(table + ":{} 里的 @combine:value 中的value里 " + ws[i] + " 不合法！"
                                        + "不允许传 [" + JsonApiObject.KEY_ID + ", " + JsonApiObject.KEY_ID_IN + ", " + JsonApiObject.KEY_USER_ID + ", " + JsonApiObject.KEY_USER_ID_IN + "] 其中任何一个！");
                            }
                        }

                        whereList.add(w);
                    }
                    if (request.containsKey(w) == false) {
                        throw new IllegalArgumentException(table + ":{} 里的 @combine:value 中的value里 " + ws[i] + " 对应的 " + w + " 不在它里面！");
                    }
                }

            }


            Map<String, Object> tableContent = Maps.newLinkedHashMap();
            Object value;
            for (String key : set) {
                value = request.get(key);

                if (value instanceof Map) {
                    throw new IllegalArgumentException("不允许 " + key + " 等任何key的value类型为 {JSONObject} !");
                }

                //解决AccessVerifier新增userId没有作为条件，而是作为内容，导致PUT，DELETE出错
                if (isWhere) {
                    tableWhere.put(key, value);
                    if (whereList == null || whereList.contains(key) == false) {
                        andList.add(key);
                    }
                } else if (whereList != null && whereList.contains(key)) {
                    tableWhere.put(key, value);
                } else {
                    tableContent.put(key, value);
                }
            }

            combineMap.put("&", andList);
            combineMap.put("|", orList);
            combineMap.put("!", notList);
            config.setCombine(combineMap);

            config.setContent(tableContent);
        }

        List<String> cs = new ArrayList<>();
        String[] fks = StringUtil.split(column, ";");
        if (fks != null) {
            String[] ks;
            for (String fk : fks) {
                if (fk.contains("(")) {
                    cs.add(fk);
                } else { //key0,key1...
                    ks = StringUtil.split(fk);
                    if (ks != null && ks.length > 0) {
                        cs.addAll(Arrays.asList(ks));
                    }
                }
            }
        }

        config.setColumn(cs);
        config.setWhere(tableWhere);

        config.setId(id);

        config.setRole(role);
        config.setDatabase(database);
        config.setSchema(schema);
        config.setGroup(group);
        config.setHaving(having);
        config.setOrder(order);

        //TODO 解析JOIN，包括 @column，@group 等要合并

        //后面还可能用到，要还原
        //id或id{}条件
        request.put(JsonApiObject.KEY_ID, id);
        request.put(JsonApiObject.KEY_ID_IN, idIn);
        //关键词
        request.put(JsonApiObject.KEY_ROLE, role);
        request.put(JsonApiObject.KEY_DATABASE, database);
        request.put(JsonApiObject.KEY_SCHEMA, schema);
        request.put(JsonApiObject.KEY_COMBINE, combine);
        request.put(JsonApiObject.KEY_COLUMN, column);
        request.put(JsonApiObject.KEY_GROUP, group);
        request.put(JsonApiObject.KEY_HAVING, having);
        request.put(JsonApiObject.KEY_ORDER, order);

        return config;
    }

    public static AbstractSQLConfig parseJoin(RequestMethod method, AbstractSQLConfig config, List<Join> joinList, Callback callback) throws Exception {
        Boolean isQuery = RequestMethod.isQueryMethod(method);
        config.setKeyPrefix(isQuery && config.isMain() == false);

        //TODO 解析出 SqlConfig 再合并 column, order, group 等
        if (joinList == null || joinList.isEmpty() || RequestMethod.isQueryMethod(method) == false) {
            return config;
        }


        String name;
        for (Join j : joinList) {
            name = j.getName();
            //JOIN子查询不能设置LIMIT，因为ON关系是在子查询后处理的，会导致结果会错误
            SqlConfig joinConfig = newSQLConfig(method, name, j.getTable(), null, callback);
            SqlConfig cacheConfig = newSQLConfig(method, name, j.getTable(), null, callback).setCount(1);

            if (j.isAppJoin() == false) {
                if (isQuery) {
                    config.setKeyPrefix(true);
                }

                joinConfig.setMain(false).setKeyPrefix(true);
            }

            //解决 query: 1/2 查数量时报错
            /* SELECT  count(*)  AS count  FROM sys.Moment AS Moment
               LEFT JOIN ( SELECT count(*)  AS count FROM sys.Comment ) AS Comment ON Comment.momentId = Moment.id LIMIT 1 OFFSET 0 */
            if (RequestMethod.isHeadMethod(method, true)) {
                joinConfig.setMethod(RequestMethod.GET);
                joinConfig.setColumn(Arrays.asList(j.getKey()));

                cacheConfig.setMethod(RequestMethod.GET);
                cacheConfig.setColumn(Arrays.asList(j.getKey()));
            }

            j.setJoinConfig(joinConfig);
            j.setCacheConfig(cacheConfig);
        }

        config.setJoinList(joinList);

        return config;
    }


    /**
     * 获取客户端实际需要的key
     * verifyName = true
     *
     * @param method
     * @param originKey
     * @param isTableKey
     * @param saveLogic  保留逻辑运算符 & | !
     * @return
     */
    public static String getRealKey(RequestMethod method, String originKey
            , Boolean isTableKey, Boolean saveLogic, String quote) throws Exception {
        return getRealKey(method, originKey, isTableKey, saveLogic, true, quote);
    }

    /**
     * 获取客户端实际需要的key
     *
     * @param method
     * @param originKey
     * @param isTableKey
     * @param saveLogic  保留逻辑运算符 & | !
     * @param verifyName 验证key名是否符合代码变量/常量名
     * @return
     */
    public static String getRealKey(RequestMethod method, String originKey
            , Boolean isTableKey, Boolean saveLogic, Boolean verifyName, String quote) throws Exception {
        log.info("【saveLogic】：{}，【originKey】：{}", saveLogic, originKey);

        if (originKey == null || originKey.startsWith(quote) || JsonApiObject.isArrayKey(originKey)) {

            return originKey;
        }

        String key = new String(originKey);
        if (key.endsWith("$")) {//搜索，查询时处理
            key = key.substring(0, key.length() - 1);
        } else if (key.endsWith("~") || key.endsWith("?")) {//匹配正则表达式，查询时处理  TODO ？可能以后会被废弃，全用 ~ 和 *~ 替代，更接近 PostgreSQL 语法
            key = key.substring(0, key.length() - 1);
            if (key.endsWith("*")) {//忽略大小写
                key = key.substring(0, key.length() - 1);
            }
        } else if (key.endsWith("%")) {//数字、文本、日期范围，BETWEEN SqlOper.AND
            key = key.substring(0, key.length() - 1);
        } else if (key.endsWith("{}")) {//被包含，或者说key对应值处于value的范围内。查询时处理
            key = key.substring(0, key.length() - 2);
        } else if (key.endsWith("<>")) {//包含，或者说value处于key对应值的范围内。查询时处理
            key = key.substring(0, key.length() - 2);
        } else if (key.endsWith("()")) {//方法，查询完后处理，先用一个Map<key,function>保存？
            key = key.substring(0, key.length() - 2);
        } else if (key.endsWith("@")) {//引用，引用对象查询完后处理。fillTarget中暂时不用处理，因为非GET请求都是由给定的id确定，不需要引用
            key = key.substring(0, key.length() - 1);
        } else if (key.endsWith("+")) {//延长，PUT查询时处理
            if (method == RequestMethod.PUT) {//不为PUT就抛异常
                key = key.substring(0, key.length() - 1);
            }
        } else if (key.endsWith("-")) {//缩减，PUT查询时处理
            if (method == RequestMethod.PUT) {//不为PUT就抛异常
                key = key.substring(0, key.length() - 1);
            }
        }

        //不用Logic优化代码，否则 key 可能变为 key| 导致 key=value 变成 key|=value 而出错
        String last = null;
        //逻辑运算符仅供GET,HEAD方法使用
        if (RequestMethod.isQueryMethod(method)) {
            last = key.isEmpty() ? "" : key.substring(key.length() - 1);
            if ("&".equals(last) || "|".equals(last) || "!".equals(last)) {
                key = key.substring(0, key.length() - 1);
            } else {
                //避免key + StringUtil.getString(last)错误延长
                last = null;
            }
        }

        //"User:toUser":User转换"toUser":User, User为查询同名Table得到的JSONObject。交给客户端处理更好
        //不允许在column key中使用Type:key形式
        if (isTableKey) {
            //table以左边为准
            key = Pair.parseEntry(key, true).getKey();
        } else {
            //column以右边为准
            key = Pair.parseEntry(key).getValue();
        }

        if (verifyName && StringUtil.isName(key.startsWith("@") ? key.substring(1) : key) == false) {
            throw new IllegalArgumentException(method + "请求，字符 " + originKey + " 不合法！"
                    + " key:value 中的key只能关键词 '@key' 或 'key[逻辑符][条件符]' 或 PUT请求下的 'key+' / 'key-' ！");
        }

        if (saveLogic && last != null) {
            key = key + last;
        }

        log.info("【key】：{}", key);
        return key;
    }


    public interface Callback {
        /**
         * 获取 SqlConfig 的实例
         *
         * @param method
         * @param table
         * @return
         */
        AbstractSQLConfig getSQLConfig(RequestMethod method, String table);

        /**
         * 为 post 请求新建 id， 只能是 Long 或 String
         *
         * @param method
         * @param table
         * @return
         */
        Object newId(RequestMethod method, String table);
    }

}
