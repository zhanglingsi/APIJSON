package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.ConflictException;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.model.Test;
import com.zhangls.apijson.base.service.Parser;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.SqlCreator;
import com.zhangls.apijson.base.service.SqlExecutor;
import com.zhangls.apijson.utils.StringUtil;

import javax.activation.UnsupportedDataTypeException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * 结构类
 * 增删改查: OPERATION(ADD,REPLACE,PUT,REMOVE)   OPERATION:{key0:value0, key1:value1 ...}
 * 对值校验: VERIFY:{key0:value0, key1:value1 ...}  (key{}:range,key$:"%m%"等)
 * 对值重复性校验: UNIQUE:"key0:, key1 ..."  (UNIQUE:"phone,email" 等)
 *
 * @author Lemon
 */
public class Structure {
    private static final String TAG = "Structure";

    private Structure() {
    }


    /**
     * 从request提取target指定的内容
     *
     * @param method
     * @param name
     * @param target
     * @param request
     * @param creator
     * @return
     * @throws Exception
     */
    public static JSONObject parseRequest(@NotNull final RequestMethod method, final String name
            , final JSONObject target, final JSONObject request, final SqlCreator creator) throws Exception {
        return parseRequest(method, name, target, request, Parser.MAX_UPDATE_COUNT, creator);
    }

    /**
     * 从request提取target指定的内容
     *
     * @param method
     * @param name
     * @param target
     * @param request
     * @param maxUpdateCount
     * @param creator
     * @return
     * @throws Exception
     */
    public static JSONObject parseRequest(@NotNull final RequestMethod method, final String name
            , final JSONObject target, final JSONObject request, final int maxUpdateCount, final SqlCreator creator) throws Exception {

        if (target == null || request == null) {

            return null;
        }

        return parse(name, target, request, creator, new OnParseCallback() {

            @Override
            public JSONObject onParseJSONObject(String key, JSONObject tobj, JSONObject robj) throws Exception {

                if (robj == null) {
                    if (tobj != null) {
                        throw new IllegalArgumentException(method.name() + "请求，请在 " + name + " 内传 " + key + ":{} ！");
                    }
                } else if (JsonApiObject.isTableKey(key)) {
                    if (method == RequestMethod.POST) {
                        if (robj.containsKey(JsonApiObject.KEY_ID)) {
                            throw new IllegalArgumentException("POST请求，" + name + "/" + key + " 不能传 " + JsonApiObject.KEY_ID + " ！");
                        }
                    } else {
                        if (!RequestMethod.isQueryMethod(method)) {
                            verifyId(method.name(), name, key, robj, JsonApiObject.KEY_ID, maxUpdateCount, true);
                            verifyId(method.name(), name, key, robj, JsonApiObject.KEY_USER_ID, maxUpdateCount, false);
                        }
                    }
                }

                return parseRequest(method, key, tobj, robj, maxUpdateCount, creator);
            }
        });

    }

    /**
     * @param method
     * @param name
     * @param key
     * @param robj
     * @param idKey
     * @param atLeastOne 至少有一个不为null
     */
    private static void verifyId(@NotNull String method, @NotNull String name, @NotNull String key
            , @NotNull JSONObject robj, @NotNull String idKey, final int maxUpdateCount, boolean atLeastOne) {
        //单个修改或删除
        Object id = robj.get(idKey); //如果必须传 id ，可在Request表中配置NECESSARY
        if (id != null && id instanceof Number == false && id instanceof String == false) {
            throw new IllegalArgumentException(method + "请求，" + name + "/" + key
                    + " 里面的 " + idKey + ":value 中value的类型只能是 Long 或 String ！");
        }


        //批量修改或删除
        String idInKey = idKey + "{}";

        JSONArray idIn = null;
        try {
            idIn = robj.getJSONArray(idInKey); //如果必须传 id{} ，可在Request表中配置NECESSARY
        } catch (Exception e) {
            throw new IllegalArgumentException(method + "请求，" + name + "/" + key
                    + " 里面的 " + idInKey + ":value 中value的类型只能是 [Long] ！");
        }
        if (idIn == null) {
            if (atLeastOne && id == null) {
                throw new IllegalArgumentException(method + "请求，" + name + "/" + key
                        + " 里面 " + idKey + " 和 " + idInKey + " 至少传其中一个！");
            }
        } else {
            if (idIn.size() > maxUpdateCount) { //不允许一次操作 maxUpdateCount 条以上记录
                throw new IllegalArgumentException(method + "请求，" + name + "/" + key
                        + " 里面的 " + idInKey + ":[] 中[]的长度不能超过 " + maxUpdateCount + " ！");
            }
            //解决 id{}: ["1' OR 1='1'))--"] 绕过id{}限制
            //new ArrayList<Long>(idIn) 不能检查类型，Java泛型擦除问题，居然能把 ["a"] 赋值进去还不报错
            for (int i = 0; i < idIn.size(); i++) {
                Object o = idIn.get(i);
                if (o != null && o instanceof Number == false && o instanceof String == false) {
                    throw new IllegalArgumentException(method + "请求，" + name + "/" + key
                            + " 里面的 " + idInKey + ":[] 中所有项的类型都只能是 Long 或 String ！");
                }
            }
        }
    }


