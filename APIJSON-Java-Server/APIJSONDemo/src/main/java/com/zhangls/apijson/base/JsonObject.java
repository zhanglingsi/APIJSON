package com.zhangls.apijson.base;

import com.zhangls.apijson.annotation.MethodAccess;
import com.zhangls.apijson.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangls on 2019/1/4.
 * @author zhangls
 */
public class JsonObject extends com.alibaba.fastjson.JSONObject {
    private static final long serialVersionUID = 1L;

    private static final String TAG = "JSONObject";

    public JsonObject() {
        super(true);
    }

    public JsonObject(Object object) {
        this(toJSONString(object));
    }

    public JsonObject(String json) {
        this(parseObject(json));
    }

    public JsonObject(com.alibaba.fastjson.JSONObject object) {
        this();
        putsAll(object);
    }


    public static final String KEY_ARRAY = "[]";

    /**
     * 判断是否为Array的key
     *
     * @param key
     * @return
     */
    public static boolean isArrayKey(String key) {
        return key != null && key.endsWith(KEY_ARRAY);
    }

    /**
     * 判断是否为对应Table的key
     *
     * @param key
     * @return
     */
    public static boolean isTableKey(String key) {
        return StringUtil.isBigName(key);
    }


    public static String KEY_ID = "id";
    public static String KEY_ID_IN = KEY_ID + "{}";
    public static String KEY_USER_ID = "userId";
    public static String KEY_USER_ID_IN = KEY_USER_ID + "{}";

    /**
     * set "id":id in Table layer
     *
     * @param id
     * @return
     */
    public JsonObject setId(Long id) {
        return puts(KEY_ID, id);
    }

    /**
     * set "id{}":[] in Table layer
     *
     * @param list
     * @return
     */
    public JsonObject setIdIn(List<Object> list) {
        return puts(KEY_ID_IN, list);
    }

    /**
     * set "userId":userId in Table layer
     *
     * @param id
     * @return
     */
    public JsonObject setUserId(Long id) {
        return puts(KEY_USER_ID, id);
    }

    /**
     * set "userId{}":[] in Table layer
     *
     * @param list
     * @return
     */
    public JsonObject setUserIdIn(List<Object> list) {
        return puts(KEY_USER_ID_IN, list);
    }


    //@key关键字都放这个类 <<<<<<<<<<<<<<<<<<<<<<
    public static final String KEY_ROLE = "@role"; //角色，拥有对某些数据的某些操作的权限
    public static final String KEY_TRY = "@try"; //尝试，忽略异常
    public static final String KEY_DROP = "@drop"; //丢弃，不返回
    public static final String KEY_CORRECT = "@correct"; //字段校正

    public static final String KEY_DATABASE = "@database"; //数据库类型，默认为MySQL
    public static final String KEY_SCHEMA = "@schema"; //数据库，Table在非默认schema内时需要声明
    public static final String KEY_COLUMN = "@column"; //查询的Table字段或SQL函数
    public static final String KEY_COMBINE = "@combine"; //条件组合，每个条件key前面可以放&,|,!逻辑关系  "id!{},&sex,!name&$"
    public static final String KEY_GROUP = "@group"; //分组方式
    public static final String KEY_HAVING = "@having"; //聚合函数条件，一般和@group一起用
    public static final String KEY_ORDER = "@order"; //排序方式

    public static final List<String> TABLE_KEY_LIST;

    static {
        TABLE_KEY_LIST = new ArrayList<String>();
        TABLE_KEY_LIST.add(KEY_ROLE);
        TABLE_KEY_LIST.add(KEY_DATABASE);
        TABLE_KEY_LIST.add(KEY_SCHEMA);
        TABLE_KEY_LIST.add(KEY_COLUMN);
        TABLE_KEY_LIST.add(KEY_COMBINE);
        TABLE_KEY_LIST.add(KEY_GROUP);
        TABLE_KEY_LIST.add(KEY_HAVING);
        TABLE_KEY_LIST.add(KEY_ORDER);
    }

    //@key关键字都放这个类 >>>>>>>>>>>>>>>>>>>>>>


    /**
     * set role of request sender
     *
     * @param role
     * @return this
     */
    public JsonObject setRole(String role) {
        return puts(KEY_ROLE, role);
    }

    /**
     * set try, ignore exceptions
     *
     * @param tri
     * @return this
     */
    public JsonObject setTry(boolean tri) {
        return puts(KEY_TRY, tri);
    }

    /**
     * set drop, data dropped will not return
     *
     * @param drop
     * @return this
     */
    public JsonObject setDrop(boolean drop) {
        return puts(KEY_DROP, drop);
    }

    /**
     * set correct, correct keys to target ones
     *
     * @param correct Map{originKey, [posibleKeys]}， posibleKey之间用 , 隔开
     * @return this
     */
    public JsonObject setCorrect(Map<String, String> correct) {
        return puts(KEY_CORRECT, correct);
    }


    /**
     * set database where table was puts
     *
     * @param database
     * @return this
     */
    public JsonObject setDatabase(String database) {
        return puts(KEY_DATABASE, database);

    }

    /**
     * set schema where table was puts
     *
     * @param schema
     * @return this
     */
    public JsonObject setSchema(String schema) {
        return puts(KEY_SCHEMA, schema);
    }

    /**
     * set keys need to be returned
     *
     * @param keys key0, key1, key2 ...
     * @return {@link #setColumn(String)}
     */
    public JsonObject setColumn(String... keys) {
        return setColumn(StringUtil.getString(keys, true));
    }

    /**
     * set keys need to be returned
     *
     * @param keys "key0,key1,key2..."
     * @return
     */
    public JsonObject setColumn(String keys) {
        return puts(KEY_COLUMN, keys);
    }

