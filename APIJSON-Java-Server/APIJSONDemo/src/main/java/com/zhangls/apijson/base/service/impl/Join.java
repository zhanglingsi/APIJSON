package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.service.SqlConfig;
import lombok.Data;


/**
 * JOIN 配置
 *
 * @author Lemon
 */
@Data
public class Join {

    private String path;

    private String originKey;
    private String originValue;

    /** "@" - APP, "&" - INNER, "|" - FULL, "!" - OUTTER, "<" - LEFT, ">" - RIGHT, "^" - SIDE, "*" - CROSS*/
    private String joinType;
    /** "" - 一对一, "{}" - 一对多, "<>" - 多对一*/
    private String relateType;
    /** { "id@":"/Moment/userId" } */
    private JSONObject table;
    private String name;
    private String key;
    private String targetName;
    private String targetKey;

    private SqlConfig joinConfig;
    private SqlConfig cacheConfig;


    public void setKeyAndType(@NotNull String originKey) throws Exception {
        if (originKey.endsWith("@")) {
            originKey = originKey.substring(0, originKey.length() - 1);
        } else {
            throw new IllegalArgumentException(joinType + "/.../" + name + "/" + originKey + " 不合法！join:'.../refKey'" + " 中 refKey 必须以 @ 结尾！");
        }

        if (originKey.endsWith("{}")) {
            setRelateType("{}");
            setKey(originKey.substring(0, originKey.length() - 2));
        } else if (originKey.endsWith("<>")) {
            setRelateType("<>");
            setKey(originKey.substring(0, originKey.length() - 2));
        } else {
            setRelateType("");
            setKey(originKey);
        }
    }

    public boolean isSQLJoin() {
        return !isAppJoin();
    }

    public static boolean isSQLJoin(Join j) {
        return j != null && j.isSQLJoin();
    }

    public boolean isAppJoin() {
        return "@".equals(getJoinType());
    }

    public static boolean isAppJoin(Join j) {
        return j != null && j.isAppJoin();
    }


}