    /**
     * 校验并将response转换为指定的内容和结构
     *
     * @param method
     * @param name
     * @param target
     * @param response
     * @param callback
     * @param creator
     * @return
     * @throws Exception
     */
    public static JSONObject parseResponse(@NotNull final RequestMethod method, final String name
            , final JSONObject target, final JSONObject response, SqlCreator creator, OnParseCallback callback) throws Exception {

        if (target == null || response == null) {

            return response;
        }

        //解析
        return parse(name, target, response, creator, callback != null ? callback : new OnParseCallback() {
        });
    }


    /**
     * 对request和response不同的解析用callback返回
     *
     * @param target
     * @param callback
     * @param creator
     * @return
     * @throws Exception
     */
    public static JSONObject parse(String name, JSONObject target, JSONObject real
            , SqlCreator creator, @NotNull OnParseCallback callback) throws Exception {
        if (target == null) {
            return null;
        }


        //获取配置<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        JSONObject type = target.getJSONObject(Operation.TYPE.name());
        JSONObject verify = target.getJSONObject(Operation.VERIFY.name());
        JSONObject add = target.getJSONObject(Operation.ADD.name());
        JSONObject update = target.getJSONObject(Operation.UPDATE.name());
        JSONObject put = target.getJSONObject(Operation.PUT.name());
        JSONObject replace = target.getJSONObject(Operation.REPLACE.name());

        String unique = StringUtil.getNoBlankString(target.getString(Operation.UNIQUE.name()));
        String remove = StringUtil.getNoBlankString(target.getString(Operation.REMOVE.name()));
        String necessary = StringUtil.getNoBlankString(target.getString(Operation.NECESSARY.name()));
        String disallow = StringUtil.getNoBlankString(target.getString(Operation.DISALLOW.name()));

        //不还原，传进来的target不应该是原来的
        target.remove(Operation.TYPE.name());
        target.remove(Operation.VERIFY.name());
        target.remove(Operation.ADD.name());
        target.remove(Operation.UPDATE.name());
        target.remove(Operation.PUT.name());
        target.remove(Operation.REPLACE.name());

        target.remove(Operation.UNIQUE.name());
        target.remove(Operation.REMOVE.name());
        target.remove(Operation.NECESSARY.name());
        target.remove(Operation.DISALLOW.name());
        //获取配置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        //移除字段<<<<<<<<<<<<<<<<<<<
        String[] removes = StringUtil.split(remove);
        if (removes != null && removes.length > 0) {
            for (String r : removes) {
                real.remove(r);
            }
        }
        //移除字段>>>>>>>>>>>>>>>>>>>

        //判断必要字段是否都有<<<<<<<<<<<<<<<<<<<
        String[] necessarys = StringUtil.split(necessary);
        List<String> necessaryList = necessarys == null ? new ArrayList<String>() : Arrays.asList(necessarys);
        for (String s : necessaryList) {
            if (real.get(s) == null) {//可能传null进来，这里还会通过 real.containsKey(s) == false) {
                throw new IllegalArgumentException(name
                        + " 里面不能缺少 " + s + " 等[" + necessary + "]内的任何字段！");
            }
        }
        //判断必要字段是否都有>>>>>>>>>>>>>>>>>>>


        Set<String> objKeySet = Sets.newHashSet();

        //解析内容<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        Set<Entry<String, Object>> set = new LinkedHashSet<>(target.entrySet());
        if (set.isEmpty() == false) {

            String key;
            Object tvalue;
            Object rvalue;
            for (Entry<String, Object> entry : set) {
                key = entry == null ? null : entry.getKey();
                if (key == null) {
                    continue;
                }
                tvalue = entry.getValue();
                rvalue = real.get(key);
                if (callback.onParse(key, tvalue, rvalue) == false) {
                    continue;
                }

                if (tvalue instanceof JSONObject) {
                    tvalue = callback.onParseJSONObject(key, (JSONObject) tvalue, (JSONObject) rvalue);

                    objKeySet.add(key);
                } else if (tvalue instanceof JSONArray) {
                    tvalue = callback.onParseJSONArray(key, (JSONArray) tvalue, (JSONArray) rvalue);
                } else {//其它Object
                    tvalue = callback.onParseObject(key, tvalue, rvalue);
                }

                if (tvalue != null) {
                    real.put(key, tvalue);
                }
            }

        }

        //解析内容>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        Set<String> rkset = real.keySet();

        //解析不允许的字段<<<<<<<<<<<<<<<<<<<
        List<String> disallowList = Lists.newArrayList();
        if ("!".equals(disallow)) {
            for (String key : rkset) {
                if (key != null && !key.startsWith("@")
                        && !necessaryList.contains(key) && !objKeySet.contains(key)) {
                    disallowList.add(key);
                }
            }
        } else {
            String[] disallows = StringUtil.split(disallow);
            if (disallows != null && disallows.length > 0) {
                disallowList.addAll(Arrays.asList(disallows));
            }
        }
        //解析不允许的字段>>>>>>>>>>>>>>>>>>>


        //判断不允许传的key<<<<<<<<<<<<<<<<<<<<<<<<<
        for (String rk : rkset) {
            if (disallowList.contains(rk)) {
                throw new IllegalArgumentException(name
                        + " 里面不允许传 " + rk + " 等" + StringUtil.getString(disallowList) + "内的任何字段！");
            }

            if (rk == null) { //无效的key
                real.remove(rk);
                continue;
            }

            //不在target内的 key:{}
            if (rk.startsWith("@") == false && objKeySet.contains(rk) == false && real.get(rk) instanceof JSONObject) {
                throw new UnsupportedOperationException(name + " 里面不允许传 " + rk + ":{} ！");
            }
        }
        //判断不允许传的key>>>>>>>>>>>>>>>>>>>>>>>>>


        //校验与修改Request<<<<<<<<<<<<<<<<<
        //在tableKeySet校验后操作，避免 导致put/add进去的Table 被当成原Request的内容
        real = operate(Operation.TYPE, type, real, creator);
        real = operate(Operation.VERIFY, verify, real, creator);
        real = operate(Operation.ADD, add, real, creator);
        real = operate(Operation.UPDATE, update, real, creator);
        real = operate(Operation.PUT, put, real, creator);
        real = operate(Operation.REPLACE, replace, real, creator);
        //校验与修改Request>>>>>>>>>>>>>>>>>

        //TODO放在operate前？考虑性能、operate修改后再验证的值是否和原来一样
        //校验重复<<<<<<<<<<<<<<<<<<<
        String[] uniques = StringUtil.split(unique);
        if (uniques != null && uniques.length > 0) {
            long exceptId = real.getLongValue(JsonApiObject.KEY_ID);
            for (String u : uniques) {
                verifyRepeat(name, u, real.get(u), exceptId, creator);
            }
        }
        //校验重复>>>>>>>>>>>>>>>>>>>


        return real;
    }


