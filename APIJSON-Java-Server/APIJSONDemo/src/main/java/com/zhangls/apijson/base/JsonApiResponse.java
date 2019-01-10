package com.zhangls.apijson.base;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangls on 2019/1/4.
 * @author zhangls
 */
@Slf4j
public class JsonApiResponse extends JsonApiObject {

    private static final long serialVersionUID = 1L;

    private static final String TAG = "JsonApiResponse";

    public JsonApiResponse() {
        super();
    }

    public JsonApiResponse(String json) {
        this(parseObject(json));
    }

    public JsonApiResponse(JSONObject object) {
        super(format(object));
    }


    /**成功*/
    public static final Integer CODE_SUCCESS = 200;
    /**编码错误*/
    public static final Integer CODE_UNSUPPORTED_ENCODING = 400;
    /**权限错误*/
    public static final Integer CODE_ILLEGAL_ACCESS = 401;
    /**禁止操作*/
    public static final Integer CODE_UNSUPPORTED_OPERATION = 403;
    /**未找到*/
    public static final Integer CODE_NOT_FOUND = 404;
    /**参数错误*/
    public static final Integer CODE_ILLEGAL_ARGUMENT = 406;
    /**未登录*/
    public static final Integer CODE_NOT_LOGGED_IN = 407;
    /**超时*/
    public static final Integer CODE_TIME_OUT = 408;
    /**重复，已存在*/
    public static final Integer CODE_CONFLICT = 409;
    /**条件错误，如密码错误*/
    public static final Integer CODE_CONDITION_ERROR = 412;
    /**类型错误*/
    public static final Integer CODE_UNSUPPORTED_TYPE = 415;
    /**超出范围*/
    public static final Integer CODE_OUT_OF_RANGE = 416;
    /**对象为空*/
    public static final Integer CODE_NULL_POINTER = 417;
    /**服务器内部错误*/
    public static final Integer CODE_SERVER_ERROR = 500;


    /**
     * 成功
     */
    public static final String MSG_SUCCEED = "success";
    /**
     * 服务器内部错误
     */
    public static final String MSG_SERVER_ERROR = "Internal Server Error!";


    public static final String KEY_CODE = "code";
    public static final String KEY_MSG = "msg";
    public static final String KEY_COUNT = "count";
    public static final String KEY_TOTAL = "total";

    /**
     * 获取状态
     *
     * @return
     */
    public int getCode() {
        try {
            return getIntValue(KEY_CODE);
        } catch (Exception e) {
            //empty
        }
        return 0;
    }

    /**
     * 获取状态
     *
     * @return
     */
    public static int getCode(JsonApiObject reponse) {
        try {
            return reponse.getIntValue(KEY_CODE);
        } catch (Exception e) {
            //empty
        }
        return 0;
    }

    /**
     * 获取状态描述
     *
     * @return
     */
    public String getMsg() {
        return getString(KEY_MSG);
    }

    /**
     * 获取状态描述
     *
     * @param reponse
     * @return
     */
    public static String getMsg(JSONObject reponse) {
        return reponse == null ? null : reponse.getString(KEY_MSG);
    }

    /**
     * 获取id
     *
     * @return
     */
    public long getId() {
        try {
            return getLongValue(KEY_ID);
        } catch (Exception e) {
            //empty
        }
        return 0;
    }

    /**
     * 获取数量
     *
     * @return
     */
    public int getCount() {
        try {
            return getIntValue(KEY_COUNT);
        } catch (Exception e) {
            //empty
        }
        return 0;
    }

    /**
     * 获取总数
     *
     * @return
     */
    public int getTotal() {
        try {
            return getIntValue(KEY_TOTAL);
        } catch (Exception e) {
            //empty
        }
        return 0;
    }


    /**
     * 是否成功
     *
     * @return
     */
    public boolean isSuccess() {
        return isSuccess(getCode());
    }

    /**
     * 是否成功
     *
     * @param code
     * @return
     */
    public static boolean isSuccess(int code) {
        return code == CODE_SUCCESS;
    }

    /**
     * 是否成功
     *
     * @param response
     * @return
     */
    public static boolean isSuccess(JsonApiResponse response) {
        return response != null && response.isSuccess();
    }

    /**
     * 是否成功
     *
     * @param response
     * @return
     */
    public static boolean isSuccess(JSONObject response) {
        return response != null && isSuccess(response.getIntValue(KEY_CODE));
    }

    /**
     * 校验服务端是否存在table
     *
     * @return
     */
    public boolean isExist() {
        return isExist(getCount());
    }

    /**
     * 校验服务端是否存在table
     *
     * @param count
     * @return
     */
    public static boolean isExist(int count) {
        return count > 0;
    }

    /**
     * 校验服务端是否存在table
     *
     * @param response
     * @return
     */
    public static boolean isExist(JsonApiResponse response) {
        return response != null && response.isExist();
    }

