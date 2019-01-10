package com.zhangls.apijson.base.service.impl;


import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.utils.StringUtil;

import java.util.Map;

/**
 * JSONRequest for Server to replace zuo.biao.apijson.JSONRequest,
 * put JSON.parseObject(value) and not encode in default cases
 *
 * @author Lemon
 */
public class JsonBaseRequest extends JsonApiRequest {
    private static final long serialVersionUID = 1L;

    public JsonBaseRequest() {
        super();
    }

    /**
     * encode = true
     * {@link #JsonBaseRequest(String, Object)}
     *
     * @param object
     */
    public JsonBaseRequest(Object object) {
        super(object);
    }

    /**
     * @param name
     * @param object
     */
    public JsonBaseRequest(String name, Object object) {
        super(name, object);
    }


    @Override
    public JsonBaseRequest putsAll(Map<? extends String, ? extends Object> map) {
        super.putsAll(map);
        return this;
    }

    /**
     * @param value
     * @return {@link #puts(String, Object)}
     */
    @Override
    public JsonBaseRequest puts(Object value) {
        return puts(null, value);
    }

    /**
     * @param key
     * @param value
     * @return this
     * @see {@link #put(String, Object)}
     */
    @Override
    public JsonBaseRequest puts(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * @param value
     * @return {@link #put(String, Object)}
     */
    @Override
    public Object put(Object value) {
        return put(null, value);
    }

    /**
     * 自定义类型必须转为JSONObject或JSONArray，否则RequestParser解析不了
     */
    @Override
    public Object put(String key, Object value) {
        if (value == null) {//  || key == null
            return null;
        }

        Object target = JsonApi.parse(value);
        //		if (target == null) { // "tag":"User" 报错
        //			return null;
        //		}
        return super.put(StringUtil.isNotEmpty(key, true) ? key : value.getClass().getSimpleName() //must handle key here
                , target == null ? value : target);
    }

}