    /**
     * 执行操作
     *
     * @param opt
     * @param targetChild
     * @param real
     * @param creator
     * @return
     * @throws Exception
     */
    private static JSONObject operate(Operation opt, JSONObject targetChild, JSONObject real, SqlCreator creator) throws Exception {
        if (targetChild == null) {
            return real;
        }
        if (real == null) {
            throw new IllegalArgumentException("operate  real == null!!!");
        }


        Set<Entry<String, Object>> set = new LinkedHashSet<>(targetChild.entrySet());
        String tk;
        Object tv;

        for (Entry<String, Object> e : set) {
            tk = e == null ? null : e.getKey();
            if (tk == null) {
                continue;
            }
            tv = e.getValue();

            if (opt == Operation.TYPE) {
                type(tk, tv, real);
            } else if (opt == Operation.VERIFY) {
                verify(tk, tv, real, creator);
            } else if (opt == Operation.UPDATE) {
                real.put(tk, tv);
            } else if (opt == Operation.PUT) {
                real.put(tk, tv);
            } else {
                if (real.containsKey(tk)) {
                    if (opt == Operation.REPLACE) {
                        real.put(tk, tv);
                    }
                } else {
                    if (opt == Operation.ADD) {
                        real.put(tk, tv);
                    }
                }
            }
        }

        return real;
    }


