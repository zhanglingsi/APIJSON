package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.zhangls.apijson.annotation.MethodAccess;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.ConflictException;
import com.zhangls.apijson.base.exception.NotLoggedInException;
import com.zhangls.apijson.base.model.*;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.Verifier;
import com.zhangls.apijson.base.service.Visitor;
import com.zhangls.apijson.utils.StringUtil;

import javax.activation.UnsupportedDataTypeException;
import java.util.*;

/**
 * 权限验证
 *
 * @param <T> id 与 userId 的类型，一般为 Long
 * @author Lemon
 */
public abstract class AbstractVerifier<T> implements Verifier<T> {

    public static final Map<String, Map<RequestMethod, RequestRole[]>> ACCESS_MAP;

    static {
        ACCESS_MAP = Maps.newHashMap();

        ACCESS_MAP.put(Table.class.getSimpleName(), getAccessMap(Table.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Column.class.getSimpleName(), getAccessMap(Column.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Test.class.getSimpleName(), getAccessMap(Test.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Request.class.getSimpleName(), getAccessMap(Request.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Response.class.getSimpleName(), getAccessMap(Response.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Document.class.getSimpleName(), getAccessMap(Document.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(TestRecord.class.getSimpleName(), getAccessMap(TestRecord.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Function.class.getSimpleName(), getAccessMap(Function.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Access.class.getSimpleName(), getAccessMap(Access.class.getAnnotation(MethodAccess.class)));
    }

    /**
     * 获取权限Map，每种操作都只允许对应的角色
     *
     * @param access
     * @return
     */
    public static HashMap<RequestMethod, RequestRole[]> getAccessMap(MethodAccess access) {
        if (access == null) {
            return null;
        }

        HashMap<RequestMethod, RequestRole[]> map = Maps.newHashMap();
        map.put(RequestMethod.GET, access.GET());
        map.put(RequestMethod.HEAD, access.HEAD());
        map.put(RequestMethod.GETS, access.GETS());
        map.put(RequestMethod.HEADS, access.HEADS());
        map.put(RequestMethod.POST, access.POST());
        map.put(RequestMethod.PUT, access.PUT());
        map.put(RequestMethod.DELETE, access.DELETE());

        return map;
    }


    @NotNull
    protected Visitor<T> visitor;

    protected Object visitorId;

    @NotNull
    @Override
    public Visitor<T> getVisitor() {
        return visitor;
    }

    @Override
    public AbstractVerifier<T> setVisitor(Visitor<T> visitor) {
        this.visitor = visitor;
        this.visitorId = visitor == null ? null : visitor.getId();

        return this;
    }


    /**
     * 验证权限是否通过
     *
     * @param config
     * @return
     * @throws Exception
     */
    @Override
    public Boolean verify(SqlConfig config) throws Exception {
        String table = config == null ? null : config.getTable();
        if (table == null) {
            return true;
        }
        RequestRole role = config.getRole();
        if (role == null) {
            role = RequestRole.UNKNOWN;
        }

        if (role != RequestRole.UNKNOWN) {
            verifyLogin();
        }

        RequestMethod method = config.getMethod();

        verifyRole(table, method, role);


        //验证角色，假定真实强制匹配<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        String visitorIdkey = getVisitorIdKey(config.getTable());

        Object requestId;
        switch (role) {
            case LOGIN://verifyRole通过就行
                break;
            case CONTACT:
            case CIRCLE:
                //TODO 做一个缓存contactMap<visitorId, contactArray>，提高[]:{}查询性能， removeAccessInfo时map.remove(visitorId)
                //不能在Visitor内null -> [] ! 否则会导致某些查询加上不需要的条件！
                List<Object> list = visitor.getContactIdList() == null
                        ? new ArrayList<Object>() : new ArrayList<Object>(visitor.getContactIdList());
                if (role == RequestRole.CIRCLE) {
                    list.add(visitorId);
                }

                //key!{}:[] 或 其它没有明确id的条件 等 可以和key{}:list组合。类型错误就报错
                requestId = (Number) config.getWhere(visitorIdkey, true);//JSON里数值不能保证是Long，可能是Integer
                JSONArray requestIdArray = (JSONArray) config.getWhere(visitorIdkey + "{}", true);//不能是 &{}， |{} 不要传，直接{}
                if (requestId != null) {
                    if (requestIdArray == null) {
                        requestIdArray = new JSONArray();
                    }
                    requestIdArray.add(requestId);
                }

                if (requestIdArray == null) {//可能是@得到 || requestIdArray.isEmpty()) {//请求未声明key:id或key{}:[...]条件，自动补全
                    config.putWhere(visitorIdkey + "{}", JsonApi.parseArray(list), true); //key{}:[]有效，SQLConfig里throw NotExistException
                } else {//请求已声明key:id或key{}:[]条件，直接验证
                    for (Object id : requestIdArray) {
                        if (id == null) {
                            continue;
                        }
                        if (id instanceof Number == false) {//不能准确地判断Long，可能是Integer
                            throw new UnsupportedDataTypeException(table + ".id类型错误，id类型必须是Long！");
                        }
                        if (list.contains(new Long("" + id)) == false) {//Integer等转为Long才能正确判断。强转崩溃
                            throw new IllegalAccessException(visitorIdkey + " = " + id + " 的 " + table
                                    + " 不允许 " + role.name() + " 用户的 " + method.name() + " 请求！");
                        }
                    }
                }
                break;
            case OWNER:
                if (config.getMethod() == RequestMethod.POST) {
                    List<String> c = config.getColumn();
                    List<List<Object>> ovs = config.getValues();
                    if ((c == null || c.isEmpty()) || (ovs == null || ovs.isEmpty())) {
                        throw new IllegalArgumentException("POST 请求必须在Table内设置要保存的 key:value ！");
                    }

                    int index = c.indexOf(visitorIdkey);
                    if (index >= 0) {
                        Object oid;
                        for (List<Object> ovl : ovs) {
                            oid = ovl == null || index >= ovl.size() ? null : ovl.get(index);
                            if (oid == null || StringUtil.getString(oid).equals("" + visitorId) == false) {
                                throw new IllegalAccessException(visitorIdkey + " = " + oid + " 的 " + table
                                        + " 不允许 " + role.name() + " 用户的 " + method.name() + " 请求！");
                            }
                        }
                    } else {
                        List<String> nc = new ArrayList<>(c);
                        nc.add(visitorIdkey);
                        config.setColumn(nc);

                        List<List<Object>> nvs = new ArrayList<>();
                        List<Object> nvl;
                        for (List<Object> ovl : ovs) {
                            nvl = ovl == null || ovl.isEmpty() ? new ArrayList<>() : new ArrayList<>(ovl);
                            nvl.add(visitorId);
                            nvs.add(nvl);
                        }

                        config.setValues(nvs);
                    }
                } else {
                    requestId = config.getWhere(visitorIdkey, true);//JSON里数值不能保证是Long，可能是Integer
                    if (requestId != null && StringUtil.getString(requestId).equals(StringUtil.getString(visitorId)) == false) {
                        throw new IllegalAccessException(visitorIdkey + " = " + requestId + " 的 " + table
                                + " 不允许 " + role.name() + " 用户的 " + method.name() + " 请求！");
                    }

                    config.putWhere(visitorIdkey, visitorId, true);
                }
                break;
            case ADMIN://这里不好做，在特定接口内部判。 可以是  /get/admin + 固定秘钥  Parser#noVerify，之后全局跳过验证
                verifyAdmin();
                break;
            default://unknown，verifyRole通过就行
                break;
        }

        //验证角色，假定真实强制匹配>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        return true;
    }


    /**
     * 允许请求，角色不好判断，让访问者发过来角色名，OWNER,CONTACT,ADMIN等
     *
     * @param table
     * @param method
     * @param role
     * @return
     * @throws Exception
     */
    @Override
    public void verifyRole(String table, RequestMethod method, RequestRole role) throws Exception {
        if (table != null) {
            if (method == null) {
                method = RequestMethod.GET;
            }
            if (role == null) {
                role = RequestRole.UNKNOWN;
            }
            Map<RequestMethod, RequestRole[]> map = ACCESS_MAP.get(table);

            if (map == null || Arrays.asList(map.get(method)).contains(role) == false) {
                throw new IllegalAccessException(table + " 不允许 " + role.name() + " 用户的 " + method.name() + " 请求！");
            }
        }
    }


    /**
     * 登录校验
     *
     * @throws Exception
     * @author Lemon
     */
    @Override
    public void verifyLogin() throws Exception {
        //未登录没有权限操作
        if (visitorId == null) {
            throw new NotLoggedInException("未登录，请登录后再操作！");
        }

        if (visitorId instanceof Number) {
            if (((Number) visitorId).longValue() <= 0) {
                throw new NotLoggedInException("未登录，请登录后再操作！");
            }
        } else if (visitorId instanceof String) {
            if (StringUtil.isEmpty(visitorId, true)) {
                throw new NotLoggedInException("未登录，请登录后再操作！");
            }
        } else {
            throw new UnsupportedDataTypeException("visitorId 只能是 Long 或 String 类型！");
        }

    }

    @Override
    public void verifyAdmin() throws Exception {
        throw new UnsupportedOperationException("不支持 ADMIN 角色！如果要支持就在子类重写这个方法来校验 ADMIN 角色，不通过则 throw IllegalAccessException!");
    }


    /**
     * 验证是否重复
     *
     * @param table
     * @param key
     * @param value
     * @throws Exception
     */
    @Override
    public void verifyRepeat(String table, String key, Object value) throws Exception {
        verifyRepeat(table, key, value, 0);
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
    @Override
    public void verifyRepeat(String table, String key, Object value, long exceptId) throws Exception {
        if (key == null || value == null) {
            return;
        }
        if (value instanceof JsonApi) {
            throw new UnsupportedDataTypeException(key + ":value 中value的类型不能为JSON！");
        }

        JsonApiRequest request = new JsonApiRequest(key, value);
        if (exceptId > 0) {
            request.put(JsonApiRequest.KEY_ID + "!", exceptId);
        }
        JSONObject repeat = createParser().setMethod(RequestMethod.HEAD).setNoVerify(true).parseResponse(
                new JsonApiRequest(table, request)
        );
        repeat = repeat == null ? null : repeat.getJSONObject(table);
        if (repeat == null) {
            throw new Exception("服务器内部错误  verifyRepeat  repeat == null");
        }
        if (repeat.getIntValue(JsonApiResponse.KEY_COUNT) > 0) {
            throw new ConflictException(key + ": " + value + " 已经存在，不能重复！");
        }
    }

}
