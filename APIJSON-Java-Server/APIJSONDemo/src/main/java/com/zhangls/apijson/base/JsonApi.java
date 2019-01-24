package com.zhangls.apijson.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by zhangls on 2019/1/4.
 * @author zhangls
 */
@Slf4j
public class JsonApi {

    /**
     * 判断json格式是否正确
     *
     * @param s
     * @return
     */
    public static boolean isJsonCorrect(String s) {

        if (s == null
                || s.equals("")
                || s.equals("[null]")
                || s.equals("{null}")
                || s.equals("null")) {
            return false;
        }
        return true;
    }

    /**
     * 获取有效的json
     *
     * @param json
     * @return
     */
    public static String getCorrectJson(String json) {
        return getCorrectJson(json, false);
    }

    /**
     * 获取有效的json
     *
     * @param json
     * @param isArray
     * @return
     */
    public static String getCorrectJson(String json, boolean isArray) {
        json = StringUtil.getTrimedString(json);
        return json;
    }



    public static Object parse(Object obj) {
        int features = JSON.DEFAULT_PARSER_FEATURE;
        features |= Feature.OrderedField.getMask();
        try {
            return JSON.parse(obj instanceof String ? (String) obj : toJSONString(obj), features);
        } catch (Exception e) {
            log.error("转换异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * obj转JSONObject
     *
     * @param
     * @return
     */
    public static JSONObject parseObject(Object obj) {
        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }
        return parseObject(toJSONString(obj));
    }

    /**
     * String转JSONObject
     *
     * @param json
     * @return
     */
    public static JSONObject parseObject(String json) {
        int features = JSON.DEFAULT_PARSER_FEATURE;
        features |= Feature.OrderedField.getMask();

        return parseObject(json, features);
    }

    /**
     * json转JSONObject
     *
     * @param json
     * @param features
     * @return
     */
    public static JSONObject parseObject(String json, int features) {
        try {
            return JSON.parseObject(getCorrectJson(json), JSONObject.class, features);
        } catch (Exception e) {
            log.error("转换异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * JSONObject转实体类
     *
     * @param object
     * @param clazz
     * @return
     */
    public static <T> T parseObject(JSONObject object, Class<T> clazz) {
        return parseObject(toJSONString(object), clazz);
    }

    /**
     * json转实体类
     * 一个组件中可能有很多特性需要支持，但在实际应用中却需要根据使用组件之人的喜好进行配置。比如在 FastJSON 组件中就有以下特性可由使用者自主选择：
     * AutoCloseSource, 自动关闭源
     * AllowComment, 支持注释
     * AllowUnQuotedFieldNames, 支持未引用的字段名
     * AllowSingleQuotes, 支持单引号
     * InternFieldNames,
     * AllowISO8601DateFormat, 支持ISO8601格式的日期
     * AllowArbitraryCommas, 支持任意多个逗号
     * UseBigDecimal, 使用大数
     * IgnoreNotMatch, 忽略不匹配的
     * SortFeidFastMatch,
     * DisableASM,
     * DisableCircularReferenceDetect, 关闭循环引用发现
     * InitStringFieldAsEmpty, 初始化字符串字段为 空字符串
     * SupportArrayToBean, 支持由数组转换成Bean对象
     * OrderedField, 按字段排序
     * DisableSpecialKeyDetect,
     * UseObjectArray,
     * SupportNonPublicField, 支持非 public 字段
     * IgnoreAutoType, 忽略自动类型
     * DisableFieldSmartMatch,  关闭字段的智能匹配特性
     * SupportAutoType, 支持自动类型
     * NonStringKeyAsString,
     * CustomMapDeserializer,
     *
     * fastjson默认开启的如下：
     * int features = 0;
     * features |= Feature.AutoCloseSource.getMask();
     * features |= Feature.InternFieldNames.getMask();
     * features |= Feature.UseBigDecimal.getMask();
     * features |= Feature.AllowUnQuotedFieldNames.getMask();
     * features |= Feature.AllowSingleQuotes.getMask();
     * features |= Feature.AllowArbitraryCommas.getMask();
     * features |= Feature.SortFeidFastMatch.getMask();
     * features |= Feature.IgnoreNotMatch.getMask();
     * DEFAULT_PARSER_FEATURE = features;
     *
     * @param jsonStr 需要转换的jsonStr
     * @param clazz 需要转换的目标类类型
     * @return
     */
    public static <T> T parseObject(String jsonStr, Class<T> clazz) {
        if (clazz == null) {
            log.error("参数类型异常：{}", clazz);
        } else {
            try {
                // 获取默认的配置
                int features = JSON.DEFAULT_PARSER_FEATURE;
                // 开启 OrderedField, 按字段排序
                features |= Feature.OrderedField.getMask();
                return JSON.parseObject(getCorrectJson(jsonStr), clazz, features);
            } catch (Exception e) {
                log.error("转换异常：{}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * list转JSONArray
     *
     * @param list
     * @return
     */
    public static JSONArray parseArray(List<Object> list) {
        return new JSONArray(list);
    }

    /**
     * obj转JSONArray
     *
     * @param obj
     * @return
     */
    public static JSONArray parseArray(Object obj) {
        if (obj instanceof JSONArray) {
            return (JSONArray) obj;
        }
        return parseArray(toJSONString(obj));
    }

    /**
     * json转JSONArray
     *
     * @param json
     * @return
     */
    public static JSONArray parseArray(String json) {
        try {
            return com.alibaba.fastjson.JSON.parseArray(getCorrectJson(json, true));
        } catch (Exception e) {
            log.error("转换异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * JSONArray转实体类列表
     *
     * @param array
     * @param clazz
     * @return
     */
    public static <T> List<T> parseArray(JSONArray array, Class<T> clazz) {
        return parseArray(toJSONString(array), clazz);
    }

    /**
     * json转实体类列表
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (clazz == null) {
            log.error("参数类型异常：{}", clazz);
        } else {
            try {
                return com.alibaba.fastjson.JSON.parseArray(getCorrectJson(json, true), clazz);
            } catch (Exception e) {
                log.error("转换异常：{}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 实体类转json
     *
     * @param obj
     * @return
     */
    public static String toJSONString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return com.alibaba.fastjson.JSON.toJSONString(obj);
        } catch (Exception e) {
            log.error("转换异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 实体类转json
     *
     * @param obj
     * @param features
     * @return
     */
    public static String toJSONString(Object obj, SerializerFeature... features) {
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return com.alibaba.fastjson.JSON.toJSONString(obj, features);
        } catch (Exception e) {
            log.error("转换异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 格式化，显示更好看
     *
     * @param json
     * @return
     */
    public static String format(String json) {
        return format(parse(json));
    }

    /**
     * 格式化，显示更好看
     *
     * @param object
     * @return
     */
    public static String format(Object object) {
        return toJSONString(object, SerializerFeature.PrettyFormat);
    }

    /**
     * 判断是否为JSONObject
     *
     * @param obj instanceof String ? parseObject
     * @return
     */
    public static boolean isJSONObject(Object obj) {
        if (obj instanceof JSONObject) {
            return true;
        }
        if (obj instanceof String) {
            try {
                JSONObject json = parseObject((String) obj);
                return json != null && json.isEmpty() == false;
            } catch (Exception e) {
                log.error("转换异常：{}", e.getMessage());
            }
        }

        return false;
    }

    /**
     * 判断是否为JSONArray
     *
     * @param obj instanceof String ? parseArray
     * @return
     */
    public static boolean isJSONArray(Object obj) {
        if (obj instanceof JSONArray) {
            return true;
        }
        if (obj instanceof String) {
            try {
                JSONArray json = parseArray((String) obj);
                return json != null && !json.isEmpty();
            } catch (Exception e) {
                log.error("转换异常：{}", e.getMessage());
            }
        }

        return false;
    }

    /**
     * 判断是否为 Boolean,Number,String 中的一种
     *
     * @param obj
     * @return
     */
    public static boolean isBooleanOrNumberOrString(Object obj) {
        return obj instanceof Boolean || obj instanceof Number || obj instanceof String;
    }
}