    /**
     * 验证值类型
     *
     * @param tk
     * @param tv
     * @param real
     * @throws Exception
     */
    private static void type(@NotNull String tk, Object tv, @NotNull JSONObject real) throws Exception {
        if (tv == null) {
            return;
        }
        if (tv instanceof String == false) {
            throw new UnsupportedDataTypeException("服务器内部错误，" + tk + ":value 的value不合法！"
                    + "Request表校验规则中 TYPE:{ key:value } 中的value只能是String类型！");
        }
        String t = (String) tv;
        Object rv = real.get(tk);
        if (rv == null) {
            return;
        }

        switch (t) {
            case "Boolean":
                //Boolean.parseBoolean(real.getString(tk)); 只会判断null和true
                if (rv instanceof Boolean == false) {
                    throw new UnsupportedDataTypeException(tk + ":value 的value不合法！类型必须是 Boolean !");
                }
                break;
            case "Long":
                try {
                    Long.parseLong(real.getString(tk));
                } catch (Exception e) {
                    throw new UnsupportedDataTypeException(tk + ":value 的value不合法！类型必须是 Long !");
                }
                break;
            case "Double":
                try {
                    Double.parseDouble(rv.toString());
                } catch (Exception e) {
                    throw new UnsupportedDataTypeException(tk + ":value 的value不合法！类型必须是 Double !");
                }
                break;
            case "String":
                if (rv instanceof String == false) {
                    throw new UnsupportedDataTypeException(tk + ":value 的value不合法！类型必须是 String !");
                }
                break;
            case "Object":
                if (rv instanceof Map == false) {
                    throw new UnsupportedDataTypeException(tk + ":value 的value不合法！类型必须是 {Object} !");
                }
                break;
            case "Array":
                if (rv instanceof Collection == false) {
                    throw new UnsupportedDataTypeException(tk + ":value 的value不合法！类型必须是 [Array] !");
                }
                break;
            default:
                throw new UnsupportedDataTypeException("服务器内部错误，类型 " + t + " 不合法！Request表校验规则中"
                        + " TYPE:{ key:value } 中的value类型必须是 [Boolean, Long, Double, String, Object, Array] 中的一个!");
        }
    }


    /**
     * 验证值
     *
     * @param tk
     * @param tv
     * @param real
     * @param creator
     * @throws Exception
     */
    private static void verify(@NotNull String tk, @NotNull Object tv, @NotNull JSONObject real, SqlCreator creator) throws Exception {
        if (tv == null) {
            throw new IllegalArgumentException("operate  operate == VERIFY " + tk + ":" + tv + " ,  >> tv == null!!!");
        }

        String rk;
        Object rv;
        Logic logic;
        if (tk.endsWith("$")) {
            sqlVerify("$", real, tk, tv, creator);
        } else if (tk.endsWith("~") || tk.endsWith("?")) {
            logic = new Logic(tk.substring(0, tk.length() - 1));
            rk = logic.getKey();
            rv = real.get(rk);
            if (rv == null) {
                return;
            }

            JSONArray array = AbstractSQLConfig.newJSONArray(tv);

            boolean m;
            boolean isOr = false;
            Pattern reg;
            for (Object r : array) {
                if (r instanceof String == false) {
                    throw new UnsupportedDataTypeException(rk + ":" + rv + " 中value只支持 String 或 [String] 类型！");
                }
                reg = AbstractObjectParser.COMPILE_MAP.get(r);
                if (reg == null) {
                    reg = Pattern.compile((String) r);
                }
                m = reg.matcher("" + rv).matches();
                if (m) {
                    if (logic.isNot()) {
                        throw new IllegalArgumentException(rk + ":value 中value不合法！必须匹配 " + tk + ":" + tv + " !");
                    }
                    if (logic.isOr()) {
                        isOr = true;
                        break;
                    }
                } else {
                    if (logic.isAnd()) {
                        throw new IllegalArgumentException(rk + ":value 中value不合法！必须匹配 " + tk + ":" + tv + " !");
                    }
                }
            }

            if (isOr == false && logic.isOr()) {
                throw new IllegalArgumentException(rk + ":value 中value不合法！必须匹配 " + tk + ":" + tv + " !");
            }
        } else if (tk.endsWith("{}")) {
            if (tv instanceof String) {//TODO  >= 0, < 10
                sqlVerify("{}", real, tk, tv, creator);
            } else if (tv instanceof JSONArray) {
                logic = new Logic(tk.substring(0, tk.length() - 2));
                rk = logic.getKey();
                rv = real.get(rk);
                if (rv == null) {
                    return;
                }

                if (((JSONArray) tv).contains(rv) == logic.isNot()) {
                    throw new IllegalArgumentException(rk + ":value 中value不合法！必须匹配 " + tk + ":" + tv + " !");
                }
            } else {
                throw new UnsupportedDataTypeException("服务器Request表verify配置错误！");
            }
        } else if (tk.endsWith("<>")) {
            logic = new Logic(tk.substring(0, tk.length() - 2));
            rk = logic.getKey();
            rv = real.get(rk);
            if (rv == null) {
                return;
            }

            if (rv instanceof JSONArray == false) {
                throw new UnsupportedDataTypeException("服务器Request表verify配置错误！");
            }

            JSONArray array = AbstractSQLConfig.newJSONArray(tv);

            boolean isOr = false;
            for (Object o : array) {
                if (((JSONArray) rv).contains(o)) {
                    if (logic.isNot()) {
                        throw new IllegalArgumentException(rk + ":value 中value不合法！必须匹配 " + tk + ":" + tv + " !");
                    }
                    if (logic.isOr()) {
                        isOr = true;
                        break;
                    }
                } else {
                    if (logic.isAnd()) {
                        throw new IllegalArgumentException(rk + ":value 中value不合法！必须匹配 " + tk + ":" + tv + " !");
                    }
                }
            }

            if (isOr == false && logic.isOr()) {
                throw new IllegalArgumentException(rk + ":value 中value不合法！必须匹配 " + tk + ":" + tv + " !");
            }
        } else {
            throw new IllegalArgumentException("服务器Request表verify配置错误！");
        }
    }

