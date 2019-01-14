package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApiObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.exception.ConflictException;
import com.zhangls.apijson.base.exception.NotExistException;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.service.ObjectParser;
import com.zhangls.apijson.base.service.Parser;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;


import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;



/**
 * 简化Parser，getObject和getArray(getArrayConfig)都能用
 *
 * @author Lemon
 */
@Slf4j
public abstract class AbstractObjectParser implements ObjectParser {
    private static final String TAG = "ObjectParser";

    @NotNull
    protected Parser<?> parser;

    public AbstractObjectParser setParser(Parser<?> parser) {
        this.parser = parser;
        return this;
    }


    protected JSONObject request;
    protected String parentPath;
    protected SqlConfig arrayConfig;

    protected final Integer type;
    protected final List<Join> joinList;
    protected final Boolean isTable;
    protected final String path;
    protected final String table;


    protected final Boolean tri;
    protected final Boolean drop;
    protected JSONObject correct;


    public AbstractObjectParser(@NotNull JSONObject request, String parentPath, String name, SqlConfig arrayConfig) throws Exception {
        if (request == null) {
            throw new IllegalArgumentException(TAG + ".ObjectParser  request == null!!!");
        }
        this.request = request;
        this.parentPath = parentPath;
        this.arrayConfig = arrayConfig;

        this.type = arrayConfig == null ? 0 : arrayConfig.getType();
        this.joinList = arrayConfig == null ? null : arrayConfig.getJoinList();
        this.path = ParserHelper.getAbsPath(parentPath, name);
        this.table = Pair.parseEntry(name, true).getKey();
        this.isTable = JsonApiObject.isTableKey(table);

        Boolean isEmpty = request.isEmpty();
        if (isEmpty) {
            this.tri = false;
            this.drop = false;
        } else {
            this.tri = request.getBooleanValue(JsonApiObject.KEY_TRY);
            this.drop = request.getBooleanValue(JsonApiObject.KEY_DROP);
            this.correct = request.getJSONObject(JsonApiObject.KEY_CORRECT);

            request.remove(JsonApiObject.KEY_TRY);
            request.remove(JsonApiObject.KEY_DROP);
            request.remove(JsonApiObject.KEY_CORRECT);

            try {
                parseCorrect();
            } catch (Exception e) {
                if (!tri) {
                    throw e;
                }
                invalidate();
            }
        }

        log.info("【Table表名】：{} ,【isTable】：{},【isEmpty】：{} ,【tri】：{} ,【drop】：{}", table, isTable, isEmpty, tri, drop);
    }

    public static final Map<String, Pattern> COMPILE_MAP;

    static {
        COMPILE_MAP = Maps.newHashMap();
        //手机
        COMPILE_MAP.put("phone", StringUtil.PATTERN_PHONE);
        //邮箱
        COMPILE_MAP.put("email", StringUtil.PATTERN_EMAIL);
        //身份证号
        COMPILE_MAP.put("idCard", StringUtil.PATTERN_ID_CARD);
    }

    protected Map<String, String> corrected;

    /**
     * 解析 @correct 校正
     *
     * @throws Exception
     */
    @Override
    public AbstractObjectParser parseCorrect() throws Exception {
        Set<String> set = correct == null ? null : new HashSet<>(correct.keySet());

        if (set != null && set.isEmpty() == false) {
            corrected = new HashMap<>();

            String value;
            String v;
            String[] posibleKeys;

            for (String k : set) {
                v = k == null ? null : correct.getString(k);
                value = v == null ? null : request.getString(k);
                posibleKeys = value == null ? null : StringUtil.split(v);

                if (posibleKeys != null && posibleKeys.length > 0) {
                    String rk = null;
                    Pattern p;
                    for (String pk : posibleKeys) {
                        p = pk == null ? null : COMPILE_MAP.get(pk);
                        if (p != null && p.matcher(value).matches()) {
                            rk = pk;
                            break;
                        }
                    }

                    if (rk == null) {
                        throw new IllegalArgumentException(
                                "格式错误！找不到 " + k + ":" + value + " 对应[" + v + "]内的任何一项！");
                    }
                    request.put(rk, request.remove(k));
                    corrected.put(k, rk);
                }
            }
        }

        return this;
    }


