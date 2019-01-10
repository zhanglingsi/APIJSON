package com.zhangls.apijson.base;

import com.google.common.collect.Lists;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangls on 2019/1/9.
 *
 * @author zhangls
 */
@Slf4j
public class JsonApiRequest extends JsonApiObject {
    private static final long serialVersionUID = 1L;

    public JsonApiRequest() {
        super();
    }

    public JsonApiRequest(Object object) {
        this(null, object);
    }

    public JsonApiRequest(String name, Object object) {
        this();
        puts(name, object);
    }

    /**
     * 只在最外层，最外层用JSONRequest
     */
    public static final String KEY_TAG = "tag";
    /**
     * 只在最外层，最外层用JSONRequest
     */
    public static final String KEY_VERSION = "version";
    /**
     * 只在最外层，最外层用JSONRequest
     */
    public static final String KEY_FORMAT = "format";


    public JsonApiRequest setTag(String tag) {
        return puts(KEY_TAG, tag);
    }

    public JsonApiRequest setVersion(String version) {
        return puts(KEY_VERSION, version);
    }

    public JsonApiRequest setFormat(Boolean format) {
        return puts(KEY_FORMAT, format);
    }

    public static final int QUERY_TABLE = 0;
    public static final int QUERY_TOTAL = 1;
    public static final int QUERY_ALL = 2;

    public static final String KEY_QUERY = "query";
    public static final String KEY_COUNT = "count";
    public static final String KEY_PAGE = "page";
    public static final String KEY_JOIN = "join";

    public static final List<String> ARRAY_KEY_LIST;

    static {
        ARRAY_KEY_LIST = Lists.newArrayList();
        ARRAY_KEY_LIST.add(KEY_QUERY);
        ARRAY_KEY_LIST.add(KEY_COUNT);
        ARRAY_KEY_LIST.add(KEY_PAGE);
    }

    public JsonApiRequest setQuery(int query) {
        return puts(KEY_QUERY, query);
    }

    public JsonApiRequest setCount(int count) {
        return puts(KEY_COUNT, count);
    }

    public JsonApiRequest setPage(int page) {
        return puts(KEY_PAGE, page);
    }

    public JsonApiRequest toArray(int count, int page) {
        return toArray(count, page, null);
    }

    public JsonApiRequest toArray(int count, int page, String name) {
        return new JsonApiRequest(StringUtil.getString(name) + KEY_ARRAY, this.setCount(count).setPage(page));
    }


    @Override
    public JsonApiObject putsAll(Map<? extends String, ? extends Object> map) {
        super.putsAll(map);
        return this;
    }

    @Override
    public JsonApiRequest puts(Object value) {
        return puts(null, value);
    }

    @Override
    public JsonApiRequest puts(String key, Object value) {
        super.puts(key, value);
        return this;
    }
}