    /**
     * set combination of keys for conditions
     *
     * @param keys key0,&key1,|key2,!kye3 ...
     * @return {@link #setColumn(String)}
     */
    public JsonObject setCombine(String... keys) {
        return setCombine(StringUtil.getString(keys, true));
    }

    /**
     * set combination of keys for conditions
     *
     * @param keys key0,&key1,|key2,!kye3 ...
     * @return
     */
    public JsonObject setCombine(String keys) {
        return puts(KEY_COMBINE, keys);
    }

    /**
     * set keys for group by
     *
     * @param keys key0, key1, key2 ...
     * @return {@link #setGroup(String)}
     */
    public JsonObject setGroup(String... keys) {
        return setGroup(StringUtil.getString(keys, true));
    }

    /**
     * set keys for group by
     *
     * @param keys "key0,key1,key2..."
     * @return
     */
    public JsonObject setGroup(String keys) {
        return puts(KEY_GROUP, keys);
    }

    /**
     * set keys for having
     *
     * @param keys count(key0) > 1, sum(key1) <= 5, function2(key2) ? value2 ...
     * @return {@link #setHaving(String)}
     */
    public JsonObject setHaving(String... keys) {
        return setHaving(StringUtil.getString(keys, true));
    }

    /**
     * set keys for having
     *
     * @param keys "key0,key1,key2..."
     * @return
     */
    public JsonObject setHaving(String keys) {
        return puts(KEY_HAVING, keys);
    }

    /**
     * set keys for order by
     *
     * @param keys key0, key1+, key2- ...
     * @return {@link #setOrder(String)}
     */
    public JsonObject setOrder(String... keys) {
        return setOrder(StringUtil.getString(keys, true));
    }

    /**
     * set keys for order by
     *
     * @param keys "key0,key1+,key2-..."
     * @return
     */
    public JsonObject setOrder(String keys) {
        return puts(KEY_ORDER, keys);
    }


    //JSONObject内关键词 key >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    //Request <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


    /**
     * @param key
     * @param keys path = keys[0] + "/" + keys[1] + "/" + keys[2] + ...
     * @return {@link #puts(String, Object)}
     */
    public JsonObject putsPath(String key, String... keys) {
        return puts(key + "@", StringUtil.getString(keys, "/"));
    }

    /**
     * @param key
     * @param isNull
     * @return {@link #puts(String, Object)}
     */
    public JsonObject putsNull(String key, boolean isNull) {
        return puts(key + "{}", SqlOper.isNull(isNull));
    }

    /**
     * trim = false
     *
     * @param key
     * @param isEmpty
     * @return {@link #putsEmpty(String, boolean, boolean)}
     */
    public JsonObject putsEmpty(String key, boolean isEmpty) {
        return putsEmpty(key, isEmpty, false);
    }

    /**
     * @param key
     * @param isEmpty
     * @return {@link #puts(String, Object)}
     */
    public JsonObject putsEmpty(String key, boolean isEmpty, boolean trim) {
        return puts(key + "{}", SqlOper.isEmpty(key, isEmpty, trim));
    }

    /**
     * @param key
     * @param compare <=0, >5 ...
     * @return {@link #puts(String, Object)}
     */
    public JsonObject putsLength(String key, String compare) {
        return puts(key + "{}", SqlOper.length(key) + compare);
    }

    /**
     * 设置搜索
     * type = SEARCH_TYPE_CONTAIN_FULL
     *
     * @param key
     * @param value
     * @return {@link #putsSearch(String, String, int)}
     */
    public JsonObject putsSearch(String key, String value) {
        return putsSearch(key, value, SqlOper.SEARCH_TYPE_CONTAIN_FULL);
    }

    /**
     * 设置搜索
     *
     * @param key
     * @param value
     * @param type
     * @return {@link #puts(String, Object)}
     */
    public JsonObject putsSearch(String key, String value, int type) {
        return puts(key + "$", SqlOper.search(value, type));
    }


    /**
     * puts key-value in object into this
     *
     * @param map
     * @return this
     */
    public JsonObject putsAll(Map<? extends String, ? extends Object> map) {
        putAll(map);
        return this;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        if (map != null && map.isEmpty() == false) {
            super.putAll(map);
        }
    }


    /**
     * put and return this
     *
     *
     * @return {@link #puts(String, Object)}
     */
    public JsonObject puts(Object value) {
        return puts(null, value);
    }

    /**
     * put and return this
     *
     * @param key
     * @param value
     * @return this
     * @see {@link #put(String, Object)}
     */
    public JsonObject puts(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * put and return value
     *
     * @return {@link #put(String, Object)}
     */
    public Object put(Object value) {
        return put(null, value);
    }

    /**
     * put and return value
     *
     * @param key   StringUtil.isEmpty(key, true) ? key = value.getClass().getSimpleName();
     * @param value
     * @return value
     */
    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            return null;
        }
        if (StringUtil.isEmpty(key, true)) {
            Class<?> clazz = value.getClass();
            if (clazz.getAnnotation(MethodAccess.class) == null) {
                throw new IllegalArgumentException("puts  StringUtil.isEmpty(key, true)" +
                        " clazz.getAnnotation(MethodAccess.class) == null" +
                        " \n key为空时仅支持 类型被@MethodAccess注解 的value !!!" +
                        " \n 如果一定要这么用，请对 " + clazz.getName() + " 注解！" +
                        " \n 如果是类似 key[]:{} 结构的请求，建议用 putsAll(...) ！");
            }
            key = value.getClass().getSimpleName();
        }
        return super.put(key, value);
    }


}