    private Boolean invalidate = false;

    public void invalidate() {
        invalidate = true;
    }

    public Boolean isInvalidate() {
        return invalidate;
    }

    private Boolean breakParse = false;

    public void breakParse() {
        breakParse = true;
    }

    public Boolean isBreakParse() {
        return breakParse || isInvalidate();
    }


    protected JSONObject response;
    protected JSONObject sqlRequest;
    protected JSONObject sqlReponse;
    /**
     * 自定义关键词
     */
    protected Map<String, Object> customMap;
    /**
     * 远程函数
     * {"-":{ "key-()":value }, "0":{ "key()":value }, "+":{ "key+()":value } }
     * - : 在executeSQL前解析
     * 0 : 在executeSQL后、onChildParse前解析
     * + : 在onChildParse后解析
     */
    protected Map<String, Map<String, String>> functionMap;
    /**
     * 子对象
     */
    protected Map<String, JSONObject> childMap;

    /**
     * 解析成员
     * response重新赋值
     *
     * @return null or this
     * @throws Exception
     */
    @Override
    public AbstractObjectParser parse() throws Exception {
        if (isInvalidate() == false) {
            breakParse = false;

            response = new JSONObject(true);

            sqlRequest = new JSONObject(true);
            sqlReponse = null;
            customMap = null;
            functionMap = null;
            childMap = null;

            Set<Entry<String, Object>> set = Sets.newLinkedHashSet(request.entrySet());

            //判断换取少几个变量的初始化是否值得？
            if (set != null && set.isEmpty() == false) {
                //非Table下必须保证原有顺序！否则 count,page 会丢, total@:"/[]/total" 会在[]:{}前执行！
                if (isTable) {
                    customMap = Maps.newLinkedHashMap();
                    childMap = Maps.newLinkedHashMap();
                }
                functionMap = Maps.newLinkedHashMap();

                List<String> whereList = null;
                //这里只有PUTArray需要处理  || method == DELETE) {
                if (method == RequestMethod.PUT) {
                    String[] combine = StringUtil.split(request.getString(JsonApiObject.KEY_COMBINE));
                    if (combine != null) {
                        String w;
                        //去除 &,|,! 前缀
                        for (Integer i = 0; i < combine.length; i++) {
                            w = combine[i];
                            if (w != null && (w.startsWith("&") || w.startsWith("|") || w.startsWith("!"))) {
                                combine[i] = w.substring(1);
                            }
                        }
                    }
                    //Arrays.asList()返回值不支持add方法！
                    whereList = Lists.newArrayList(Arrays.asList(combine != null ? combine : new String[]{}));

                    whereList.add(JsonApiRequest.KEY_ID);
                    whereList.add(JsonApiRequest.KEY_ID_IN);
                }

                String key;
                Object value;
                Integer index = 0;
                for (Entry<String, Object> entry : set) {
                    if (isBreakParse()) {
                        break;
                    }

                    value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    key = entry.getKey();

                    try {
                        //JSONObject，往下一级提取
                        if (value instanceof JSONObject && key.startsWith("@") == false) {
                            //添加到childMap，最后再解析
                            if (childMap != null) {
                                childMap.put(key, (JSONObject) value);
                                //直接解析并替换原来的，[]:{} 内必须直接解析，否则会因为丢掉count等属性，并且total@:"/[]/total"必须在[]:{} 后！
                            } else {
                                response.put(key, onChildParse(index, key, (JSONObject) value));
                                index++;
                            }
                        } else if (method == RequestMethod.PUT && value instanceof JSONArray
                                && (whereList == null || whereList.contains(key) == false)) {
                            onPUTArrayParse(key, (JSONArray) value);
                        } else {//JSONArray或其它Object，直接填充
                            if (onParse(key, value) == false) {
                                invalidate();
                            }
                        }
                    } catch (Exception e) {
                        if (!tri) {
                            //不忽略错误，抛异常
                            throw e;
                        }
                        //忽略错误，还原request
                        invalidate();
                    }
                }

                //非Table内的函数会被滞后在onChildParse后调用！ onFunctionResponse("-");
            }
        }

        if (isInvalidate()) {
            recycle();
            return null;
        }

        return this;
    }