    /**
     * 通过数据库执行SQL语句来验证条件
     *
     * @param funChar
     * @param real
     * @param tk
     * @param tv
     * @param creator
     * @throws Exception
     */
    private static void sqlVerify(@NotNull String funChar, @NotNull JSONObject real, @NotNull String tk, @NotNull Object tv
            , @NotNull SqlCreator creator) throws Exception {
        //不能用Parser, 0 这种不符合 StringUtil.isName !
        Logic logic = new Logic(tk.substring(0, tk.length() - funChar.length()));
        String rk = logic.getKey();
        Object rv = real.get(rk);
        if (rv == null) {
            return;
        }

        SqlConfig config = creator.createSQLConfig().setMethod(RequestMethod.HEAD).setCount(1).setPage(0);
        config.setTable(Test.class.getSimpleName());
        config.setTest(true);
        config.putWhere("'" + rv + "'" + logic.getChar() + funChar, tv, false);

        SqlExecutor executor = creator.createSQLExecutor();
        JSONObject result = null;
        try {
            result = executor.execute(config);
        } finally {
            executor.close();
        }
        if (result != null && JsonApiResponse.isExist(result.getIntValue(JsonApiResponse.KEY_COUNT)) == false) {
            throw new IllegalArgumentException(rk + ":" + rv + "中value不合法！必须匹配 " + logic.getChar() + tv + " ！");
        }
    }


    /**
     * 验证是否重复
     *
     * @param table
     * @param key
     * @param value
     * @throws Exception
     */
    public static void verifyRepeat(String table, String key, Object value, @NotNull SqlCreator creator) throws Exception {
        verifyRepeat(table, key, value, 0, creator);
    }

    /**
     * 验证是否重复
     *
     * @param table
     * @param key
     * @param value
     * @param exceptId 不包含id
     * @throws Exception
     */
    public static void verifyRepeat(String table, String key, Object value, long exceptId, @NotNull SqlCreator creator) throws Exception {
        if (key == null || value == null) {
            return;
        }
        if (value instanceof JsonApi) {
            throw new UnsupportedDataTypeException(key + ":value 中value的类型不能为JSON！");
        }


        SqlConfig config = creator.createSQLConfig().setMethod(RequestMethod.HEAD).setCount(1).setPage(0);
        config.setTable(table);
        if (exceptId > 0) {//允许修改自己的属性为该属性原来的值
            config.putWhere(JsonApiRequest.KEY_ID + "!", exceptId, false);
        }
        config.putWhere(key, value, false);

        SqlExecutor executor = creator.createSQLExecutor();
        try {
            JSONObject result = executor.execute(config);
            if (result == null) {
                throw new Exception("服务器内部错误  verifyRepeat  result == null");
            }
            if (result.getIntValue(JsonApiResponse.KEY_COUNT) > 0) {
                throw new ConflictException(key + ": " + value + " 已经存在，不能重复！");
            }
        } finally {
            executor.close();
        }
    }


}