    /**
     * 获取内部的JSONResponse
     *
     * @param key
     * @return
     */
    public JsonApiResponse getJSONResponse(String key) {
        return getObject(key, JsonApiResponse.class);
    }
    //cannot get javaBeanDeserizer
    //	/**获取内部的JSONResponse
    //	 * @param response
    //	 * @param key
    //	 * @return
    //	 */
    //	public static JSONResponse getJSONResponse(JSONObject response, String key) {
    //		return response == null ? null : response.getObject(key, JSONResponse.class);
    //	}
    //状态信息，非GET请求获得的信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    /**
     * key = clazz.getSimpleName()
     *
     * @param clazz
     * @return
     */
    public <T> T getObject(Class<T> clazz) {
        return getObject(clazz == null ? "" : clazz.getSimpleName(), clazz);
    }

    /**
     * @param key
     * @param clazz
     * @return
     */
    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        return getObject(this, key, clazz);
    }

    /**
     * @param object
     * @param key
     * @param clazz
     * @return
     */
    public static <T> T getObject(JSONObject object, String key, Class<T> clazz) {
        return toObject(object == null ? null : object.getJSONObject(formatObjectKey(key)), clazz);
    }

    /**
     * @param clazz
     * @return
     */
    public <T> T toObject(Class<T> clazz) {
        return toObject(this, clazz);
    }

    /**
     * @param object
     * @param clazz
     * @return
     */
    public static <T> T toObject(JSONObject object, Class<T> clazz) {
        return JsonApi.parseObject(JsonApi.toJSONString(object), clazz);
    }


    /**
     * key = KEY_ARRAY
     *
     * @param clazz
     * @return
     */
    public <T> List<T> getList(Class<T> clazz) {
        return getList(KEY_ARRAY, clazz);
    }

    /**
     * arrayObject = this
     *
     * @param key
     * @param clazz
     * @return
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        return getList(this, key, clazz);
    }

    /**
     * key = KEY_ARRAY
     *
     * @param object
     * @param clazz
     * @return
     */
    public static <T> List<T> getList(JsonApiObject object, Class<T> clazz) {
        return getList(object, KEY_ARRAY, clazz);
    }

    /**
     * @param object
     * @param key
     * @param clazz
     * @return
     */
    public static <T> List<T> getList(JsonApiObject object, String key, Class<T> clazz) {
        return object == null ? null : JsonApi.parseArray(object.getString(formatArrayKey(key)), clazz);
    }

    /**
     * key = KEY_ARRAY
     *
     * @return
     */
    public JSONArray getArray() {
        return getArray(KEY_ARRAY);
    }

    /**
     * @param key
     * @return
     */
    public JSONArray getArray(String key) {
        return getArray(this, key);
    }

    /**
     * @param object
     * @return
     */
    public static JSONArray getArray(JsonApiObject object) {
        return getArray(object, KEY_ARRAY);
    }

    /**
     * key = KEY_ARRAY
     *
     * @param object
     * @param key
     * @return
     */
    public static JSONArray getArray(JsonApiObject object, String key) {
        return object == null ? null : object.getJSONArray(formatArrayKey(key));
    }

    /**
     * 格式化key名称
     *
     * @param object
     * @return
     */
    public static JSONObject format(final JSONObject object) {
        if (object == null || object.isEmpty()) {
            return object;
        }
        JsonApiObject formatedObject = new JsonApiObject(true);

        Set<String> set = object.keySet();
        if (set != null) {

            Object value;
            for (String key : set) {
                value = object.get(key);
                //JSONArray，遍历来format内部项
                if (value instanceof JSONArray) {
                    formatedObject.put(formatArrayKey(key), format((JSONArray) value));
                    //JSONObject，往下一级提取
                } else if (value instanceof JsonApiObject) {
                    formatedObject.put(formatObjectKey(key), format((JsonApiObject) value));
                } else {//其它Object，直接填充
                    formatedObject.put(formatOtherKey(key), value);
                }
            }
        }

        return formatedObject;
    }

    /**
     * 格式化key名称
     *
     * @param array
     * @return
     */
    public static JSONArray format(final JSONArray array) {
        if (array == null || array.isEmpty()) {
            return array;
        }
        JSONArray formatedArray = new JSONArray();

        Object value;
        for (int i = 0; i < array.size(); i++) {
            value = array.get(i);
            //JSONArray，遍历来format内部项
            if (value instanceof JSONArray) {
                formatedArray.add(format((JSONArray) value));
                //JSONObject，往下一级提取
            } else if (value instanceof JsonApiObject) {
                formatedArray.add(format((JsonApiObject) value));
            } else {//其它Object，直接填充
                formatedArray.add(value);
            }
        }

        return formatedArray;
    }


    /**
     * 获取表名称
     *
     * @param fullName name 或 name:alias
     * @return name => name; name:alias => alias
     */
    public static String getTableName(String fullName) {
        //key:alias  -> alias; key:alias[] -> alias[]
        int index = fullName == null ? -1 : fullName.indexOf(":");
        return index < 0 ? fullName : fullName.substring(0, index);
    }

    /**
     * 获取变量名
     *
     * @param fullName
     * @return {@link #formatKey(String, boolean, boolean, boolean, boolean)} formatColon = true, formatAt = true, formatHyphen = true, firstCase = true
     */
    public static String getVariableName(String fullName) {
        if (isArrayKey(fullName)) {
            fullName = StringUtil.addSuffix(fullName.substring(0, fullName.length() - 2), "list");
        }
        return formatKey(fullName, true, true, true, true);
    }

    /**
     * 格式化数组的名称 key[] => keyList; key:alias[] => aliasList; Table-column[] => tableColumnList
     *
     * @param key empty ? "list" : key + "List" 且首字母小写
     * @return {@link #formatKey(String, boolean, boolean, boolean, boolean)} formatColon = false, formatAt = true, formatHyphen = true, firstCase = true
     */
    public static String formatArrayKey(String key) {
        if (isArrayKey(key)) {
            key = StringUtil.addSuffix(key.substring(0, key.length() - 2), "list");
        }
        int index = key == null ? -1 : key.indexOf(":");
        if (index >= 0) {
            return key.substring(index + 1);
        }
        //节约性能，除了数组对象 Table-column:alias[] ，一般都符合变量命名规范
        return formatKey(key, false, true, true, true);
    }

    /**
     * 格式化对象的名称 name => name; name:alias => alias
     *
     * @param key name 或 name:alias
     * @return {@link #formatKey(String, boolean, boolean, boolean, boolean)} formatColon = false, formatAt = true, formatHyphen = false, firstCase = true
     */
    public static String formatObjectKey(String key) {
        int index = key == null ? -1 : key.indexOf(":");
        if (index >= 0) {
            //不处理自定义的
            return key.substring(index + 1);
        }

        //节约性能，除了表对象 Table:alias ，一般都符合变量命名规范
        return formatKey(key, false, true, false, true);
    }

    /**
     * 格式化普通值的名称 name => name; name:alias => alias
     *
     * @param fullName name 或 name:alias
     * @return {@link #formatKey(String, boolean, boolean, boolean, boolean)} formatColon = false, formatAt = true, formatHyphen = false, firstCase = false
     */
    public static String formatOtherKey(String fullName) {
        //节约性能，除了关键词 @key ，一般都符合变量命名规范，不符合也原样返回便于调试
        return formatKey(fullName, false, true, false, false);
    }


    /**
     * 格式化名称
     *
     * @param fullName     name 或 name:alias
     * @param formatAt     去除前缀 @ ， @a => a
     * @param formatColon  去除分隔符 : ， A:b => b
     * @param formatHyphen 去除分隔符 - ， A-b-cd-Efg => aBCdEfg
     * @param firstCase    第一个单词首字母小写，后面的首字母大写， Ab => ab ; A-b-Cd => aBCd
     * @return name => name; name:alias => alias
     */
    public static String formatKey(String fullName, boolean formatColon, boolean formatAt, boolean formatHyphen, boolean firstCase) {
        if (fullName == null) {
            return null;
        }

        if (formatColon) {
            fullName = formatColon(fullName);
        }
        //关键词只去掉前缀，不格式化单词，例如 @a-b 返回 a-b ，最后不会调用 setter
        if (formatAt) {
            fullName = formatAt(fullName);
        }
        if (formatHyphen) {
            fullName = formatHyphen(fullName, firstCase);
        }
        //不格式化普通 key:value (value 不为 [], {}) 的 key
        return firstCase ? StringUtil.firstCase(fullName) : fullName;
    }

    /**
     * "@key" => "key"
     *
     * @param key
     * @return
     */
    public static String formatAt(@NotNull String key) {
        return key.startsWith("@") ? key.substring(1) : key;
    }

    /**
     * key:alias => alias
     *
     * @param key
     * @return
     */
    public static String formatColon(@NotNull String key) {
        int index = key.indexOf(":");
        return index < 0 ? key : key.substring(index + 1);
    }

    /**
     * A-b-cd-Efg => ABCdEfg
     *
     * @param key
     * @return
     */
    public static String formatHyphen(@NotNull String key, boolean firstCase) {
        boolean first = true;
        int index;

        String name = "";
        String part;
        do {
            index = key.indexOf("-");
            part = index < 0 ? key : key.substring(0, index);

            name += firstCase && !first ? StringUtil.firstCase(part, true) : part;
            key = key.substring(index + 1);

            first = false;
        }
        while (index >= 0);

        return name;
    }


}