    /**
     * 解析普通成员
     *
     * @param key
     * @param value
     * @return whether parse succeed
     */
    @Override
    public Boolean onParse(@NotNull String key, @NotNull Object value) throws Exception {
        //StringUtil.isPath((String) value)) {
        if (key.endsWith("@")) {
            if (value instanceof String == false) {
                throw new IllegalArgumentException("\"key@\": 后面必须为依赖路径String！");
            }
            //key{}@ getRealKey
            String replaceKey = key.substring(0, key.length() - 1);
            String targetPath = ParserHelper.getValuePath(type.equals(SqlConfig.TYPE_ITEM)
                    ? path : parentPath, new String((String) value));

            //先尝试获取，尽量保留缺省依赖路径，这样就不需要担心路径改变
            Object target = onReferenceParse(targetPath);

            //String#equals(null)会出错
            if (target == null) {

                return true;
            }
            //target可能是从requestObject里取出的 {}
            if (target instanceof Map) {
                return false;
            }
            if (targetPath.equals(target)) {

                //非查询关键词 @key 不影响查询，直接跳过
                if (isTable && (key.startsWith("@") == false || JsonApiRequest.TABLE_KEY_LIST.contains(key))) {
                    return false;
                } else {
                    return true;
                }
            }


            //直接替换原来的key@:path为key:target
            key = replaceKey;
            value = target;
        }

        if (key.endsWith("()")) {
            if (value instanceof String == false) {
                throw new IllegalArgumentException(path + "/" + key + ":function() 后面必须为函数String！");
            }

            String k = key.substring(0, key.length() - 2);
            //远程函数比较少用，一般一个Table:{}内用到也就一两个，所以这里用 "-","0","+" 更直观，转用 -1,0,1 对性能提升不大。
            String type;
            //不能封装到functionMap后批量执行，否则会导致非Table内的 key-():function() 在onChildParse后执行！
            if (k.endsWith("-")) {
                type = "-";
                k = k.substring(0, k.length() - 1);

                parseFunction(request, k, (String) value);
            } else {
                if (k.endsWith("+")) {
                    type = "+";
                    k = k.substring(0, k.length() - 1);
                } else {
                    type = "0";
                }

                //远程函数比较少用，一般一个Table:{}内用到也就一两个，所以这里循环里new出来对性能影响不大。
                Map<String, String> map = functionMap.get(type);
                if (map == null) {
                    map = new LinkedHashMap<>();
                }
                map.put(k, (String) value);

                functionMap.put(type, map);
            }
        } else if (isTable && key.startsWith("@") && JsonApiRequest.TABLE_KEY_LIST.contains(key) == false) {
            customMap.put(key, value);
        } else {
            sqlRequest.put(key, value);
        }

        return true;
    }


