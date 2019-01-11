package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.*;
import com.zhangls.apijson.utils.StringUtil;

import javax.activation.UnsupportedDataTypeException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

/**
 * Created by zhangls on 2019/1/11.
 */
public class ParserHelper {

    /**
     * 新建带状态内容的JSONObject
     *
     * @param code
     * @param msg
     * @return
     */
    public static JSONObject newResult(Integer code, String msg) {
        return extendResult(null, code, msg);
    }

    /**
     * 添加JSONObject的状态内容，一般用于错误提示结果
     *
     * @param object
     * @param code
     * @param msg
     * @return
     */
    public static JSONObject extendResult(JSONObject object, Integer code, String msg) {
        if (object == null) {
            object = new JSONObject(true);
        }
        if (!object.containsKey(JsonApiResponse.KEY_CODE)) {
            object.put(JsonApiResponse.KEY_CODE, code);
        }
        String m = StringUtil.getString(object.getString(JsonApiResponse.KEY_MSG));
        if (!m.isEmpty()) {
            msg = m + " ;\n " + StringUtil.getString(msg);
        }
        object.put(JsonApiResponse.KEY_MSG, msg);

        return object;
    }


    /**
     * 添加请求成功的状态内容
     *
     * @param object
     * @return
     */
    public static JSONObject extendSuccessResult(JSONObject object) {
        return extendResult(object, JsonApiResponse.CODE_SUCCESS, JsonApiResponse.MSG_SUCCEED);
    }

    /**
     * 获取请求成功的状态内容
     *
     * @return
     */
    public static JSONObject newSuccessResult() {
        return newResult(JsonApiResponse.CODE_SUCCESS, JsonApiResponse.MSG_SUCCEED);
    }

    /**
     * 添加请求成功的状态内容
     *
     * @param object
     * @return
     */
    public static JSONObject extendErrorResult(JSONObject object, Exception e) {
        JSONObject error = newErrorResult(e);
        return extendResult(object, error.getIntValue(JsonApiResponse.KEY_CODE), error.getString(JsonApiResponse.KEY_MSG));
    }

    /**
     * 新建错误状态内容
     *
     * @param e
     * @return
     */
    public static JSONObject newErrorResult(Exception e) {
        if (e != null) {
            e.printStackTrace();

            Integer code;
            if (e instanceof UnsupportedEncodingException) {
                code = JsonApiResponse.CODE_UNSUPPORTED_ENCODING;
            } else if (e instanceof IllegalAccessException) {
                code = JsonApiResponse.CODE_ILLEGAL_ACCESS;
            } else if (e instanceof UnsupportedOperationException) {
                code = JsonApiResponse.CODE_UNSUPPORTED_OPERATION;
            } else if (e instanceof NotExistException) {
                code = JsonApiResponse.CODE_NOT_FOUND;
            } else if (e instanceof IllegalArgumentException) {
                code = JsonApiResponse.CODE_ILLEGAL_ARGUMENT;
            } else if (e instanceof NotLoggedInException) {
                code = JsonApiResponse.CODE_NOT_LOGGED_IN;
            } else if (e instanceof TimeoutException) {
                code = JsonApiResponse.CODE_TIME_OUT;
            } else if (e instanceof ConflictException) {
                code = JsonApiResponse.CODE_CONFLICT;
            } else if (e instanceof ConditionErrorException) {
                code = JsonApiResponse.CODE_CONDITION_ERROR;
            } else if (e instanceof UnsupportedDataTypeException) {
                code = JsonApiResponse.CODE_UNSUPPORTED_TYPE;
            } else if (e instanceof OutOfRangeException) {
                code = JsonApiResponse.CODE_OUT_OF_RANGE;
            } else if (e instanceof NullPointerException) {
                code = JsonApiResponse.CODE_NULL_POINTER;
            } else {
                code = JsonApiResponse.CODE_SERVER_ERROR;
            }

            return newResult(code, e.getMessage());
        }

        return newResult(JsonApiResponse.CODE_SERVER_ERROR, JsonApiResponse.MSG_SERVER_ERROR);
    }

    /**
     * 根据路径取值
     *
     * @param parent
     * @param pathKeys
     * @return
     */
    protected static Object getValue(JSONObject parent, String[] pathKeys) {
        if (parent == null || pathKeys == null || pathKeys.length <= 0) {
            return parent;
        }

        //逐层到达child的直接容器JSONObject parent
        final Integer last = pathKeys.length - 1;
        for (Integer i = 0; i < last; i++) {//一步一步到达指定位置
            if (parent == null) {//不存在或路径错误(中间的key对应value不是JSONObject)
                break;
            }
            parent = getJSONObject(parent, pathKeys[i]);
        }

        return parent == null ? null : parent.get(pathKeys[last]);
    }


    /**
     * 获取被依赖引用的key的路径, 实时替换[] -> []/i
     *
     * @param parentPath
     * @param valuePath
     * @return
     */
    public static String getValuePath(String parentPath, String valuePath) {
        if (valuePath.startsWith("/")) {
            valuePath = getAbsPath(parentPath, valuePath);
        } else {//处理[] -> []/i
            valuePath = replaceArrayChildPath(parentPath, valuePath);
        }
        return valuePath;
    }

    /**
     * 获取绝对路径
     *
     * @param path
     * @param name
     * @return
     */
    public static String getAbsPath(String path, String name) {
        path = StringUtil.getString(path);
        name = StringUtil.getString(name);
        if (StringUtil.isNotEmpty(path, false)) {
            if (StringUtil.isNotEmpty(name, false)) {
                path += ((name.startsWith("/") ? "" : "/") + name);
            }
        } else {
            path = name;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    /**
     * 替换[] -> []/i
     * 不能写在getAbsPath里，因为name不一定是依赖路径
     *
     * @param parentPath
     * @param valuePath
     * @return
     */
    public static String replaceArrayChildPath(String parentPath, String valuePath) {
        String[] ps = StringUtil.split(parentPath, "]/");//"[]/");
        if (ps != null && ps.length > 1) {
            String[] vs = StringUtil.split(valuePath, "]/");

            if (vs != null && vs.length > 0) {
                String pos;
                for (Integer i = 0; i < ps.length - 1; i++) {
                    if (ps[i] == null || ps[i].equals(vs[i]) == false) {//允许""？
                        break;
                    }

                    pos = ps[i + 1].contains("/") == false ? ps[i + 1]
                            : ps[i + 1].substring(0, ps[i + 1].indexOf("/"));
                    if (
                        //StringUtil.isNumer(pos) &&
                            vs[i + 1].startsWith(pos + "/") == false) {
                        vs[i + 1] = pos + "/" + vs[i + 1];
                    }
                }
                return StringUtil.getString(vs, "]/");
            }
        }
        return valuePath;
    }

    public static JSONObject getJSONObject(JSONObject object, String key) {
        try {
            return object.getJSONObject(key);
        } catch (Exception e) {
            // todo exception
        }
        return null;
    }
}
