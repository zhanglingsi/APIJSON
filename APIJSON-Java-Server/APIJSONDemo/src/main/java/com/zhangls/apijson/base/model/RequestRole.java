package com.zhangls.apijson.base.model;


import com.zhangls.apijson.utils.StringUtil;

/**
 * 来访的用户角色
 *
 * @author Lemon
 */
public enum RequestRole {

    /**
     * 未登录，不明身份的用户
     */
    UNKNOWN,

    /**
     * 已登录的用户
     */
    LOGIN,

    /**
     * 联系人，必须已登录
     */
    CONTACT,

    /**
     * 圈子成员(CONTACT + OWNER)，必须已登录
     */
    CIRCLE,

    /**
     * 拥有者，必须已登录
     */
    OWNER,

    /**
     * 管理员，必须已登录
     */
    ADMIN;


    public static final String[] NAMES = {
            UNKNOWN.name(), LOGIN.name(), CONTACT.name(), CIRCLE.name(), OWNER.name(), ADMIN.name()
    };

    public static RequestRole get(String name) throws Exception {
        if (name == null) {
            return null;
        }
        try {
            return RequestRole.valueOf(StringUtil.toUpperCase(name));
        } catch (Exception e) {
            throw new IllegalArgumentException("角色 " + name + " 不存在！只能是[" + StringUtil.getString(NAMES) + "]中的一种！", e);
        }
    }

}