    /**
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    @Override
    public JSON onChildParse(Integer index, String key, JSONObject value) throws Exception {
        Boolean isFirst = index <= 0;
        Boolean isMain = isFirst && type.equals(SqlConfig.TYPE_ITEM);

        JSON child;
        Boolean isEmpty;

        if (JsonApiObject.isArrayKey(key)) {
            if (isMain) {
                throw new IllegalArgumentException(parentPath + "/" + key + ":{} 不合法！"
                        + "数组 []:{} 中第一个 key:{} 必须是主表 TableKey:{} ！不能为 arrayKey[]:{} ！");
            }

            child = parser.onArrayParse(value, path, key);
            isEmpty = child == null || ((JSONArray) child).isEmpty();
        } else {
            if (type.equals(SqlConfig.TYPE_ITEM) && JsonApiRequest.isTableKey(Pair.parseEntry(key, true).getKey()) == false) {
                throw new IllegalArgumentException(parentPath + "/" + key + ":{} 不合法！"
                        + "数组 []:{} 中每个 key:{} 都必须是表 TableKey:{} 或 数组 arrayKey[]:{} ！");
            }

            child = parser.onObjectParse(value, path, key, isMain ? arrayConfig.setType(SqlConfig.TYPE_ITEM_CHILD_0) : null);

            isEmpty = child == null || ((JSONObject) child).isEmpty();
            if (isFirst && isEmpty) {
                invalidate();
            }
        }
        //只添加! isChildEmpty的值，可能数据库返回数据不够count
        return isEmpty ? null : child;
    }


    //TODO 改用 MySQL json_add,json_remove,json_contains 等函数！

    /**
     * PUT key:[]
     *
     * @param key
     * @param array
     * @throws Exception
     */
    @Override
    public void onPUTArrayParse(@NotNull String key, @NotNull JSONArray array) throws Exception {
        if (isTable == false || array.isEmpty()) {
            return;
        }

        Integer putType = 0;
        if (key.endsWith("+")) {
            putType = 1;
        } else if (key.endsWith("-")) {
            putType = 2;
        } else {
            //replace
            //			throw new IllegalAccessException("PUT " + path + ", PUT Array不允许 " + key +
            //					" 这种没有 + 或 - 结尾的key！不允许整个替换掉原来的Array！");
        }
        String realKey = AbstractSQLConfig.getRealKey(method, key, false, false, "`");

        //GET > add all 或 remove all > PUT > remove key

        //GET <<<<<<<<<<<<<<<<<<<<<<<<<
        JSONObject rq = new JSONObject();
        rq.put(JsonApiRequest.KEY_ID, request.get(JsonApiRequest.KEY_ID));
        rq.put(JsonApiRequest.KEY_COLUMN, realKey);
        JSONObject rp = parseResponse(new JsonApiRequest(table, rq));
        //GET >>>>>>>>>>>>>>>>>>>>>>>>>


        //add all 或 remove all <<<<<<<<<<<<<<<<<<<<<<<<<
        if (rp != null) {
            rp = rp.getJSONObject(table);
        }
        JSONArray targetArray = rp == null ? null : rp.getJSONArray(realKey);
        if (targetArray == null) {
            targetArray = new JSONArray();
        }
        for (Object obj : array) {
            if (obj == null) {
                continue;
            }
            if (putType == 1) {
                if (targetArray.contains(obj)) {
                    throw new ConflictException("PUT " + path + ", " + realKey + ":" + obj + " 已存在！");
                }
                targetArray.add(obj);
            } else if (putType == 2) {
                if (targetArray.contains(obj) == false) {
                    throw new NullPointerException("PUT " + path + ", " + realKey + ":" + obj + " 不存在！");
                }
                targetArray.remove(obj);
            }
        }

        //add all 或 remove all >>>>>>>>>>>>>>>>>>>>>>>>>

        //PUT <<<<<<<<<<<<<<<<<<<<<<<<<
        sqlRequest.put(realKey, targetArray);
        //PUT >>>>>>>>>>>>>>>>>>>>>>>>>

    }


    /**
     * SQL查询，for single object
     *
     * @return {@link #executeSQL(Integer, Integer, Integer)}
     * @throws Exception
     */
    @Override
    public AbstractObjectParser executeSQL() throws Exception {
        return executeSQL(1, 0, 0);
    }

    protected SqlConfig sqlConfig = null;

    /**
     * SQL查询，for array item
     *
     * @param count
     * @param page
     * @param position
     * @return this
     * @throws Exception
     */
    @Override
    public AbstractObjectParser executeSQL(Integer count, Integer page, Integer position) throws Exception {
        //执行SQL操作数据库
        if (!isTable) {
            sqlReponse = new JSONObject(sqlRequest);
        } else {
            try {
                if (sqlConfig == null) {
                    sqlConfig = newSQLConfig();
                }
                sqlConfig.setCount(count).setPage(page).setPosition(position);
                sqlReponse = onSQLExecute();
            } catch (Exception e) {
                if (e instanceof NotExistException) {

                    sqlReponse = null;
                } else {
                    throw e;
                }
            }

            if (drop) {
                sqlReponse = null;
            }
        }

        return this;
    }

    /**
     * @return response
     * @throws Exception
     */
    @Override
    public JSONObject response() throws Exception {
        if (sqlReponse == null || sqlReponse.isEmpty()) {
            if (isTable) {
                return response;
            }
        } else {
            response.putAll(sqlReponse);
        }


        //把已校正的字段键值对corrected<originKey, correctedKey>添加进来，还是correct直接改？
        if (corrected != null) {
            response.put(JsonApiObject.KEY_CORRECT, corrected);
        }

        //把isTable时取出去的custom重新添加回来
        if (customMap != null) {
            response.putAll(customMap);
        }


        onFunctionResponse("0");

        onChildResponse();

        onFunctionResponse("+");

        onComplete();

        return response;
    }


    @Override
    public void onFunctionResponse(String type) throws Exception {
        Map<String, String> map = functionMap == null ? null : functionMap.get(type);

        //解析函数function
        Set<Entry<String, String>> functionSet = map == null ? null : map.entrySet();
        if (functionSet != null && functionSet.isEmpty() == false) {
//			JSONObject json = "-".equals(type) ? request : response; // key-():function 是实时执行，而不是在这里批量执行

            for (Entry<String, String> entry : functionSet) {

//				parseFunction(json, entry.getKey(), entry.getValue());
                parseFunction(response, entry.getKey(), entry.getValue());
            }
        }
    }

    public void parseFunction(JSONObject json, String key, String value) throws Exception {
        Object result = parser.onFunctionParse(json, value);

        if (result != null) {
            String k = AbstractSQLConfig.getRealKey(method, key, false, false, "`"); //FIXME PG 是 "

            response.put(k, result);
            parser.putQueryResult(ParserHelper.getAbsPath(path, k), result);
        }
    }

    @Override
    public void onChildResponse() throws Exception {
        //把isTable时取出去child解析后重新添加回来
        Set<Entry<String, JSONObject>> set = childMap == null ? null : childMap.entrySet();
        if (set != null) {
            Integer index = 0;
            for (Entry<String, JSONObject> entry : set) {
                if (entry != null) {
                    response.put(entry.getKey(), onChildParse(index, entry.getKey(), entry.getValue()));
                    index++;
                }
            }
        }
    }


    @Override
    public Object onReferenceParse(@NotNull String path) {
        return parser.getValueByPath(path);
    }

    @Override
    public JSONObject onSQLExecute() throws Exception {
        JSONObject result = parser.executeSQL(sqlConfig);
        if (result != null) {
            //解决获取关联数据时requestObject里不存在需要的关联数据
            parser.putQueryResult(path, result);
        }
        return result;
    }


    /**
     * response has the final value after parse (and query if isTable)
     */
    @Override
    public void onComplete() {
    }


    /**
     * 回收内存
     */
    @Override
    public void recycle() {
        //后面还可能用到，要还原,避免返回未传的字段
        if (tri) {
            request.put(JsonApiObject.KEY_TRY, tri);
        }
        if (drop) {
            request.put(JsonApiObject.KEY_DROP, drop);
        }
        if (correct != null) {
            request.put(JsonApiObject.KEY_CORRECT, correct);
        }


        correct = null;
        corrected = null;
        method = null;
        parentPath = null;
        arrayConfig = null;

        //		if (response != null) {
        //			response.clear();//有效果?
        //			response = null;
        //		}

        request = null;
        response = null;
        sqlRequest = null;
        sqlReponse = null;

        functionMap = null;
        customMap = null;
        childMap = null;
    }


    protected RequestMethod method;

    @Override
    public AbstractObjectParser setMethod(RequestMethod method) {
        if (this.method != method) {
            this.method = method;
            sqlConfig = null;
            //TODO ?			sqlReponse = null;
        }
        return this;
    }

    @Override
    public RequestMethod getMethod() {
        return method;
    }


    @Override
    public Boolean isTable() {
        return isTable;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public SqlConfig getArrayConfig() {
        return arrayConfig;
    }


    @Override
    public SqlConfig getSQLConfig() {
        return sqlConfig;
    }

    @Override
    public JSONObject getResponse() {
        return response;
    }

    @Override
    public JSONObject getSqlRequest() {
        return sqlRequest;
    }

    @Override
    public JSONObject getSqlReponse() {
        return sqlReponse;
    }

    @Override
    public Map<String, Object> getCustomMap() {
        return customMap;
    }

    @Override
    public Map<String, Map<String, String>> getFunctionMap() {
        return functionMap;
    }

    @Override
    public Map<String, JSONObject> getChildMap() {
        return childMap;
    }


}
